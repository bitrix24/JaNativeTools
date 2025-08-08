package com.janative.tools.deps.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.PhpReturn
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.janative.tools.deps.model.DependencyType

class DependencySorter {

    fun sortDependenciesInArray(
        project: Project,
        targetDepsPsiFile: PsiFile,
        phpElementFactory: PhpDepsPsiElementFactory,
        psiModifier: PsiModifier
    ) {
        val dependencyTypes = DependencyType.entries

        for (depType in dependencyTypes) {
            val arrayPsiElement = findDependencyArrayElement(targetDepsPsiFile, depType)
            if (arrayPsiElement != null) {
                sortSingleArray(project, arrayPsiElement, phpElementFactory, psiModifier)
            }
        }
    }

    private fun findDependencyArrayElement(psiFile: PsiFile, dependencyType: DependencyType): ArrayCreationExpression? {
        if (psiFile !is PhpFile) return null

        val phpReturn = PsiTreeUtil.findChildOfType(psiFile, PhpReturn::class.java) ?: return null
        val topLevelArray = phpReturn.argument as? ArrayCreationExpression ?: return null

        return topLevelArray.hashElements
            .firstOrNull {
                val key = it.key as? StringLiteralExpression
                key?.contents == dependencyType.value
            }
            ?.value as? ArrayCreationExpression
    }

    private fun sortSingleArray(
        project: Project,
        arrayPsiElement: ArrayCreationExpression,
        phpElementFactory: PhpDepsPsiElementFactory,
        psiModifier: PsiModifier
    ) {
        val stringLiterals = PsiTreeUtil.collectElementsOfType(arrayPsiElement, StringLiteralExpression::class.java)
        if (stringLiterals.size <= 1) return

        val sortedContents = stringLiterals
            .map { it.contents }
            .distinct()
            .sortedWith(
                compareBy(
                    { it.contains(":") }, // Sort by presence of ":" (items with ":" come last)
                    {
                        val path = if (it.contains(":")) it.substringAfter(":") else it
                        path.substringBefore('/')
                    },
                    {
                        val path = if (it.contains(":")) it.substringAfter(":") else it
                        path.count { ch -> ch == '/' }
                    },
                    { it }
                )
            )

        val newArray = phpElementFactory.createEmptyArray(project)

        sortedContents.forEach { value ->
            val newLiteral = phpElementFactory.createStringLiteral(project, value)
            psiModifier.addElementToArray(project, newArray, newLiteral, phpElementFactory)
        }

        arrayPsiElement.replace(newArray)
    }
}