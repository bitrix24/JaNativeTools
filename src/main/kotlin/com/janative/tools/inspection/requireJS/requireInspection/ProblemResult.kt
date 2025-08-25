package com.janative.tools.inspection.requireJS.requireInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.vfs.VirtualFile

data class ProblemResult(
    val id: String,
    val message: String,
    val highlightType: ProblemHighlightType = ProblemHighlightType.WARNING,
    val fixes: List<LocalQuickFix> = emptyList()
)

data class CheckerResultData(
    val definedPath: String,
    var file: VirtualFile? = null
)