package com.janative.tools.inspection.requireJS.requireInspection.checkers

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.lib.localization.Loc
import com.janative.tools.inspection.requireJS.requireInspection.ProblemResult
import com.janative.tools.inspection.requireJS.requireInspection.CheckerResultData
import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.lib.utils.Format
import com.janative.tools.lib.utils.Path

class ExtensionInvalidRequireChecker() : BaseProblemChecker() {
    override fun check(
        node: JSCallExpression,
        checkerResultData: CheckerResultData,
        problemResult: ProblemResult?
    ): ProblemResult? {
        val defineFilePath = checkerResultData.file?.path ?: return checkNext(node, checkerResultData)
        val definePath = checkerResultData.definedPath
        val defineFile = Path.getJaNativeDefinePath(defineFilePath)

        if (defineFile != definePath) {
            val result = ProblemResult(
                id = "extensionInvalidRequirePath",
                message = Loc.getMessage("problem.require.path.invalid", definePath, defineFile),
                highlightType = ProblemHighlightType.WEAK_WARNING
            )
            return result
        }

        return checkNext(node, checkerResultData)
    }
}