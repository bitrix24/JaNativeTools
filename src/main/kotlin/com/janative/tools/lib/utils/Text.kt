package com.janative.tools.lib.utils

import kotlin.text.iterator

class Text {
    companion object {
        fun escapeForSingleQuotedJs(string: String): String {
            val sb = StringBuilder(string.length + 8)
            for (ch in string) {
                when (ch) {
                    '\\' -> sb.append("\\\\")
                    '\'' -> sb.append("\\'")
                    '\n' -> sb.append("\\n")
                    '\r' -> sb.append("\\r")
                    '\t' -> sb.append("\\t")
                    else -> sb.append(ch)
                }
            }
            return sb.toString()
        }

        fun kebabToCamelCase(input: String): String {
            return input.split("-")
                .mapIndexed { _, word -> word.replaceFirstChar { it.uppercase() } }
                .joinToString("")
        }
    }
}