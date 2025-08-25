package com.janative.tools.lib.utils

import java.io.File
import java.nio.file.Paths

class Format {
    companion object {

        fun removeExtJs(fileName: String): String {
            return fileName.trim().removeSuffix(".js")
        }

        fun getFileNameFromAbsolutePath(path: String): String {
            val trimmed = path.trim()
            if (trimmed.isEmpty() || trimmed.endsWith(File.separatorChar)) {
                return ""
            }
            val normalized = trimmed.trimEnd(File.separatorChar)
            return Paths.get(normalized).fileName?.toString() ?: ""
        }
    }
}