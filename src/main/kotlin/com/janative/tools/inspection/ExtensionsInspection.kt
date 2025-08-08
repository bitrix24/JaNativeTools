package com.janative.tools.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.inspection.checkers.*
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.utils.Loc

class ExtensionsInspection : LocalInspectionTool() {

    override fun getGroupDisplayName(): String {
        return Loc.getMessage("inspection.name")
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JSElementVisitor() {

            private val depsManager: DepsManager by lazy { DepsManager.createDefault() }

            // Initialize the checker chain with adapted classes
            private val problemCheckerChain: ProblemCheckerChain by lazy {
                ProblemCheckerChain(
                    listOf(
                        // The order of checkers is important:
                        // 1. ExtensionNativeImportChecker: if this is a native path, other checks may not be needed.
                        ExtensionNativeImportChecker(),
                        // 2. ExtensionDoesNotExistChecker: checks if the file exists. If not, other deps.php checks are meaningless.
                        ExtensionDoesNotExistChecker(),
                        // 3. ExtensionNotFoundChecker: checks if the dependency is in deps.php (if the file exists).
                        ExtensionNotFoundChecker(depsManager),
                        // 4. ExtensionInvalidRequireChecker: checks the canonicality of the path if it is in deps.js.
                        ExtensionInvalidRequireChecker(depsManager)
                    )
                )
            }

            override fun visitJSCallExpression(node: JSCallExpression) {
                val firstChildText = node.children.getOrNull(0)?.firstChild?.text
                if (firstChildText != "require") return

                val jsLiteralExpressions = PsiTreeUtil.findChildrenOfType(node, JSLiteralExpression::class.java)
                if (jsLiteralExpressions.isEmpty()) {
                    return
                }
                val requiredPathLiteral = jsLiteralExpressions.first()

                val definePathFromRequire = DependencyPsiUtils.getStringLiteral(node)
                if (definePathFromRequire.isBlank()) return

                val initialMissingData = MissingData(path = definePathFromRequire, file = null)
                val initialProblemResult = ProblemResult(id = "", message = "", missingData = initialMissingData)

                val finalResult = problemCheckerChain.check(node, initialProblemResult)

                finalResult?.let { result ->
                    if (result.message.isNotBlank()) {
                        holder.registerProblem(
                            requiredPathLiteral,
                            result.message,
                            result.highlightType,
                            *result.fixes.toTypedArray()
                        )
                    }
                }
            }
        }
    }
}