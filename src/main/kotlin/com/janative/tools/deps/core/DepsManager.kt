package com.janative.tools.deps.core

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.janative.tools.deps.model.DependencyInfo
import com.janative.tools.deps.psi.PsiModifier
import com.janative.tools.deps.psi.PhpDepsPsiElementFactory
import com.janative.tools.deps.psi.DepsFileModifier
import com.janative.tools.deps.psi.DependencyExistenceChecker
import com.janative.tools.deps.psi.DependencySorter
import com.janative.tools.deps.resolver.DependencyTypeResolver
import com.janative.tools.deps.resolver.DependencyPathResolver
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.settings.MainSettings

class DepsManager(
    private val psiElementFactory: PhpDepsPsiElementFactory,
    private val psiModifier: PsiModifier,
    private val depsFileAccessor: DepsFileModifier,
    private val existenceChecker: DependencyExistenceChecker,
    private val dependencySorter: DependencySorter
) {
    private var depsPsiFile: PsiFile? = null

    companion object {
        fun createDefault(): DepsManager {
            val psiModifier = PsiModifier()
            return DepsManager(
                psiElementFactory = PhpDepsPsiElementFactory(),
                psiModifier = psiModifier,
                depsFileAccessor = DepsFileModifier(psiModifier),
                existenceChecker = DependencyExistenceChecker(),
                dependencySorter = DependencySorter()
            )
        }
    }

    fun setDepsFile(psiFile: PsiFile) {
        this.depsPsiFile = psiFile
    }

    fun hasDepsFile(): Boolean {
        return depsPsiFile != null
    }

    fun addMissingDependencies(
        targetDepsPsiFile: PsiFile,
        missingDependencyPaths: List<String>,
        project: Project,
    ) {
        val resolvedDependencies = mutableListOf<DependencyInfo>()

        if (!this.hasDepsFile()) {
            this.setDepsFile(targetDepsPsiFile)
        }

        for (definePath in missingDependencyPaths) {
            resolveDependencyInfo(definePath = definePath, project = project)?.let { info ->
                resolvedDependencies.add(info)
            }
        }

        if (resolvedDependencies.isEmpty()) return

        modifyPsiAndCommit(project, targetDepsPsiFile) {
            val groupedDependencies = resolvedDependencies.groupBy { it.type }

            // Stage 1: Adding all dependencies
            for ((depType, depsInfoList) in groupedDependencies) {
                val psiArrayContainer = depsFileAccessor.getOrCreateDependencyArrayElement(
                    targetDepsPsiFile,
                    depType,
                    project,
                    psiElementFactory
                ) ?: continue

                for (depInfo in depsInfoList) {
                    if (existenceChecker.checkExists(psiArrayContainer, depInfo.path)) {
                        continue
                    }

                    val literalExpression = psiElementFactory.createStringLiteral(
                        project,
                        depInfo.path,
                    )

                    psiModifier.addElementToArray(
                        project,
                        psiArrayContainer,
                        literalExpression,
                        psiElementFactory,
                    )
                }
            }

            // Stage 2: Sorting dependencies
            if (MainSettings.isDepsSortEnabled()) {
                dependencySorter.sortDependenciesInArray(
                    project,
                    targetDepsPsiFile,
                    psiElementFactory,
                    psiModifier,
                )
            }
//			CodeStyleManager.getInstance(project).reformat(targetDepsPsiFile)
        }
    }

    fun isDependencyListed(definePath: String, defineFile: VirtualFile?): Pair<Boolean, String?> {
        val depsFile = depsPsiFile ?: return Pair(false, null)
        val depInfo = resolveDependencyInfo(definePath, defineFile, depsFile.project) ?: return Pair(false, null)
        val mainArray = depsFileAccessor.findMainReturnArray(depsFile) ?: return Pair(false, depInfo.path)
        val exists = existenceChecker.checkExists(mainArray, depInfo.path)

        return Pair(exists, if (exists) depInfo.path else null)
    }

    private fun resolveDependencyInfo(
        definePath: String?,
        defineFile: VirtualFile? = null,
        project: Project
    ): DependencyInfo? {
        if (definePath.isNullOrBlank() || this.depsPsiFile == null) {
            return null
        }

        val defineFile =
            defineFile ?: DependencyPsiUtils.findFileByDefinePath(definePath, project)

        if (defineFile == null) {
            return null
        }

        val dependencyType = DependencyTypeResolver().resolve(
            definePath = definePath,
            defineFile = defineFile,
            project = project
        ) ?: return null

        val depsDir = this.depsPsiFile?.containingDirectory ?: return null
        val dependencyPath = DependencyPathResolver().resolve(
            depsDir,
            defineFile,
            definePath,
            dependencyType
        )
        return DependencyInfo(dependencyPath, dependencyType)
    }

    private fun modifyPsiAndCommit(project: Project, psiFile: PsiFile, action: () -> Unit) {
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(psiFile)

        WriteCommandAction.runWriteCommandAction(project, action)

        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            psiDocumentManager.commitDocument(document)

            if (!document.text.endsWith("\n")) {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.insertString(document.textLength, "\n")
                    psiDocumentManager.commitDocument(document)
                }
            }
        }
    }
}