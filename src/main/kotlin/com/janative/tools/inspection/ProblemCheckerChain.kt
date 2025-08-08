package com.janative.tools.inspection

import com.intellij.lang.javascript.psi.JSCallExpression

class ProblemCheckerChain(private val checkers: List<ProblemChecker>) {
    init {
        for (i in 0 until checkers.size - 1) {
            checkers[i].setNext(checkers[i + 1])
        }
    }

    fun check(
        node: JSCallExpression,
        initialResult: ProblemResult? = null
    ): ProblemResult? {
        return if (checkers.isNotEmpty()) checkers.first().check(node, initialResult) else initialResult
    }
}