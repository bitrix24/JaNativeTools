package com.janative.tools.lib.utils

import com.janative.tools.deps.constants.ProjectStructureConstants
import com.janative.tools.deps.model.DependencyType

class Path {
    companion object {
        fun isJaNativeMobilePath(path: String): Boolean {
            return path.contains(ProjectStructureConstants.MOBILE_APP_DIR_NAME)
        }

        fun getJaNativeRelativePath(directoryPath: String): String {
            val parts = directoryPath.split('/')
            val mobileAppIndex = parts.indexOf(ProjectStructureConstants.MOBILE_APP_DIR_NAME)

            if (mobileAppIndex == -1 || mobileAppIndex + 1 >= parts.size) {
                return ""
            }

            for (i in mobileAppIndex + 1 until parts.size) {
                if (parts[i] == DependencyType.EXTENSIONS.value || parts[i] == DependencyType.COMPONENTS.value) {
                    val relevantPathParts = parts.subList(i + 1, parts.size)

                    val finalPathParts =
                        if (relevantPathParts.isNotEmpty() && relevantPathParts[0] == ProjectStructureConstants.BITRIX_DIR_NAME) {
                            relevantPathParts.subList(1, relevantPathParts.size)
                        } else {
                            relevantPathParts
                        }
                    return finalPathParts.joinToString("/")
                }
            }

            return ""
        }

        fun getJaNativeDefinePath(path: String): String {
            val relativePath = getJaNativeRelativePath(path)
            val normalized = relativePath.replace('\\', '/')
            val stripped = when {
                normalized.endsWith(ProjectStructureConstants.EXTENSION_FILE_NAME_JS) ->
                    normalized.removeSuffix(ProjectStructureConstants.EXTENSION_FILE_NAME_JS)

                normalized.endsWith(ProjectStructureConstants.COMPONENT_FILE_NAME_JS) ->
                    normalized.removeSuffix(ProjectStructureConstants.COMPONENT_FILE_NAME_JS)

                normalized.endsWith(".js") -> Format.removeExtJs(normalized)

                else -> normalized
            }

            return stripped.trimEnd('/')
        }
    }
}