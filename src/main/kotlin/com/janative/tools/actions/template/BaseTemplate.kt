package com.janative.tools.actions.template

import com.intellij.psi.PsiDirectory
import com.janative.tools.deps.constants.ProjectStructureConstants.EXTENSION_FILE_NAME_JS
import com.janative.tools.lib.utils.Text
import com.janative.tools.lib.utils.Format
import com.janative.tools.lib.utils.Path

abstract class BaseTemplate(private val dir: PsiDirectory, private val name: String) {
    fun generateContent(): String {
        val definePath = getDefinePath()

        return createJnContent(definePath).trimIndent()
    }

    private fun getDefinePath(): String {
        val relativePath = Path.getJaNativeRelativePath(dir.virtualFile.path)

        if (isExtension()) {
            return relativePath
        }

        return "$relativePath/${getFileName()}"
    }

    private fun createJnContent(definePath: String): String {
        return """
/**
 * @module $definePath
 */
jn.define('$definePath', (require, exports, module) => {
    ${createBodyContent()}
 
    module.exports = {
        ${getClassName()}
    };
});

"""
    }

    private fun isExtension(): Boolean {
        return getFileName().lowercase() == Format.removeExtJs(EXTENSION_FILE_NAME_JS)
    }

    protected fun getClassName(): String {
        val name = if (isExtension()) dir.name else getFileName()

        return Text.kebabToCamelCase(name)
    }

    protected fun getFileName(): String {
        return Format.removeExtJs(name)
    }

    protected abstract fun createBodyContent(): String
}


