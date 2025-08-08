package com.janative.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.PsiTreeUtil


class PsiHelper {
    companion object {
        fun getPsiElementText(element: PsiElement): String {
            val psiElements = PsiTreeUtil.findChildrenOfType(element, PsiElement::class.java)

            if (psiElements.size > 1) {
                return psiElements.elementAt(1).text
            }

            return ""
        }

        fun findFilesContainingText(project: Project, searchText: String, first: Boolean = false): List<VirtualFile> {
            val filesContainingText = mutableListOf<VirtualFile>()
            val psiSearchHelper = PsiSearchHelper.getInstance(project)
            val searchScope = GlobalSearchScope.projectScope(project)

            psiSearchHelper.processElementsWithWord(
                { element, _ ->
                    if (first) {
                        filesContainingText.add(element.containingFile.virtualFile)
                        return@processElementsWithWord false
                    }
                    true
                },
                searchScope,
                searchText,
                UsageSearchContext.ANY,
                true
            )

            return filesContainingText
        }
    }
}