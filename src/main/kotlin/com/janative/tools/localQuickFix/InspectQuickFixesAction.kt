import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class InspectQuickFixesAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        Messages.showMessageDialog(
            event.project,
            "Hello, this is my custom action!",
            "Custom Action",
            Messages.getInformationIcon()
        )
    }
}