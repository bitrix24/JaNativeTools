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
import com.janative.tools.lib.utils.Format
import com.janative.tools.deps.constants.ProjectStructureConstants.EXTENSION_FILE_NAME_JS
import com.janative.tools.lib.localization.Loc

class CreateExtensionFileAction : CreateFileFromTemplateAction(
    Loc.getMessage("action.createExtensionFile.title"),
    Loc.getMessage("action.createExtensionFile.description"),
    AllIcons.FileTypes.JavaScript
) {
    companion object {
        private const val CLASS_TEMPLATE = "Class"
        private const val FUNCTION_TEMPLATE = "Function"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(Loc.getMessage("action.createExtensionFile.dialog.title"))
            .addKind(Loc.getMessage("template.extension.class"), AllIcons.FileTypes.JavaScript, CLASS_TEMPLATE)
            .addKind(Loc.getMessage("template.extension.function"), AllIcons.FileTypes.JavaScript, FUNCTION_TEMPLATE)
            .setDefaultText(Format.removeExtJs(EXTENSION_FILE_NAME_JS))
    }

    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
        return Loc.getMessage("action.createExtensionFile.actionName", newName)
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
                            else -> throw IllegalArgumentException(Loc.getMessage("error.unexpected.template.name", templateName))
                        }
                        val additionalContent = templateInstance.generateContent()
                        doc.insertString(doc.textLength, additionalContent)
                    }
                    PsiDocumentManager.getInstance(project).commitDocument(doc)
                }
            }

            psiFile
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                Loc.getMessage("error.creating.file", e.message ?: ""),
                Loc.getMessage("error.title")
            )
            null
        }
    }
}