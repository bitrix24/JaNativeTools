package com.janative.tools.deps.utils

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.janative.tools.PsiHelper
import com.janative.tools.deps.constants.ProjectStructureConstants.COMPONENT_FILE_NAME_JS
import com.janative.tools.deps.constants.ProjectStructureConstants.DEPS_FILE_NAME
import com.janative.tools.deps.constants.ProjectStructureConstants.EXTENSION_FILE_NAME_JS
import com.janative.tools.deps.constants.ProjectStructureConstants.MOBILE_APP_DIR_NAME
import com.janative.tools.deps.model.DependencyType

object DependencyPsiUtils {
    private val rootIndicatorFiles = setOf(
        EXTENSION_FILE_NAME_JS,
        COMPONENT_FILE_NAME_JS,
        DEPS_FILE_NAME
    )

    fun collectDefinePaths(psiFile: PsiFile): List<String> {
        val definePaths = mutableSetOf<String>()
        val callExpressions = PsiTreeUtil.findChildrenOfType(psiFile, JSCallExpression::class.java)

        for (callExpression in callExpressions) {
            if (callExpression.methodExpression?.text == "require" && callExpression.argumentList != null) {
                val definePath = getStringLiteral(callExpression)
                if (definePath.isNotBlank()) {
                    definePaths.add(definePath)
                }
            }
        }
        return definePaths.toList()
    }

    fun collectFilesFromRootDirectory(rootDirectory: PsiDirectory): List<PsiFile> {
        val files = mutableListOf<PsiFile>()
        collectFilesRecursively(rootDirectory, files, isRoot = true)

        return files
    }

    fun findExtensionRootDirectory(fileDirectory: PsiDirectory?): PsiDirectory? {
        return generateSequence(fileDirectory) { it.parentDirectory }
            .firstOrNull { isExtensionRoot(it, true) }
    }

    fun findDepsPhpFile(fileDirectory: PsiDirectory?): PsiFile? {
        return findExtensionRootDirectory(fileDirectory)?.findFile(DEPS_FILE_NAME)
    }

    fun findFileByDefinePath(definePath: String, project: Project): PsiFile? {
        return ApplicationManager.getApplication().runReadAction<PsiFile?> {
            PsiHelper.findFilesContainingText(project, "jn.define('${definePath}'", true)
                .firstOrNull()
                ?.let { PsiManager.getInstance(project).findFile(it) }
        }
    }

    fun getStringLiteral(jsCallExpression: JSCallExpression): String {
        val expression = PsiTreeUtil.findChildrenOfType(jsCallExpression, JSLiteralExpression::class.java)

        return if (expression.isNotEmpty()) {
            expression.first().value.toString()
        } else {
            ""
        }
    }

    private fun collectFilesRecursively(currentDirectory: PsiDirectory, files: MutableList<PsiFile>, isRoot: Boolean) {
        currentDirectory.children.forEach { psiElement ->
            when (psiElement) {
                is PsiFile -> files.add(psiElement)
                is PsiDirectory -> {
                    if (isRoot || !isExtensionRoot(psiElement)) {
                        collectFilesRecursively(psiElement, files, isRoot = false)
                    }
                }
            }
        }
    }

    private fun isExtensionRoot(directory: PsiDirectory, withComponentCheck: Boolean = false): Boolean {
        val targetFileName = rootIndicatorFiles.find { directory.findFile(it) != null }

        return when {
            targetFileName == null -> false
            !withComponentCheck -> true
            targetFileName == COMPONENT_FILE_NAME_JS -> isPathValidForComponents(directory)
            else -> true
        }
    }

    private fun isPathValidForComponents(directory: PsiDirectory): Boolean {
        return generateSequence(directory.parentDirectory) { it.parentDirectory }
            .fold(false) { hasComponentsFolder, currentDirectory ->
                when (currentDirectory.name) {
                    DependencyType.COMPONENTS.value -> true
                    MOBILE_APP_DIR_NAME -> return hasComponentsFolder
                    else -> hasComponentsFolder
                }
            }
    }
}