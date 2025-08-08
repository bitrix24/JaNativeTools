package com.janative.tools

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class PsiElements {
    companion object {

        fun comma(project: Project): PsiElement {
            return PhpPsiElementFactory.createComma(project)
        }

        fun whiteSpace(project: Project): PsiElement {
            return PhpPsiElementFactory.createWhiteSpace(project)
        }

        fun newLine(project: Project): PsiElement {
            return PhpPsiElementFactory.createNewLine(project)
        }
    }

}