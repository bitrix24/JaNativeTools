package com.janative.tools.inspection.requireJS.requireInspection.quickFix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.janative.tools.lib.localization.Loc
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.deps.psi.DepsFileModifier

class CurrentDependencyFix(private val missingDepsName: String, private val contextPsiFile: PsiFile) : LocalQuickFix {
    override fun getName(): String = Loc.getMessage("quickfix.deps.add.current.name")

    override fun getFamilyName(): String = Loc.getMessage("quickfix.deps.add.current.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val depsPsiFile = DepsFileModifier.getOrCreateDepsFile(project, contextPsiFile.containingDirectory)

        if (depsPsiFile == null) return

        val depsManager = DepsManager.createDefault()
        depsManager.addMissingDependencies(
            depsPsiFile,
            listOf(missingDepsName),
            project,
        )
    }
}