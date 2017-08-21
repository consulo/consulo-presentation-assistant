package consulo.presentationAssistant;

import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.util.SystemInfo;

/**
 * @author VISTALL
 * @since 21-Aug-17
 */
public class Keymaps
{
	static enum KeymapKind
	{
		WIN("Win/Linux", "$default"),
		MAC("Mac", "Mac OS X 10.5+");

		private String myDisplayName;
		private String myDefaultKeymapName;

		KeymapKind(String displayName, String defaultKeymapName)
		{

			myDisplayName = displayName;
			myDefaultKeymapName = defaultKeymapName;
		}

		public Keymap getKeymap()
		{
			switch(this)
			{
				case WIN:
					return winKeymap;
				case MAC:
					return macKeymap;
				default:
					throw new UnsupportedOperationException();
			}
		}

		public KeymapKind getAlternativeKind()
		{
			switch(this)
			{
				case WIN:
					return MAC;
				case MAC:
					return WIN;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	public static class KeymapDescription
	{
		private String name;
		private String displayText;

		public KeymapDescription(String name, String displayText)
		{
			this.name = name;
			this.displayText = displayText;
		}

		public KeymapKind getKind()
		{
			return name.contains("Mac OS") ? KeymapKind.MAC : KeymapKind.WIN;
		}

		public String getName()
		{
			return name;
		}

		public String getDisplayText()
		{
			return displayText;
		}

		public Keymap getKeymap()
		{
			return KeymapManager.getInstance().getKeymap(name);
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || getClass() != o.getClass())
			{
				return false;
			}

			KeymapDescription that = (KeymapDescription) o;

			if(name != null ? !name.equals(that.name) : that.name != null)
			{
				return false;
			}
			if(displayText != null ? !displayText.equals(that.displayText) : that.displayText != null)
			{
				return false;
			}

			return true;
		}

		@Override
		public int hashCode()
		{
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (displayText != null ? displayText.hashCode() : 0);
			return result;
		}
	}

	private static final Keymap winKeymap = KeymapManager.getInstance().getKeymap(KeymapKind.WIN.myDefaultKeymapName);
	private static final Keymap macKeymap = KeymapManager.getInstance().getKeymap(KeymapKind.MAC.myDefaultKeymapName);

	public static KeymapKind getCurrentOSKind()
	{
		if(SystemInfo.isMac)
		{
			return KeymapKind.MAC;
		}
		return KeymapKind.WIN;
	}

	public static KeymapDescription getDefaultMainKeymap()
	{
		return new KeymapDescription(getCurrentOSKind().myDefaultKeymapName, "");
	}

	public static KeymapDescription getDefaultAlternativeKeymap()
	{
		KeymapKind keymap = getCurrentOSKind().getAlternativeKind();
		if(keymap != null)
		{
			return new KeymapDescription(keymap.myDefaultKeymapName, "for " + keymap.myDisplayName);
		}
		return null;
	}
}
