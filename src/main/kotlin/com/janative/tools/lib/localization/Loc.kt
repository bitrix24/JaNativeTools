package com.janative.tools.lib.localization

import java.text.MessageFormat
import java.util.ResourceBundle

class Loc {
    companion object {
        private fun getBundle(): ResourceBundle {
            return ResourceBundle.getBundle("i18n/messages")
        }

        fun getMessage(messageId: String, vararg params: Any?): String {
            val pattern = getBundle().getString(messageId)
            return if (params.isEmpty()) pattern else MessageFormat.format(pattern, *params)
        }
    }
}