package com.janative.tools.deps.utils

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.deps.model.DependencyType
import com.janative.tools.utils.Format
import java.io.File
import java.nio.file.Paths

object DependencyPathUtils {

    private const val MODULE_PATH_SEPARATOR = ":"
    private const val DIRECTORY_SEPARATOR = "/"

    fun getDependencyType(file: VirtualFile): DependencyType {
        val path = file.path
        return when {
            path.contains("/${DependencyType.COMPONENTS.value}/") -> DependencyType.COMPONENTS
            path.contains("/${DependencyType.EXTENSIONS.value}/") -> DependencyType.EXTENSIONS
            else -> DependencyType.BUNDLE
        }
    }

    fun createRelativeBundlePath(definePath: String, rootDirectory: PsiDirectory, file: PsiFile?): String {
        val filePath = file?.virtualFile?.path
        val directoryPath = rootDirectory.virtualFile.path

        if (filePath != null) {
            val relativePath = getRelativePath(directoryPath, filePath)

            return toRelativeBundlePath(relativePath)
        }

        var defineBundlePath = definePath
        val modulePathParts = getJaNativeRelativePath(directoryPath).split('/')

        for (i in modulePathParts.indices) {
            val suffix = modulePathParts.subList(i, modulePathParts.size).joinToString("/")
            if (definePath.startsWith("$suffix/")) {
                defineBundlePath = definePath.substring(suffix.length + 1)
                break
            }
        }

        return toRelativeBundlePath(defineBundlePath)
    }

    private fun toRelativeBundlePath(path: String): String {
        return if (path.startsWith("..")) path else ".$DIRECTORY_SEPARATOR$path"
    }

    fun createBundleRequirePath(definedFile: VirtualFile): String {
        return Format.removeExtJs(getJaNativeRelativePath(definedFile.path))
    }

    fun createExtensionOrComponentPath(definePath: String, definedFile: PsiFile?): String {
        if (definedFile == null) return definePath

        val moduleName = getModuleNameForFile(definedFile.virtualFile)
        val jaNativeRelativePath = Format.removeExtJs(getJaNativeRelativePath(definedFile.virtualFile.path))

        return if (moduleName != null && jaNativeRelativePath.startsWith(moduleName)) {
            definePath.replaceFirst(DIRECTORY_SEPARATOR, MODULE_PATH_SEPARATOR)
        } else {
            definePath
        }
    }

    fun getModuleNameForFile(file: VirtualFile): String? {
        val parts = file.path.split('/')
        // Use values from the new DependencyType
        val extensionsIndex = parts.indexOf(DependencyType.EXTENSIONS.value)
        val componentsIndex = parts.indexOf(DependencyType.COMPONENTS.value)

        val targetIndex = when {
            extensionsIndex != -1 -> extensionsIndex
            componentsIndex != -1 -> componentsIndex
            else -> -1
        }

        if (targetIndex != -1 && targetIndex + 1 < parts.size) {
            val moduleName = parts[targetIndex + 1]
            return moduleName.ifEmpty { null }
        } else {
            return null
        }
    }

    fun getJaNativeRelativePath(directoryPath: String): String {
        val parts = directoryPath.split('/')
        val mobileAppIndex = parts.indexOf(ProjectStructureConstants.MOBILE_APP_DIR_NAME)

        if (mobileAppIndex == -1 || mobileAppIndex + 1 >= parts.size) {
            return ""
        }

        for (i in mobileAppIndex + 1 until parts.size) {
            if (parts[i] == DependencyType.EXTENSIONS.value || parts[i] == DependencyType.COMPONENTS.value) {
                val relevantPathParts = parts.subList(i + 1, parts.size)

                val finalPathParts =
                    if (relevantPathParts.isNotEmpty() && relevantPathParts[0] == ProjectStructureConstants.BITRIX_DIR_NAME) {
                        relevantPathParts.subList(1, relevantPathParts.size)
                    } else {
                        relevantPathParts
                    }
                return finalPathParts.joinToString("/")
            }
        }

        return ""
    }

    fun getRelativePath(basePath: String, targetPath: String): String {
        val base = Paths.get(basePath).toAbsolutePath().normalize()
        val target = Paths.get(targetPath).toAbsolutePath().normalize()

        val resultPath = try {
            base.relativize(target)
        } catch (_: IllegalArgumentException) {
            target
        }

        return resultPath.toString().replace(File.separator, DIRECTORY_SEPARATOR)
    }

    fun isEquivalentTo(path: String, targetPath: String): Boolean {
        return path == targetPath || path == targetPath.replaceFirst("/", ":")
    }


    fun getModuleByFile(file: VirtualFile): String {
        val parts = file.path.split("/")
        val extensionsIndex = parts.indexOf(DependencyType.EXTENSIONS.value)
        val targetIndex = if (extensionsIndex != -1) {
            extensionsIndex
        } else {
            parts.indexOf(DependencyType.COMPONENTS.value)
        }

        return if (targetIndex != -1 && targetIndex + 1 < parts.size) {
            parts[targetIndex + 1]
        } else {
            ""
        }
    }
}