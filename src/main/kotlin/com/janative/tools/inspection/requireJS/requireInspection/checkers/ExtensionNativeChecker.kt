package com.janative.tools.inspection.requireJS.requireInspection.checkers

import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.inspection.requireJS.requireInspection.ProblemResult
import com.janative.tools.inspection.requireJS.requireInspection.CheckerResultData

class ExtensionNativeImportChecker : BaseProblemChecker() {
    override fun check(
        node: JSCallExpression,
        checkerResultData: CheckerResultData,
        problemResult: ProblemResult?
    ): ProblemResult? {
        return if (checkerResultData.definedPath.startsWith("native/")) {
            null
        } else {
            checkNext(node, checkerResultData)
        }
    }
}