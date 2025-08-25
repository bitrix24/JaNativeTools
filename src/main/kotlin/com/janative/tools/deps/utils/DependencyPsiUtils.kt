package com.janative.tools.deps.utils

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
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

    fun isRequireCall(callExpression: JSCallExpression): Boolean {
        val ref = callExpression.methodExpression as? JSReferenceExpression ?: return false

        return !(ref.qualifier != null || ref.referenceName != "require")
    }

    fun extractRequirePath(callExpression: JSCallExpression): String? {
        if (!isRequireCall(callExpression)) return null

        val arg = callExpression.arguments.firstOrNull() as? JSLiteralExpression ?: return null
        val value = arg.value?.toString()?.trim().orEmpty()

        return value.takeIf { it.isNotEmpty() }
    }

    fun collectRequireCallExpressions(psiFile: PsiFile): Sequence<JSCallExpression> =
        PsiTreeUtil.findChildrenOfType(psiFile, JSCallExpression::class.java)
            .asSequence()
            .filter { isRequireCall(it) }

    fun collectRequirePaths(psiFile: PsiFile): List<String> =
        collectRequireCallExpressions(psiFile)
            .mapNotNull { extractRequirePath(it) }
            .distinct()
            .toList()

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

    fun findFileByDefinePath(definePath: String, project: Project): VirtualFile? {
        return ApplicationManager.getApplication().runReadAction<VirtualFile?> {
            PsiHelper.findFilesContainingText(project, "jn.define('${definePath}'", true)
                .firstOrNull()
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