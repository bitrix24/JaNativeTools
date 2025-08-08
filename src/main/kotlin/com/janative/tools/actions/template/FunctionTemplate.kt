package com.janative.tools.actions.template

import com.intellij.psi.PsiDirectory

class FunctionTemplate(dir: PsiDirectory, name: String) : BaseTemplate(dir, name) {
    override fun createBodyContent(): String {
        return "function ${getClassName()}(){\n" +
                "}"
    }
}