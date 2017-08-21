package consulo.presentationAssistant;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.KeyStroke;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 21-Aug-17
 */
public class ShortcutPresenter implements Disposable
{
	public static class ActionData
	{
		private String actionId;
		private Project project;
		private String actionText;

		public ActionData(String actionId, Project project, String actionText)
		{
			this.actionId = actionId;
			this.project = project;
			this.actionText = actionText;
		}
	}

	private Set<String> movingActions = ContainerUtil.newHashSet("EditorLeft", "EditorRight", "EditorDown", "EditorUp", "EditorLineStart", "EditorLineEnd", "EditorPageUp", "EditorPageDown",
			"EditorPreviousWord", "EditorNextWord", "EditorScrollUp", "EditorScrollDown", "EditorTextStart", "EditorTextEnd", "EditorDownWithSelection", "EditorUpWithSelection",
			"EditorRightWithSelection", "EditorLeftWithSelection", "EditorLineStartWithSelection", "EditorLineEndWithSelection", "EditorPageDownWithSelection", "EditorPageUpWithSelection");


	private Set<String> typingActions = ContainerUtil.newHashSet(IdeActions.ACTION_EDITOR_BACKSPACE, IdeActions.ACTION_EDITOR_ENTER, IdeActions.ACTION_EDITOR_NEXT_TEMPLATE_VARIABLE);

	private Set<String> parentGroupIds = ContainerUtil.newHashSet("CodeCompletionGroup", "FoldingGroup", "GoToMenu", "IntroduceActionsGroup");

	private ActionInfoPanel infoPanel;

	private Map<String, String> parentNames = new HashMap<>();

	public ShortcutPresenter()
	{
		enable();
	}

