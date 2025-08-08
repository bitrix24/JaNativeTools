package com.janative.tools.actions.template

import com.intellij.psi.PsiDirectory

class ClassTemplate(dir: PsiDirectory, name: String) : BaseTemplate(dir, name) {
    override fun createBodyContent(): String {
        return "class ${getClassName()} extends LayoutComponent {\n" +
                "}"
    }
}