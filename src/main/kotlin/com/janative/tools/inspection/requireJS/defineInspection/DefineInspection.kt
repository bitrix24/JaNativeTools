package com.janative.tools.inspection.requireJS.defineInspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElementVisitor
import com.janative.tools.inspection.requireJS.BaseRequireJSInspection
import com.janative.tools.lib.utils.Format
import com.janative.tools.lib.localization.Loc
import com.janative.tools.lib.utils.Path
import com.intellij.lang.javascript.psi.JSCallExpression

class DefineInspection : BaseRequireJSInspection() {

    override fun getGroupDisplayName(): String {
        return Loc.getMessage("inspection.define.name")
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JSElementVisitor() {

            override fun visitJSCallExpression(node: JSCallExpression) {
                val filePath = node.containingFile.virtualFile.path
                if (!Path.isJaNativeMobilePath(filePath)) return

                val ref = node.methodExpression as? JSReferenceExpression ?: return
                if (ref.referenceName != "define") return

                val qualifierText = ref.qualifier?.text
                if (qualifierText != "jn") return

                val firstArg = node.arguments.firstOrNull() as? JSLiteralExpression ?: return
                val definePath = firstArg.stringValue ?: return

                val expectedDefinedPath = Format.removeExtJs(Path.getJaNativeDefinePath(filePath))
                if (definePath != expectedDefinedPath) {
                    holder.registerProblem(
                        firstArg,
                        Loc.getMessage("problem.define.path.mismatch", expectedDefinedPath),
                        DefinePathQuickFix(expectedDefinedPath)
                    )
                }
            }
        }
    }
}