package com.janative.tools.deps.constants

/**
 * Constants related to project structure and file/directory names
 * used in the dependency management system v2.
 */
object ProjectStructureConstants {
    // Directory names
    const val MOBILE_APP_DIR_NAME = "mobileapp"
    const val BITRIX_DIR_NAME = "bitrix"

    // File names (for DependencyTypeResolver)
    const val EXTENSION_FILE_NAME_JS = "extension.js"
    const val COMPONENT_FILE_NAME_JS = "component.js"
    const val DEPS_FILE_NAME = "deps.php"
}
