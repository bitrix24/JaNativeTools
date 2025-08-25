package com.janative.tools.deps.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.deps.utils.DependencyPathUtils
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class DependencyExistenceChecker {

    fun checkExists(depsPhpPsiElement: PsiElement, dependencyContent: String): Boolean {
        val file = depsPhpPsiElement.containingFile ?: return false
        if (file.name != ProjectStructureConstants.DEPS_FILE_NAME) return false

        val depsDir = file.containingDirectory?.virtualFile ?: return false

        var found = false

        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (found) return
                if (element is StringLiteralExpression) {
                    var pathInDeps = element.contents

                    if (pathInDeps.startsWith("./") && !dependencyContent.startsWith("./")) {
                        val rel = pathInDeps.removePrefix("./")
                        val targetFile = depsDir.findFileByRelativePath("$rel.js")
                        if (targetFile != null) {
                            pathInDeps = DependencyPathUtils.createBundleRequirePath(targetFile)
                        }
                    }

                    if (pathsMatch(pathInDeps, dependencyContent)) {
                        found = true
                        return
                    }
                }

                super.visitElement(element)
            }
        })

        return found
    }

    private fun pathsMatch(a: String, b: String): Boolean {
        if (a == b) return true
        return DependencyPathUtils.isEquivalentTo(a, b)
    }
}