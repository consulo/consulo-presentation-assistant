package consulo.presentationAssistant.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import consulo.presentationAssistant.PresentationAssistant;

/**
 * @author VISTALL
 * @since 21-Aug-17
 */
public class TogglePresentationAssistantAction extends ToggleAction
{
	public TogglePresentationAssistantAction()
	{
		super("Presentation Assistant");
	}

	@Override
	public boolean isSelected(AnActionEvent e)
	{
		return PresentationAssistant.getInstance().getConfiguration().myShowActionDescriptions;
	}

	@Override
	public void setSelected(AnActionEvent e, boolean state)
	{
		PresentationAssistant.getInstance().setShowActionsDescriptions(state, e.getProject());
	}
}
