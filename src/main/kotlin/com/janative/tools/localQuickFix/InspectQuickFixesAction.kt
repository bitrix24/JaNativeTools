import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.janative.tools.lib.localization.Loc

class InspectQuickFixesAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        Messages.showMessageDialog(
            event.project,
            Loc.getMessage("action.inspectQuickFixes.message"),
            Loc.getMessage("action.inspectQuickFixes.title"),
            Messages.getInformationIcon()
        )
    }
}