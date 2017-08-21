package consulo.presentationAssistant;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 21-Aug-17
 */
public class MacKeyStrokePresentation
{
	private static final Logger LOG = Logger.getInstance(MacKeyStrokePresentation.class);

	public static final Font macKeyStrokesFont;

	static
	{
		Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

		Font temp = allFonts[0];
		for(Font item : allFonts)
		{
			if(getNonDisplayableMacSymbols(item).size() < getNonDisplayableMacSymbols(temp).size())
			{
				temp = item;
			}
		}

		macKeyStrokesFont = temp;

		List<Pair<String, String>> macSymbols = getNonDisplayableMacSymbols(macKeyStrokesFont);
		if(!macSymbols.isEmpty())
		{
			LOG.warn("The following symbols from Mac shortcuts aren't supported in selected font:" + StringUtil.join(macSymbols, it -> it.getFirst(), ","));
		}
	}

	@NotNull
	public static List<Pair<String, String>> getNonDisplayableMacSymbols(Font font)
	{
		List<Pair<String, String>> result = new ArrayList<>();
		Field[] declaredFields = MacKeymapUtil.class.getDeclaredFields();
		for(Field field : declaredFields)
		{
			if(field.getType() == String.class && !Comparing.equal(field.getName(), "APPLE"))
			{
				try
				{
					String o = (String) field.get(null);
					if(font.canDisplayUpTo(o) != -1)
					{
						result.add(Pair.create(field.getName(), o));
					}
				}
				catch(IllegalAccessException e)
				{
					throw new Error(e);
				}
			}
		}
		return result;
	}
}