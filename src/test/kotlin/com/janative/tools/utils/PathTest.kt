package com.janative.tools.utils

import com.janative.tools.lib.utils.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PathTest {
    @Test
    fun `isMobilePath returns true for path containing mobileapp`() {
        assertTrue(Path.isJaNativeMobilePath("/project/mobileapp/module"))
        assertTrue(Path.isJaNativeMobilePath("mobileapp"))
        assertTrue(Path.isJaNativeMobilePath("/bitrix/mobileapp/"))
    }

    @Test
    fun `isJaNativeMobilePath returns false for path not containing mobileapp`() {
        assertFalse(Path.isJaNativeMobilePath("/project/webapp/module"))
        assertFalse(Path.isJaNativeMobilePath("/bitrix/admin/"))
        assertFalse(Path.isJaNativeMobilePath(""))
    }

    @Test
    fun `isJaNativeMobilePath is case sensitive`() {
        assertFalse(Path.isJaNativeMobilePath("/project/MobileApp/module"))
        assertFalse(Path.isJaNativeMobilePath("MOBILEAPP"))
    }

    // Tests for getJaNativeRelativePath
    @Test
    fun `getJaNativeRelativePath returns path after extensions directory`() {
        // Happy paths where 'extensions' is the dependency type directory; also tests bitrix trimming.
        assertEquals(
            "testing/printers.js",
            Path.getJaNativeRelativePath("/Users/user/project/mobile/install/mobileapp/mobile/extensions/bitrix/testing/printers.js")
        )
        assertEquals(
            "apptheme/src/list.js",
            Path.getJaNativeRelativePath("/Users/user/project/mobile/install/mobileapp/mobile/extensions/bitrix/apptheme/src/list.js")
        )
    }

    @Test
    fun `getJaNativeRelativePath returns path after components directory`() {
        // Should trim leading 'bitrix' segment when it is the first after 'components'.
        assertEquals(
            "chat/message/form.js",
            Path.getJaNativeRelativePath("/project/mobileapp/components/bitrix/chat/message/form.js")
        )
        // Should keep first segment when it is not 'bitrix'.
        assertEquals(
            "ui/button/index.ts",
            Path.getJaNativeRelativePath("/root/mobileapp/components/ui/button/index.ts")
        )
    }

    // Invalid / edge cases for getJaNativeRelativePath
    @Test
    fun `getJaNativeRelativePath returns empty when mobileapp not present`() {
        assertEquals("", Path.getJaNativeRelativePath("/Users/user/project/extensions/bitrix/testing/printers.js"))
        assertEquals("", Path.getJaNativeRelativePath("/extensions/bitrix/foo/bar.js"))
    }

    @Test
    fun `getJaNativeRelativePath returns empty when only mobileapp present`() {
        assertEquals("", Path.getJaNativeRelativePath("/Users/user/project/mobileapp"))
        assertEquals("", Path.getJaNativeRelativePath("/mobileapp/"))
    }

    @Test
    fun `getJaNativeRelativePath returns empty when dependency type dir has no trailing path`() {
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/extensions"))
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/components"))
        assertEquals("", Path.getJaNativeRelativePath("/a/b/mobileapp/extensions/"))
    }

    @Test
    fun `getJaNativeRelativePath returns empty when only bitrix after dependency type`() {
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/extensions/bitrix"))
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/extensions/bitrix/"))
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/components/bitrix"))
    }

    @Test
    fun `getJaNativeRelativePath returns empty when dependency type misspelled`() {
        // Should not match because directory is 'extension' not 'extensions'
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/extension/bitrix/foo"))
        // Should not match because 'component' not 'components'
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileapp/component/bitrix/foo"))
    }

    @Test
    fun `getJaNativeRelativePath returns empty when mobileapp appears only as substring of directory`() {
        // 'mobileappsrc' should not be treated as mobileapp directory
        assertEquals("", Path.getJaNativeRelativePath("/root/mobileappsrc/extensions/foo/bar"))
    }

    @Test
    fun `getJaNativeRelativePath handles multiple dependency type occurrences using first after mobileapp`() {
        // Ensures we stop at the first dependency type ('extensions') even if another ('components') follows.
        // After 'extensions' the relative path should include next directories unchanged.
        assertEquals("components/x", Path.getJaNativeRelativePath("/root/mobileapp/extensions/components/x"))
    }

    @Test
    fun `getJaNativeRelativePath ignores later dependency type directories before mobileapp`() {
        // components appears before mobileapp -> should not be considered
        assertEquals("foo/bar", Path.getJaNativeRelativePath("/root/components/tmp/mobileapp/extensions/foo/bar"))
    }

    @Test
    fun `getJaNativeRelativePath trims bitrix only if first segment`() {
        // bitrix removed only when directly following dependency type
        assertEquals("foo/bar", Path.getJaNativeRelativePath("/root/mobileapp/components/bitrix/foo/bar"))
        // bitrix retained when not first
        assertEquals("x/bitrix/foo", Path.getJaNativeRelativePath("/root/mobileapp/components/x/bitrix/foo"))
    }

    // getJaNativeDefinePath tests
    @Test
    fun `getJaNativeDefinePath handles extension define file removal`() {
        assertEquals(
            "testing/printers",
            Path.getJaNativeDefinePath("/root/mobileapp/extensions/bitrix/testing/printers/extension.js")
        )
        assertEquals(
            "foo/bar",
            Path.getJaNativeDefinePath("/mobileapp/extensions/foo/bar/extension.js")
        )
    }

    @Test
    fun `getJaNativeDefinePath handles component define file removal`() {
        assertEquals(
            "chat/message/form",
            Path.getJaNativeDefinePath("/project/mobileapp/components/bitrix/chat/message/form/component.js")
        )
        assertEquals(
            "ui/button",
            Path.getJaNativeDefinePath("/root/mobileapp/components/ui/button/component.js")
        )
    }

    @Test
    fun `getJaNativeDefinePath handles generic js file`() {
        assertEquals(
            "testing/printers/list",
            Path.getJaNativeDefinePath("/root/mobileapp/extensions/bitrix/testing/printers/list.js")
        )
        assertEquals(
            "chat/message/form/index",
            Path.getJaNativeDefinePath("/root/mobileapp/components/bitrix/chat/message/form/index.js")
        )
    }

    @Test
    fun `getJaNativeDefinePath returns unchanged for non js file`() {
        assertEquals(
            "testing/printers/readme",
            Path.getJaNativeDefinePath("/root/mobileapp/extensions/bitrix/testing/printers/readme")
        )
        assertEquals(
            "ui/button/index.ts",
            Path.getJaNativeDefinePath("/root/mobileapp/components/ui/button/index.ts")
        )
    }

    @Test
    fun `getJaNativeDefinePath normalizes backslashes`() {
        // Mixed separators inside the relative part; backslash should become '/'
        assertEquals(
            "sample/nested",
            Path.getJaNativeDefinePath("/root/mobileapp/extensions/bitrix/sample\\nested/extension.js")
        )
    }

    @Test
    fun `getJaNativeDefinePath returns empty when relative path empty`() {
        assertEquals("", Path.getJaNativeDefinePath("/root/mobileapp/extensions"))
        assertEquals("", Path.getJaNativeDefinePath("/root/mobileapp/components"))
    }

    @Test
    fun `getJaNativeDefinePath keeps uppercase JS filename intact`() {
        // Case-sensitive endings: EXTENSION.JS won't match 'extension.js' or '.js'
        assertEquals(
            "foo/EXTENSION.JS",
            Path.getJaNativeDefinePath("/root/mobileapp/extensions/bitrix/foo/EXTENSION.JS")
        )
    }
}
