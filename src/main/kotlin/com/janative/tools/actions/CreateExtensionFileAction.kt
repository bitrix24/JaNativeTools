package com.janative.tools.actions

import com.intellij.psi.PsiDirectory
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.IncorrectOperationException
import com.janative.tools.actions.template.BaseTemplate
import com.janative.tools.actions.template.ClassTemplate
import com.janative.tools.actions.template.FunctionTemplate
import com.janative.tools.utils.Format
import com.janative.tools.deps.constants.ProjectStructureConstants.EXTENSION_FILE_NAME_JS
import com.janative.tools.utils.Loc

class CreateExtensionFileAction : CreateFileFromTemplateAction(
    "Extension File", "Create a new JavaScript Extension", AllIcons.FileTypes.JavaScript
) {
    companion object {
        private const val CLASS_TEMPLATE = "Class"
        private const val FUNCTION_TEMPLATE = "Function"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("New Extension")
            .addKind(Loc.getMessage("template.extension.class"), AllIcons.FileTypes.JavaScript, CLASS_TEMPLATE)
            .addKind(Loc.getMessage("template.extension.function"), AllIcons.FileTypes.JavaScript, FUNCTION_TEMPLATE)
            .setDefaultText(Format.removeExtJs(EXTENSION_FILE_NAME_JS))
    }

    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
        return "Create JavaScript Extension $newName"
    }

    @Throws(IncorrectOperationException::class)
    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        val project = dir.project
        val template = FileTemplateManager.getInstance(project).getInternalTemplate("JavaScript File")

        return try {
            val psiElement = FileTemplateUtil.createFromTemplate(template, name, null, dir)
            val psiFile = psiElement as? PsiFile

            psiFile?.let {
                val document = PsiDocumentManager.getInstance(project).getDocument(it)
                document?.let { doc ->
                    WriteCommandAction.runWriteCommandAction(project) {
                        val templateInstance: BaseTemplate = when (templateName) {
                            CLASS_TEMPLATE -> ClassTemplate(dir, name)
                            FUNCTION_TEMPLATE -> FunctionTemplate(dir, name)
                            else -> throw IllegalArgumentException("Unexpected template name: $templateName")
                        }
                        val additionalContent = templateInstance.generateContent()
                        doc.insertString(doc.textLength, additionalContent)
                    }
                    PsiDocumentManager.getInstance(project).commitDocument(doc)
                }
            }

            psiFile
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Error creating file: ${e.message}", "Error")
            null
        }
    }
}