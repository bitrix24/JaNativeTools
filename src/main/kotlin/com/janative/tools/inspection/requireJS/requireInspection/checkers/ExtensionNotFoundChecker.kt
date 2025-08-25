package com.janative.tools.inspection.requireJS.requireInspection.checkers

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.inspection.requireJS.requireInspection.ProblemResult
import com.janative.tools.lib.localization.Loc
import com.intellij.codeInspection.ProblemHighlightType
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.inspection.requireJS.requireInspection.CheckerResultData
import com.janative.tools.inspection.requireJS.requireInspection.quickFix.AllDependenciesFix
import com.janative.tools.inspection.requireJS.requireInspection.quickFix.CurrentDependencyFix
import com.janative.tools.settings.MainSettings

class ExtensionNotFoundChecker(private val depsManager: DepsManager) : BaseProblemChecker() {
    override fun check(
        node: JSCallExpression,
        checkerResultData: CheckerResultData,
        problemResult: ProblemResult?
    ): ProblemResult? {
        val currentFile = node.containingFile

        if (!depsManager.hasDepsFile()) {
            val depsFile = DependencyPsiUtils.findDepsPhpFile(currentFile.containingDirectory)
            if (depsFile != null) {
                depsManager.setDepsFile(depsFile)
            }
        }

        val definePath = checkerResultData.definedPath

        val (isListed, _) = depsManager.isDependencyListed(definePath, checkerResultData.file)

        if (isListed) {
            return checkNext(node, checkerResultData)
        }

        val fixes = mutableListOf<LocalQuickFix>(AllDependenciesFix(currentFile))

        if (!MainSettings.isDepsAutoSyncEnabled()) {
            fixes.add(CurrentDependencyFix(definePath, currentFile))
        }


        return ProblemResult(
            id = "extensionNotFound",
            message = Loc.getMessage("problem.dependency.not.listed"),
            highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            fixes = fixes
        )
    }
}