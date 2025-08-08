    package com.janative.tools.deps.resolver

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.janative.tools.PsiHelper
import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.deps.constants.ProjectStructureConstants.EXTENSION_FILE_NAME_JS
import com.janative.tools.deps.model.DependencyType
import com.janative.tools.deps.utils.DependencyPsiUtils

class DependencyTypeResolver {

    fun resolve(
        definePath: String,
        definedFile: PsiFile?,
        project: Project
    ): DependencyType {
        val definedPsiFile = definedFile ?: DependencyPsiUtils.findFileByDefinePath(definePath, project)

        if (definedPsiFile == null) return DependencyType.EXTENSIONS

        return when (definedPsiFile.name.lowercase()) {
            ProjectStructureConstants.EXTENSION_FILE_NAME_JS -> DependencyType.EXTENSIONS
            ProjectStructureConstants.COMPONENT_FILE_NAME_JS -> DependencyType.COMPONENTS

            else -> DependencyType.BUNDLE
        }
    }
}
