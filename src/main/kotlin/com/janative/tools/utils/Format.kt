package com.janative.tools.utils

class Format {
    companion object {
        fun kebabToCamelCase(input: String): String {
            return input.split("-")
                .mapIndexed { _, word -> word.replaceFirstChar { it.uppercase() } }
                .joinToString("")
        }

        fun removeExtJs(fileName: String): String {
            return fileName.trim().removeSuffix(".js")
        }
    }
}