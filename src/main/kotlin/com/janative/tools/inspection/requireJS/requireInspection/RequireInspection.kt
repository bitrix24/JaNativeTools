package com.janative.tools.inspection.requireJS.requireInspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElementVisitor
import com.janative.tools.deps.core.DepsManager
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.inspection.requireJS.BaseRequireJSInspection
import com.janative.tools.inspection.requireJS.requireInspection.checkers.ExtensionDoesNotExistChecker
import com.janative.tools.inspection.requireJS.requireInspection.checkers.ExtensionInvalidRequireChecker
import com.janative.tools.inspection.requireJS.requireInspection.checkers.ExtensionNativeImportChecker
import com.janative.tools.inspection.requireJS.requireInspection.checkers.ExtensionNotFoundChecker
import com.janative.tools.lib.localization.Loc
import com.janative.tools.lib.utils.Path

class RequireInspection : BaseRequireJSInspection() {
    override fun getGroupDisplayName(): String = Loc.getMessage("inspection.require.name")

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JSElementVisitor() {
            private val depsManager: DepsManager by lazy { DepsManager.createDefault() }

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
                        ExtensionInvalidRequireChecker()
                    )
                )
            }

            override fun visitJSCallExpression(node: JSCallExpression) {
                val vfPath = node.containingFile.virtualFile?.path ?: return
                if (!Path.isJaNativeMobilePath(vfPath)) return

                if (!DependencyPsiUtils.isRequireCall(node)) return

                val requirePath = DependencyPsiUtils.extractRequirePath(node) ?: return
                if (requirePath.isBlank()) return

                val initialData = CheckerResultData(requirePath)
                val result = problemCheckerChain.check(node, initialData) ?: return
                if (result.message.isBlank()) return

                val argLiteral = node.arguments.firstOrNull() as? JSLiteralExpression ?: return
                holder.registerProblem(
                    argLiteral,
                    result.message,
                    result.highlightType,
                    *result.fixes.toTypedArray()
                )
            }
        }
    }
}