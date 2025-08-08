package com.janative.tools.deps.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression

class PsiModifier {

    fun addElementToArray(
        project: Project,
        arrayPsiElement: PsiElement,
        elementToAdd: PsiElement,
        phpElementFactory: PhpDepsPsiElementFactory,
        ensureNewLine: Boolean = true,
        ensureComma: Boolean = true
    ) {
        val phpArray = arrayPsiElement as? ArrayCreationExpression
        if (phpArray == null) {
            println("Error: arrayPsiElement is not ArrayCreationExpression")
            return
        }

        val elements =
            phpArray.children.filter { it !is PsiWhiteSpace && it.text != "," && it.text != "[" && it.text != "]" }
        val closingBracket = phpArray.lastChild?.takeIf { it.text == "]" } ?: return

        if (elements.isEmpty()) {
            val newElement = phpArray.addBefore(elementToAdd, closingBracket)
            if (ensureNewLine) {
                phpArray.addBefore(phpElementFactory.createNewLine(project), newElement)
                phpArray.addAfter(phpElementFactory.createNewLine(project), newElement)
            }
        } else {
            val lastElement = elements.last()

            if (ensureComma) {
                var next = lastElement.nextSibling
                while (next is PsiWhiteSpace) {
                    next = next.nextSibling
                }
                if (next?.text != ",") {
                    phpArray.addAfter(phpElementFactory.createComma(project), lastElement)
                }
            }

            val newElement = phpArray.addBefore(elementToAdd, closingBracket)
            if (ensureNewLine) {
                phpArray.addBefore(phpElementFactory.createNewLine(project), newElement)
                phpArray.addBefore(phpElementFactory.createNewLine(project), closingBracket)
            }
            if (ensureComma) {
                phpArray.addAfter(phpElementFactory.createComma(project), newElement)
            }
        }
    }

    fun addHashElementToArray(
        project: Project,
        targetArrayPsiElement: PsiElement,
        key: String,
        valuePsiElement: PsiElement,
        psiElementFactory: PhpDepsPsiElementFactory,
        ensureNewLine: Boolean = true,
        ensureComma: Boolean = true
    ) {
        if (targetArrayPsiElement !is ArrayCreationExpression) {
            println("Error: targetArrayPsiElement is not ArrayCreationExpression for addHashElementToArray")
            return
        }

        val arrayEntryText = "'$key' => ${valuePsiElement.text}"
        val newHashElement = psiElementFactory.createArrayHashElement(project, arrayEntryText)

        addElementToArray(project, targetArrayPsiElement, newHashElement, psiElementFactory, ensureNewLine, ensureComma)
    }

    fun addChildElement(project: Project, parentElement: PsiElement, childElement: PsiElement) {
        parentElement.add(childElement)
    }
}