package com.janative.tools.utils

import java.util.ResourceBundle

class Loc {
    companion object {
        private fun getBundle(): ResourceBundle {
            return ResourceBundle.getBundle("i18n/messages")
        }

        fun getMessage(messageId: String): String {
            return getBundle().getString(messageId)
        }
    }
}