	public void enable()
	{
		ActionManager actionManager = ActionManager.getInstance();
		for(String groupId : parentGroupIds)
		{
			AnAction group = actionManager.getAction(groupId);
			if(group instanceof ActionGroup)
			{
				fillParentNames((ActionGroup) group, group.getTemplatePresentation().getText());
			}
		}

		actionManager.addAnActionListener(new AnActionListener()
		{
			private ActionData currentAction;

			@Override
			public void beforeActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent event)
			{
				currentAction = null;
				String id = ActionManager.getInstance().getId(anAction);
				if(id == null)
				{
					return;
				}
				if(!movingActions.contains(id) && !typingActions.contains(id) && event != null)
				{
					Project project = event.getProject();
					String text = event.getPresentation().getText();
					currentAction = new ActionData(id, project, text);
				}
			}

			@Override
			public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event)
			{
				ActionData actionData = currentAction;
				String actionId = ActionManager.getInstance().getId(action);
				if(actionData != null && actionData.actionId.equals(actionId))
				{
					showActionInfo(actionData);
				}
			}

		}, this);
	}

	public void disable()
	{
		if(infoPanel != null)
		{
			infoPanel.close();
			infoPanel = null;
		}
		Disposer.dispose(this);
	}

	public void addText(List<Pair<String, Font>> list, String text)
	{
		list.add(Pair.create(text, null));
	}

	public void showActionInfo(ActionData actionData)
	{
		String actionId = actionData.actionId;
		String parentGroupName = parentNames.get(actionId);
		String actionText = (parentGroupName != null ? parentGroupName + " " + MacKeymapUtil.RIGHT + " " : "") + StringUtil.trimEnd(StringUtil.notNullize(actionData.actionText), "...");

		List<Pair<String, Font>> fragments = new ArrayList<>();
		if(actionText.length() > 0)
		{
			addText(fragments, "<b>" + actionText + "</b>");
		}

		Keymaps.KeymapDescription mainKeymap = PresentationAssistant.getInstance().getConfiguration().mainKeymap;
		List<Pair<String, Font>> shortcutTextFragments = shortcutTextFragments(mainKeymap, actionId, actionText);
		if(!shortcutTextFragments.isEmpty())
		{
			if(!fragments.isEmpty())
			{
				addText(fragments, " via&nbsp;");
			}
			fragments.addAll(shortcutTextFragments);
		}

		Keymaps.KeymapDescription alternativeKeymap = PresentationAssistant.getInstance().getConfiguration().alternativeKeymap;
		if(alternativeKeymap != null)
		{
			String mainShortcut = shortcutText(mainKeymap.getKeymap().getShortcuts(actionId), mainKeymap.getKind());
			List<Pair<String, Font>> altShortcutTextFragments = shortcutTextFragments(alternativeKeymap, actionId, mainShortcut);
			if(!altShortcutTextFragments.isEmpty())
			{
				addText(fragments, "&nbsp;(");
				fragments.addAll(altShortcutTextFragments);
				addText(fragments, ")");
			}
		}

		Project realProject = actionData.project == null ? ArrayUtil.getFirstElement(ProjectManager.getInstance().getOpenProjects()) : actionData.project;
		if(realProject != null && !realProject.isDisposed() && realProject.isOpen())
		{
			if(infoPanel == null || !infoPanel.canBeReused())
			{
				infoPanel = new ActionInfoPanel(realProject, fragments);
			}
			else
			{
				infoPanel.updateText(realProject, fragments);
			}
		}
	}

	private List<Pair<String, Font>> shortcutTextFragments(Keymaps.KeymapDescription keymap, String actionId, String shownShortcut)
	{
		List<Pair<String, Font>> fragments = new ArrayList<>();
		String shortcutText = shortcutText(keymap.getKeymap().getShortcuts(actionId), keymap.getKind());
		if(StringUtil.isEmpty(shortcutText) || Comparing.equal(shortcutText, shownShortcut))
		{
			return fragments;
		}
		if(keymap.getKind() == Keymaps.KeymapKind.WIN || SystemInfo.isMac)
		{
			addText(fragments, shortcutText);
		}
		else if(MacKeyStrokePresentation.macKeyStrokesFont != null && MacKeyStrokePresentation.macKeyStrokesFont.canDisplayUpTo(shortcutText) == -1)
		{
			fragments.add(Pair.create(shortcutText, MacKeyStrokePresentation.macKeyStrokesFont));
		}
		else
		{
			String altShortcutAsWin = shortcutText(keymap.getKeymap().getShortcuts(actionId), Keymaps.KeymapKind.WIN);
			if(!altShortcutAsWin.isEmpty() & !Comparing.equal(shortcutText, altShortcutAsWin))
			{
				addText(fragments, altShortcutAsWin);
			}
		}

		String keymapText = keymap.getDisplayText();
		if(!StringUtil.isEmpty(keymapText))
		{
			addText(fragments, "&nbsp;" + keymapText);
		}
		return fragments;
	}

	private String shortcutText(Shortcut[] shortcuts, Keymaps.KeymapKind keymapKind)
	{
		if(shortcuts == null || shortcuts.length == 0)
		{
			return "";
		}
		return shortcutText(shortcuts[0], keymapKind);
	}

	@NotNull
	private String shortcutText(KeyStroke keyStroke, Keymaps.KeymapKind keymapKind)
	{
		switch(keymapKind)
		{
			case MAC:
				return StringUtil.notNullize(MacKeymapUtil.getKeyStrokeText(keyStroke));
			case WIN:
				int modifiers = keyStroke.getModifiers();
				List<String> list = Arrays.asList(modifiers > 0 ? WinKeyStrokePresentation.getWinModifiersText(modifiers) : null, WinKeyStrokePresentation.getWinKeyText(keyStroke.getKeyCode()));

				return String.join("+", list.stream().filter(s -> !StringUtil.isEmpty(s)).toArray(String[]::new)).trim();
			default:
				throw new UnsupportedOperationException();
		}
	}

	@NotNull
	private String shortcutText(Shortcut shortcut, Keymaps.KeymapKind keymapKind)
	{
		if(shortcut instanceof KeyboardShortcut)
		{
			List<KeyStroke> list = Arrays.asList(((KeyboardShortcut) shortcut).getFirstKeyStroke(), ((KeyboardShortcut) shortcut).getSecondKeyStroke());
			return StringUtil.join(ContainerUtil.mapNotNull(list, keyStroke ->
			{
				if(keyStroke == null)
				{
					return null;
				}
				return shortcutText(keyStroke, keymapKind);
			}), ", ");
		}

		return "";
	}

	private void fillParentNames(ActionGroup group, String parentName)
	{
		ActionManager actionManager = ActionManager.getInstance();
		for(AnAction item : group.getChildren(null))
		{
			if(item instanceof ActionGroup)
			{
				if(!((ActionGroup) item).isPopup())
				{
					fillParentNames((ActionGroup) item, parentName);
				}
			}
			else
			{
				String id = actionManager.getId(item);
				if(id != null)
				{
					parentNames.put(id, parentName);
				}
			}
		}
	}

	@Override
	public void dispose()
	{
	}
}
