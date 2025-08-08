package com.janative.tools.inspection.checkers

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.psi.PsiManager
import com.janative.tools.utils.Loc
import com.janative.tools.inspection.ProblemResult
import com.janative.tools.inspection.MissingData
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.deps.core.DepsManager

class ExtensionInvalidRequireChecker(private val depsManager: DepsManager) : BaseProblemChecker() {

    override fun check(
        node: JSCallExpression,
        currentResult: ProblemResult?
    ): ProblemResult? {
        val project = node.project
        val definePathFromRequire = DependencyPsiUtils.getStringLiteral(node)

        if (definePathFromRequire.isBlank()) {
            return checkNext(node, currentResult)
        }

        val extensionRootPsiDir =
            DependencyPsiUtils.findExtensionRootDirectory(node.containingFile.containingDirectory)
                ?: return checkNext(node, currentResult)

        val depsVirtualFile = extensionRootPsiDir.virtualFile.findChild("deps.js")
        val depsPsiFile = depsVirtualFile?.let { PsiManager.getInstance(project).findFile(it) }

        if (depsPsiFile == null) {
            return checkNext(node, currentResult)
        }

        val (isListed, canonicalPath) = depsManager.isDependencyListed(
            depsPsiFile,
            definePathFromRequire,
        )

        if (isListed && canonicalPath != null && definePathFromRequire != canonicalPath) {
            val result = ProblemResult(
                id = "extensionInvalidRequirePath",
                message = Loc.getMessage("extension.invalid.require.path") + ": " + definePathFromRequire + ", expected: " + canonicalPath,
                missingData = currentResult?.missingData ?: MissingData(definePathFromRequire, null),
                highlightType = ProblemHighlightType.WEAK_WARNING
            )
            return result
        }

        return checkNext(node, currentResult)
    }
}