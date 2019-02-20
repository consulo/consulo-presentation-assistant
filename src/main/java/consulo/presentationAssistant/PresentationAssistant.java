/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.presentationAssistant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author VISTALL
 * @since 21-Aug-17
 *
 * original author Nikolay Chashnikov (kotlin)
 */
@State(name = "PresentationAssistant", storages = @Storage("presentation-assistant.xml"))
public class PresentationAssistant implements Disposable, ApplicationComponent, PersistentStateComponent<PresentationAssistantState>
{
	@NotNull
	public static PresentationAssistant getInstance()
	{
		return ApplicationManager.getApplication().getComponent(PresentationAssistant.class);
	}

	private PresentationAssistantState myConfiguration = new PresentationAssistantState();
	private ShortcutPresenter myPresenter;

	@Nullable
	@Override
	public PresentationAssistantState getState()
	{
		return myConfiguration;
	}

	@Override
	public void loadState(PresentationAssistantState state)
	{
		XmlSerializerUtil.copyBean(state, myConfiguration);
	}

	@NotNull
	public PresentationAssistantState getConfiguration()
	{
		return myConfiguration;
	}

	public void setShowActionsDescriptions(boolean value, Project project)
	{
		myConfiguration.myShowActionDescriptions = value;
		if(value && myPresenter == null)
		{
			myPresenter = new ShortcutPresenter();
			myPresenter.showActionInfo(new ShortcutPresenter.ActionData("presentationAssistant.ShowActionDescriptions", project, "Show Descriptions of Actions", null));
		}

		if(!value && myPresenter != null)
		{
			myPresenter.disable();
			myPresenter = null;
		}
	}

	@NotNull
	@Override
	public String getComponentName()
	{
		return "PresentationAssistant";
	}

	@Override
	public void initComponent()
	{
		if(myConfiguration.myShowActionDescriptions)
		{
			myPresenter = new ShortcutPresenter();
		}
	}

	@Override
	public void dispose()
	{
		if(myPresenter != null)
		{
			myPresenter.disable();
		}
	}

	public void setFontSize(int value)
	{
		myConfiguration.myFontSize = value;
	}
}
