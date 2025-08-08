package com.janative.tools.deps.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.elements.PhpReturn

class PhpDepsPsiElementFactory {
    fun createStringLiteral(project: Project, content: String, useSingleQuotes: Boolean = true): PsiElement {
        return PhpPsiElementFactory.createStringLiteralExpression(project, content, useSingleQuotes)
    }

    fun createComma(project: Project): PsiElement {
        return PhpPsiElementFactory.createComma(project)
    }

    fun createNewLine(project: Project): PsiElement {
        return PhpPsiElementFactory.createNewLine(project)
    }

    fun createEmptyReturn(project: Project): PsiElement? {
        return PhpPsiElementFactory.createFromText(project, PhpReturn::class.java, "return [];")
    }

    fun createWhiteSpace(project: Project): PsiElement {
        return PhpPsiElementFactory.createWhiteSpace(project)
    }

    fun createEmptyArray(project: Project): PsiElement {
        return PhpPsiElementFactory.createFromText(project, PhpElementTypes.ARRAY_CREATION_EXPRESSION, "[]")
    }

    fun createArrayHashElement(
        project: Project,
        value: String,
    ): PsiElement {
        return PhpPsiElementFactory.createFromText(
            project,
            PhpElementTypes.HASH_ARRAY_ELEMENT,
            "[${value}]"
        )
    }
}
