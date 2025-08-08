package com.janative.tools.inspection

import com.intellij.lang.javascript.psi.JSCallExpression

interface ProblemChecker {
    fun setNext(nextChecker: ProblemChecker): ProblemChecker

    /**
     * Checks for a problem in the given JSCallExpression node.
     *
     * @param node The JSCallExpression to check.
     * @param currentResult The result from a previous checker in the chain, can be null.
     * @return A ProblemResult if an issue is found, or the currentResult if no issue is found by this checker,
     *         or null if no issue is found and currentResult was null.
     */
    fun check(
        node: JSCallExpression,
        currentResult: ProblemResult?
    ): ProblemResult?
}

