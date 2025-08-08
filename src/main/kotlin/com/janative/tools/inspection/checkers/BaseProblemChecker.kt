package com.janative.tools.inspection.checkers

import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.inspection.ProblemChecker
import com.janative.tools.inspection.ProblemResult

abstract class BaseProblemChecker : ProblemChecker {
    private var nextChecker: ProblemChecker? = null

    override fun setNext(nextChecker: ProblemChecker): ProblemChecker {
        this.nextChecker = nextChecker

        return nextChecker
    }

    protected fun checkNext(
        node: JSCallExpression,
        currentResult: ProblemResult?
    ): ProblemResult? {
        return if (nextChecker == null) currentResult else nextChecker?.check(node, currentResult)
    }
}