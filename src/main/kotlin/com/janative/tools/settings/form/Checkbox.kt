package com.janative.tools.settings.form

import javax.swing.JCheckBox

data class Checkbox(
    val key: String,
    val labelKey: String,
    val defaultValue: Boolean = false
) {
    lateinit var checkBox: JCheckBox
    var value: Boolean = defaultValue
}