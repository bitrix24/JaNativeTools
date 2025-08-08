package com.janative.tools.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    @Test
    fun testKebabToCamelCase() {
        assertEquals("HelloWorld", Format.kebabToCamelCase("hello-world"))
        assertEquals("KebabToCamelCase", Format.kebabToCamelCase("kebab-to-camel-case"))
        assertEquals("Singleword", Format.kebabToCamelCase("singleword"))
    }

    @Test
    fun testRemoveExtJs() {
        assertEquals("filename", Format.removeExtJs("filename.js"))
        assertEquals("path/to/file", Format.removeExtJs("path/to/file.js"))
        assertEquals("noextension", Format.removeExtJs("noextension"))
    }
}