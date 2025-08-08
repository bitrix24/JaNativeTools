package com.janative.tools.inspection.quickFix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.utils.Loc
import com.janative.tools.deps.psi.DepsFileModifier
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.settings.MainSettings

class AllDependenciesFix(private val contextPsiFile: PsiFile) : LocalQuickFix {

    override fun getName(): String = Loc.getMessage("extension.all.name")

    override fun getFamilyName(): String = Loc.getMessage("extension.all.familyName")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val definePathsToProcess = getCollectDefinePaths()

        if (definePathsToProcess.isEmpty()) return

        val depsPsiFile = DepsFileModifier.getOrCreateDepsFile(project, contextPsiFile.containingDirectory)

        if (depsPsiFile == null) return

        val depsManager = DepsManager.createDefault()
        depsManager.addMissingDependencies(
            depsPsiFile,
            definePathsToProcess,
            project,
        )
    }

    fun getCollectDefinePaths(): List<String> {
        if (!MainSettings.isDepsAutoSyncEnabled()) {
            return DependencyPsiUtils.collectDefinePaths(contextPsiFile)
        }

        val rootDirectory = DependencyPsiUtils.findExtensionRootDirectory(contextPsiFile.containingDirectory)
            ?: return DependencyPsiUtils.collectDefinePaths(contextPsiFile)

        return DependencyPsiUtils.collectFilesFromRootDirectory(rootDirectory)
            .flatMap { DependencyPsiUtils.collectDefinePaths(it) }
    }
}