package com.janative.tools.inspection.requireJS.requireInspection.quickFix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.lib.localization.Loc
import com.janative.tools.deps.psi.DepsFileModifier
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.settings.MainSettings

class AllDependenciesFix(private val contextPsiFile: PsiFile) : LocalQuickFix {

    override fun getName(): String = Loc.getMessage("quickfix.deps.add.all.name")

    override fun getFamilyName(): String = Loc.getMessage("quickfix.deps.add.all.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val definePathsToProcess = getCollectRequirePaths()

        if (definePathsToProcess.isEmpty()) return

        val depsPsiFile = DepsFileModifier.getOrCreateDepsFile(project, contextPsiFile.containingDirectory) ?: return

        val depsManager = DepsManager.createDefault()
        depsManager.addMissingDependencies(
            depsPsiFile,
            definePathsToProcess,
            project,
        )
    }

    fun getCollectRequirePaths(): List<String> {
        if (!MainSettings.isDepsAutoSyncEnabled()) {
            return DependencyPsiUtils.collectRequirePaths(contextPsiFile)
        }

        val rootDirectory = DependencyPsiUtils.findExtensionRootDirectory(contextPsiFile.containingDirectory)
            ?: return DependencyPsiUtils.collectRequirePaths(contextPsiFile)

        return DependencyPsiUtils.collectFilesFromRootDirectory(rootDirectory)
            .flatMap { DependencyPsiUtils.collectRequirePaths(it) }
    }
}