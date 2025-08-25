package com.janative.tools.deps.resolver

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.deps.model.DependencyType
import com.janative.tools.deps.utils.DependencyPsiUtils
import com.janative.tools.lib.utils.Format

class DependencyTypeResolver {

    fun resolve(
        definePath: String,
        defineFilePath: String? = null,
        defineFile: VirtualFile?,
        project: Project?
    ): DependencyType? {

        val fileName: String = defineFilePath
            ?.let { Format.getFileNameFromAbsolutePath(it) }
            ?: defineFile?.name
            ?: run {
                if (project != null) DependencyPsiUtils.findFileByDefinePath(definePath, project)?.name
                else null
            } ?: return null

        return when (fileName.lowercase()) {
            ProjectStructureConstants.EXTENSION_FILE_NAME_JS -> DependencyType.EXTENSIONS
            ProjectStructureConstants.COMPONENT_FILE_NAME_JS -> DependencyType.COMPONENTS
            else -> DependencyType.BUNDLE
        }
    }
}
