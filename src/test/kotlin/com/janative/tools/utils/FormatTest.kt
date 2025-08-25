package com.janative.tools.utils

import com.janative.tools.lib.utils.Format
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test
    fun testGetFileNameFromAbsolutePath_FileWithExtension() {
        val path = "/Users/testuser/projects/myapp/src/Main.kt"
        val name = Format.getFileNameFromAbsolutePath(path)
        assertEquals("Main.kt", name)
    }

    @Test
    fun testGetFileNameFromAbsolutePath_FileWithTrailingSlash() {
        val path = "/Users/testuser/projects/myapp/src/Main.kt/"
        val name = Format.getFileNameFromAbsolutePath(path)
        assertEquals("", name)
    }

    @Test
    fun testGetFileNameFromAbsolutePath_DirectoryPath() {
        val path = "/Users/testuser/projects/myapp/src/"
        val name = Format.getFileNameFromAbsolutePath(path)
        assertEquals("", name)
    }

    @Test
    fun testGetFileNameFromAbsolutePath_NonexistentPath() {
        val path = "/non/existent/path/to/file.txt"
        val name = Format.getFileNameFromAbsolutePath(path)
        assertEquals("file.txt", name)
    }

    @Test
    fun testGetFileNameFromAbsolutePath_RootPathReturnsEmpty() {
        val path = "/"
        val name = Format.getFileNameFromAbsolutePath(path)
        assertEquals("", name)
    }

    @Test
    fun testGetFileNameFromAbsolutePath_EmptyOrWhitespacePathReturnsEmpty() {
        val path = "   "
        val name = Format.getFileNameFromAbsolutePath(path)
        assertEquals("", name)
    }

    @Test
    fun testRemoveExtJs() {
        assertEquals("filename", Format.removeExtJs("filename.js"))
        assertEquals("path/to/file", Format.removeExtJs("path/to/file.js"))
        assertEquals("noextension", Format.removeExtJs("noextension"))
    }
}