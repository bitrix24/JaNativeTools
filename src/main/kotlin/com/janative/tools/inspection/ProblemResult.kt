package com.janative.tools.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.vfs.VirtualFile

data class ProblemResult(
    val id: String,
    val message: String,
    val missingData: MissingData,
    val highlightType: ProblemHighlightType = ProblemHighlightType.WARNING,
    val fixes: List<LocalQuickFix> = emptyList()
)

data class MissingData(
    val path: String,
    var file: VirtualFile?
)