package consulo.presentationAssistant;

import com.intellij.util.ui.JBUI;

/**
 * @author VISTALL
 * @since 21-Aug-17
 */
public class PresentationAssistantState
{
	public boolean myShowActionDescriptions = true;
	public int myFontSize = 24;
	public Keymaps.KeymapDescription mainKeymap = Keymaps.getDefaultMainKeymap();
	public Keymaps.KeymapDescription alternativeKeymap  = Keymaps.getDefaultAlternativeKeymap();

	public int getFontSize()
	{
		return JBUI.scaleFontSize(myFontSize);
	}
}
