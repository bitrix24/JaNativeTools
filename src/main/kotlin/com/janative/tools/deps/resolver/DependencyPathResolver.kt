package com.janative.tools.deps.resolver

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.janative.tools.deps.utils.DependencyPathUtils
import com.janative.tools.deps.model.DependencyType

class DependencyPathResolver() {
    fun resolve(
        rootDirectory: PsiDirectory,
        defineFile: VirtualFile,
        definePath: String,
        dependencyType: DependencyType
    ): String {

        val canonicalPath = when (dependencyType) {
            DependencyType.BUNDLE -> DependencyPathUtils.createRelativeBundlePath(
                defineFile,
                rootDirectory,
            )

            DependencyType.EXTENSIONS,
            DependencyType.COMPONENTS -> DependencyPathUtils.createExtensionOrComponentPath(definePath, defineFile)
        }

        return canonicalPath
    }
}