package com.janative.tools.deps.utils

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.janative.tools.deps.model.DependencyType
import com.janative.tools.lib.utils.Format
import com.janative.tools.lib.utils.Path
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

    fun createRelativeBundlePath(file: VirtualFile, rootDirectory: PsiDirectory): String {
        val directoryPath = rootDirectory.virtualFile.path
        val relativePath = getRelativePath(directoryPath, file.path)

        return Format.removeExtJs(toRelativeBundlePath(relativePath))
    }

    private fun toRelativeBundlePath(path: String): String {
        return if (path.startsWith("..")) path else ".$DIRECTORY_SEPARATOR$path"
    }

    fun createBundleRequirePath(defineFile: VirtualFile): String {
        return Format.removeExtJs(Path.getJaNativeRelativePath(defineFile.path))
    }

    fun createExtensionOrComponentPath(definePath: String, defineFile: VirtualFile?): String {
        if (defineFile == null) return definePath

        val moduleName = getModuleNameForFile(defineFile)
        val jaNativeRelativePath = Format.removeExtJs(Path.getJaNativeRelativePath(defineFile.path))

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