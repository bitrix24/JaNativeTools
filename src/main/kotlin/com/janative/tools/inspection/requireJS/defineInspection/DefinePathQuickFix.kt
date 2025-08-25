package com.janative.tools.inspection.requireJS.defineInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSElementFactory
import com.intellij.psi.util.PsiTreeUtil
import com.janative.tools.lib.utils.Text
import com.janative.tools.lib.localization.Loc

class DefinePathQuickFix(private val expectedPath: String) : LocalQuickFix {
    override fun getName(): String = Loc.getMessage("quickfix.define.update.path.name")
    override fun getFamilyName(): String = Loc.getMessage("quickfix.define.update.path.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        if (element is JSLiteralExpression) {
            val current = element.stringValue
            if (current == expectedPath) return

            val literalText = "'" + Text.escapeForSingleQuotedJs(expectedPath) + "'"
            val fragment = JSElementFactory.createExpressionCodeFragment(project, literalText, element)
            val newLiteral = PsiTreeUtil.findChildOfType(fragment, JSLiteralExpression::class.java) ?: return

            element.replace(newLiteral)
        }
    }
}
