package com.janative.tools.inspection.requireJS.requireInspection.checkers

import com.intellij.lang.javascript.psi.JSCallExpression
import com.janative.tools.inspection.requireJS.requireInspection.CheckerResultData
import com.janative.tools.inspection.requireJS.requireInspection.ProblemChecker
import com.janative.tools.inspection.requireJS.requireInspection.ProblemResult

abstract class BaseProblemChecker : ProblemChecker {
    private var nextChecker: ProblemChecker? = null

    override fun setNext(nextChecker: ProblemChecker): ProblemChecker {
        this.nextChecker = nextChecker

        return nextChecker
    }

    protected fun checkNext(
        node: JSCallExpression,
        checkerResultData: CheckerResultData,
        problemResult: ProblemResult? = null
    ): ProblemResult? {
        return if (nextChecker == null) problemResult else nextChecker?.check(node, checkerResultData)
    }
}