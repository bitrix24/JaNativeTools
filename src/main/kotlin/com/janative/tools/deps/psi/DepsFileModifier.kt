package com.janative.tools.deps.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.janative.tools.deps.constants.ProjectStructureConstants.DEPS_FILE_NAME
import com.janative.tools.deps.core.DepsManager
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ArrayHashElement
import com.jetbrains.php.lang.psi.elements.PhpReturn
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.janative.tools.deps.model.DependencyType
import com.intellij.psi.PsiDocumentManager
import com.janative.tools.deps.utils.DependencyPsiUtils.findExtensionRootDirectory

class DepsFileModifier(private val psiModifier: PsiModifier) {

    companion object {
        private val logger = Logger.getInstance(DepsManager::class.java)

        fun getOrCreateDepsFile(project: Project, contextDirectory: PsiDirectory?): PsiFile? {
            val rootDir = findExtensionRootDirectory(contextDirectory)
            if (rootDir == null) {
                logger.warn("Could not find extension root directory for: ${contextDirectory?.virtualFile?.path}")
                return null
            }

            val deps = rootDir.findFile(DEPS_FILE_NAME)

            if (deps != null) return deps

            val holder = arrayOfNulls<PsiFile>(1)

            CommandProcessor.getInstance().executeCommand(
                project,
                {
                    ApplicationManager.getApplication().runWriteAction {
                        val created = rootDir.createFile(DEPS_FILE_NAME)
                        val docManager = PsiDocumentManager.getInstance(project)
                        holder[0] = created
                        val doc = docManager.getDocument(created)

                        if (doc != null) {
                            doc.setText("<?php\n\nreturn [];")
                            docManager.commitDocument(doc)
                        }
                    }
                },
                "Create Deps File",
                null
            )

            return holder[0]
        }
    }

    fun getOrCreateDependencyArrayElement(
        depsPsiFile: PsiFile,
        dependencyType: DependencyType,
        project: Project,
        psiElementFactory: PhpDepsPsiElementFactory
    ): PsiElement? {
        val dependencyKey = dependencyType.value

        val mainReturnArray = findMainReturnArray(depsPsiFile) ?: return null
        findDependencyArrayByKey(mainReturnArray, dependencyKey)?.let { return it }

        val newEmptyArrayForType = psiElementFactory.createEmptyArray(project)
        psiModifier.addHashElementToArray(
            project,
            mainReturnArray,
            dependencyKey,
            newEmptyArrayForType,
            psiElementFactory,
        )

        val updatedMainReturnArray = findMainReturnArray(depsPsiFile) ?: return null

        return findDependencyArrayByKey(updatedMainReturnArray, dependencyKey)
    }

    private fun findDependencyArrayByKey(
        mainArray: ArrayCreationExpression,
        key: String
    ): ArrayCreationExpression? {
        for (element in mainArray.children) {
            if (element is ArrayHashElement) {
                val keyElement = element.key
                if (keyElement is StringLiteralExpression && keyElement.contents == key) {
                    return element.value as? ArrayCreationExpression
                }
            }
        }
        return null
    }

    private fun findMainReturnArray(psiFile: PsiFile): ArrayCreationExpression? {
        val phpReturn = PsiTreeUtil.findChildOfType(psiFile, PhpReturn::class.java)

        return phpReturn?.firstPsiChild?.let { it as? ArrayCreationExpression }
    }
}
