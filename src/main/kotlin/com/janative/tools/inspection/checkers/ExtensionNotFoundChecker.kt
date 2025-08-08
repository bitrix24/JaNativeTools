package com.janative.tools.inspection.checkers

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.inspection.MissingData
import com.janative.tools.inspection.ProblemResult
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.utils.Loc
import com.intellij.codeInspection.ProblemHighlightType
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.inspection.quickFix.AllDependenciesFix
import com.janative.tools.inspection.quickFix.CurrentDependencyFix
import com.janative.tools.settings.MainSettings

class ExtensionNotFoundChecker(private val depsManager: DepsManager) : BaseProblemChecker() {

    override fun check(
        node: JSCallExpression,
        currentResult: ProblemResult?
    ): ProblemResult? {
        val definePathFromRequire = DependencyPsiUtils.getStringLiteral(node)

        if (definePathFromRequire.isBlank()) {
            return checkNext(node, currentResult)
        }

        val (isListed, _) = depsManager.isDependencyListed(
            node.containingFile,
            definePathFromRequire,
        )

        if (isListed) {
            return checkNext(node, currentResult)
        }

        var fixes = mutableListOf<LocalQuickFix>(AllDependenciesFix(node.containingFile))

        if (!MainSettings.isDepsAutoSyncEnabled()) {
            fixes.add(CurrentDependencyFix(definePathFromRequire, node.containingFile))
        }


        return ProblemResult(
            id = "extensionNotFound",
            message = Loc.getMessage("extension.not.found"),
            missingData = currentResult?.missingData ?: MissingData(definePathFromRequire, null),
            highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            fixes = fixes
        )
    }
}