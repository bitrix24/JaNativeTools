package com.janative.tools.colorPreviews

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.JBUI
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.janative.tools.apptheme.ColorList
import java.awt.Color
import javax.swing.Icon
import com.janative.tools.lib.localization.Loc

class ColorPreviewLineMarker : LineMarkerProviderDescriptor() {

    private val scale = JBUI.scale(10)
    private var colorList: ColorList? = null

    override fun getName() = Loc.getMessage("colorPreview.name")

    override fun getIcon(): Icon {
        val color = Color(0xFFFFA800.toInt(), true)
        return ColorIcon(scale, color)
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is JSReferenceExpression) {
            return null
        }

        if (element.firstChild.text != "Color") {
            return null
        }

        val text = element.text
        val colorPattern = Regex("Color\\.([a-zA-Z0-9_]+)")
        val matchResult = colorPattern.find(text)
        val colorName = matchResult?.groups?.get(1)?.value ?: return null

        if (colorList == null) {
            colorList = ColorList(element.project)
        }

        val hexColor = colorList?.getColor("light", colorName) ?: return null

        val color = Color.decode(hexColor)
        val icon = ColorIcon(scale, color)
        val tooltipText = Loc.getMessage("colorPreview.tooltip", hexColor)

        return LineMarkerInfo<PsiElement>(
            element,
            element.textRange,
            icon,
            { tooltipText },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltipText }
        )
    }
}