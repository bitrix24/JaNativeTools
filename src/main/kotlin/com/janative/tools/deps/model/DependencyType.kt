package com.janative.tools.deps.model

enum class DependencyType(val value: String) {
    BUNDLE("bundle"),
    EXTENSIONS("extensions"),
    COMPONENTS("components");

    companion object {
        fun fromValue(value: String): DependencyType? {
            return entries.find { it.value == value }
        }
    }
}
