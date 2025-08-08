package com.janative.tools.inspection.checkers

import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.inspection.ProblemResult
import com.janative.tools.deps.utils.DependencyPsiUtils

class ExtensionNativeImportChecker : BaseProblemChecker() {
    override fun check(
        node: JSCallExpression,
        currentResult: ProblemResult?
    ): ProblemResult? {
        val missingPath = DependencyPsiUtils.getStringLiteral(node)

        return if (missingPath.startsWith("native/")) {
            null
        } else {
            checkNext(node, currentResult)
        }
    }
}