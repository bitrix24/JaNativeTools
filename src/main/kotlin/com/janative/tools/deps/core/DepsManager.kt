package com.janative.tools.deps.core

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
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

    fun addMissingDependencies(
        targetDepsPsiFile: PsiFile,
        missingDependencyPaths: List<String>,
        project: Project,
    ) {
        val resolvedDependencies = mutableListOf<DependencyInfo>()

        for (pathString in missingDependencyPaths) {
            val definedFile = DependencyPsiUtils.findFileByDefinePath(pathString, project)
            val dependencyType = DependencyTypeResolver().resolve(pathString, definedFile, project)
            val dependencyPath = DependencyPathResolver().resolve(
                targetDepsPsiFile.containingDirectory,
                definedFile,
                pathString,
                dependencyType
            )

            resolvedDependencies.add(DependencyInfo(dependencyPath, dependencyType))
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
//            CodeStyleManager.getInstance(project).reformat(targetDepsPsiFile)
        }
    }

    fun isDependencyListed(
        contextFile: PsiFile,
        definePath: String,
    ): Pair<Boolean, String?> {
        val depsPsiFile = DependencyPsiUtils.findDepsPhpFile(contextFile.containingDirectory)
            ?: return Pair(false, null)

        val exists = existenceChecker.checkExists(depsPsiFile, definePath)

        return Pair(exists, definePath)
    }

    private fun modifyPsiAndCommit(project: Project, psiFile: PsiFile, action: () -> Unit) {
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(psiFile)

        WriteCommandAction.runWriteCommandAction(project, action)

        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            psiDocumentManager.commitDocument(document)
        }
    }
}