package com.janative.tools.deps.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.deps.utils.DependencyPathUtils
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class DependencyExistenceChecker {
    fun checkExists(depsPhpPsiElement: PsiElement, dependencyContent: String): Boolean {
        if (depsPhpPsiElement.containingFile.name != ProjectStructureConstants.DEPS_FILE_NAME) return false

        val stringLiterals = PsiTreeUtil.findChildrenOfType(depsPhpPsiElement, StringLiteralExpression::class.java)

        if (stringLiterals.isEmpty()) return false

        val depsDir = depsPhpPsiElement.containingFile.containingDirectory?.virtualFile ?: return false

        return stringLiterals.any { literal ->
            var pathInDeps = literal.contents

            if (pathInDeps.startsWith("./")) {
                val relativePath = pathInDeps.removePrefix("./")
                val targetFile = depsDir.findFileByRelativePath("$relativePath.js")
                if (targetFile != null) {
                    pathInDeps = DependencyPathUtils.createBundleRequirePath(targetFile)
                }
                pathInDeps == dependencyContent
            }

            DependencyPathUtils.isEquivalentTo(pathInDeps, dependencyContent)
        }
    }
}