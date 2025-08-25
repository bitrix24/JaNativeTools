package com.janative.tools.utils

import com.janative.tools.lib.utils.Text
import kotlin.test.Test
import kotlin.test.assertEquals

class TextTest {
    @Test
    fun testKebabToCamelCase() {
        assertEquals("HelloWorld", Text.kebabToCamelCase("hello-world"))
        assertEquals("KebabToCamelCase", Text.kebabToCamelCase("kebab-to-camel-case"))
        assertEquals("Singleword", Text.kebabToCamelCase("singleword"))
    }
}