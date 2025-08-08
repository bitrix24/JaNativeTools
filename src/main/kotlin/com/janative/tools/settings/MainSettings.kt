package com.janative.tools.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JCheckBox
import java.awt.BorderLayout
import com.intellij.ide.util.PropertiesComponent
import com.janative.tools.utils.Loc
import com.intellij.ui.TitledSeparator
import com.janative.tools.settings.form.Checkbox

class MainSettings : Configurable {
    private var panel: JPanel? = null
    private val depsOptions = listOf(
        Checkbox("janative.tools.deps.sort", "settings.deps.sort.title"),
        Checkbox("janative.tools.deps.autosync", "settings.deps.autosync.title")
    )

    companion object {
        fun isDepsSortEnabled(): Boolean {
            return PropertiesComponent.getInstance().getBoolean("janative.tools.deps.sort", true)
        }

        fun isDepsAutoSyncEnabled(): Boolean {
            return PropertiesComponent.getInstance().getBoolean("janative.tools.deps.autosync", false)
        }
    }

    init {
        depsOptions.forEach { option ->
            option.value = PropertiesComponent.getInstance().getBoolean(option.key, option.defaultValue)
            option.checkBox = JCheckBox(Loc.getMessage(option.labelKey))
            option.checkBox.isSelected = option.value
        }
    }

    override fun getDisplayName(): String = Loc.getMessage("plugin.name")

    override fun createComponent(): JComponent? {
        panel = JPanel(BorderLayout())
        val optionsPanel = JPanel()
        optionsPanel.layout = java.awt.GridLayout(0, 1)

        val depsSectionLabel = TitledSeparator(Loc.getMessage("settings.deps.section.title"))
        optionsPanel.add(depsSectionLabel)

        depsOptions.forEach { option ->
            val optionPanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
            option.checkBox.isSelected = option.value
            optionPanel.add(option.checkBox)
            optionsPanel.add(optionPanel)
        }
        panel?.add(optionsPanel, BorderLayout.NORTH)

        return panel
    }

    override fun isModified(): Boolean {
        return depsOptions.any { it.checkBox.isSelected != it.value }
    }

    override fun apply() {
        depsOptions.forEach { option ->
            option.value = option.checkBox.isSelected
            PropertiesComponent.getInstance().setValue(option.key, option.value)
        }
    }

    override fun disposeUIResources() {
        panel = null
    }
}