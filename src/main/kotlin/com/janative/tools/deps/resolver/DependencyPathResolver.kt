package com.janative.tools.deps.resolver

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.janative.tools.deps.utils.DependencyPathUtils
import com.janative.tools.deps.model.DependencyType

class DependencyPathResolver() {
    fun resolve(
        rootDirectory: PsiDirectory,
        definedFile: PsiFile?,
        definePath: String,
        dependencyType: DependencyType
    ): String {

        val canonicalPath = when (dependencyType) {
            DependencyType.BUNDLE -> DependencyPathUtils.createRelativeBundlePath(
                definePath,
                rootDirectory,
                definedFile
            )

            DependencyType.EXTENSIONS,
            DependencyType.COMPONENTS -> DependencyPathUtils.createExtensionOrComponentPath(definePath, definedFile)
        }

        return canonicalPath
    }
}