package com.janative.tools.apptheme

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.janative.tools.deps.utils.DependencyPsiUtils
import org.json.JSONObject

class ColorList(private val project: Project) {
    private val colorMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    init {
        createColorMap()
    }

    private fun createColorMap() {
        val colorListFile: VirtualFile =
            DependencyPsiUtils.findFileByDefinePath("apptheme/src/list", project) ?: return
        val content = VfsUtil.loadText(colorListFile)
        val json = JSONObject(
            content
                .substringAfter("const colors = ")
                .substringBeforeLast("module.exports")
                .substringBeforeLast(";")
        )

        json.keys().forEach { theme ->
            val themeColors = json.getJSONObject(theme)
            val themeMap = mutableMapOf<String, String>()
            themeColors.keys().forEach { colorName ->
                themeMap[colorName] = themeColors.getString(colorName)
            }
            colorMap[theme] = themeMap
        }
    }

    fun getColor(themeId: String, colorName: String): String? {
        return colorMap[themeId]?.get(colorName)
    }
}