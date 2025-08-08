package com.janative.tools.inspection.checkers

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.codeInspection.ProblemHighlightType
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.inspection.ProblemResult
import com.janative.tools.utils.Loc
import com.janative.tools.inspection.MissingData

class ExtensionDoesNotExistChecker : BaseProblemChecker() {

    override fun check(
        node: JSCallExpression,
        currentResult: ProblemResult?
    ): ProblemResult? {
        val definePath = currentResult?.missingData?.path ?: DependencyPsiUtils.getStringLiteral(node)

        if (definePath.isBlank()) return checkNext(node, currentResult)

        val initialMissingData = currentResult?.missingData ?: MissingData(definePath, null)
        val resolvedVirtualFile = DependencyPsiUtils.findFileByDefinePath(definePath, node.project)

        if (resolvedVirtualFile == null) {
            return ProblemResult(
                id = "extensionDoesNotExist",
                message = Loc.getMessage("extension.does.not.exist"),
                missingData = initialMissingData,
                highlightType = ProblemHighlightType.ERROR
            )
        }

        val resolvedMissingData = initialMissingData.copy(file = resolvedVirtualFile.virtualFile)
        val resultToPass = currentResult?.copy(missingData = resolvedMissingData)
            ?: ProblemResult(
                id = "",
                message = "",
                missingData = resolvedMissingData
            )

        return checkNext(node, resultToPass)
    }
}