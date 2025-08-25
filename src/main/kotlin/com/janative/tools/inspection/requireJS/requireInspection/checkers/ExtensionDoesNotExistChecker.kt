package com.janative.tools.inspection.requireJS.requireInspection.checkers

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.codeInspection.ProblemHighlightType
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.inspection.requireJS.requireInspection.ProblemResult
import com.janative.tools.lib.localization.Loc
import com.janative.tools.inspection.requireJS.requireInspection.CheckerResultData

class ExtensionDoesNotExistChecker : BaseProblemChecker() {
    override fun check(
        node: JSCallExpression,
        checkerResultData: CheckerResultData,
        problemResult: ProblemResult?
    ): ProblemResult? {
        val definePath = checkerResultData.definedPath
        val file = DependencyPsiUtils.findFileByDefinePath(definePath, node.project)
            ?: return ProblemResult(
                id = "extensionDoesNotExist",
                message = Loc.getMessage("problem.dependency.file.missing"),
                highlightType = ProblemHighlightType.ERROR
            )

        return checkNext(node, CheckerResultData(definePath, file))
    }
}