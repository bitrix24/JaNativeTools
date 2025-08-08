package com.janative.tools.inspection.quickFix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.janative.tools.utils.Loc
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.deps.psi.DepsFileModifier

class CurrentDependencyFix(private val missingDepsName: String, private val contextPsiFile: PsiFile) : LocalQuickFix {
    override fun getName(): String = Loc.getMessage("extension.current.name")

    override fun getFamilyName(): String = Loc.getMessage("extension.current.familyName")

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