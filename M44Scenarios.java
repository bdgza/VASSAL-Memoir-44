package com.memoir44.vassal;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.BadDataReport;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.PredefinedSetup;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.menu.ChildProxy;
import VASSAL.tools.menu.MenuItemProxy;
import VASSAL.tools.menu.MenuManager;
import VASSAL.tools.menu.MenuProxy;
import VASSAL.tools.menu.ParentProxy;

public class M44Scenarios extends AbstractConfigurable implements GameComponent {
	public static final String NAME = "name"; //$NON-NLS-1$
	protected MenuItemProxy menuItem;

	protected AbstractAction launchAction;

	public M44Scenarios() {
		launchAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				launch();
			}
		};
		menuItem = new MenuItemProxy(launchAction);
	}


	public String[] getAttributeDescriptions() {
		return new String[]{
				Resources.getString(Resources.NAME_LABEL)
		};
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[]{
				String.class
		};
	}

	public String[] getAttributeNames() {
		return new String[]{
				NAME
		};
	}

	public String getAttributeValueString(String key) {
		if (NAME.equals(key)) {
			return getConfigureName();
		} else {
			return null;
		}
	}

	public void setAttribute(String key, Object value) {
		if (NAME.equals(key)) {
			setConfigureName((String) value);
			menuItem.getAction().putValue(Action.NAME, (String) value);
		}
	}

	public VisibilityCondition getAttributeVisibility(String name) {
		return super.getAttributeVisibility(name);
	}

	public void launch() {
		M44ScenarioChooser chooser = new M44ScenarioChooser(GameModule.getGameModule().getGameState());
		chooser.setVisible(true);
	}

	public InputStream getSavedGameContents() throws IOException {
		return null;
	}

	private ChildProxy<?> getMenuInUse() {
		return menuItem;
	}

	public void addTo(Buildable parent) {
		if (parent instanceof GameModule) {
			MenuManager.getInstance().addToSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
		}

		//MenuManager.getInstance().removeAction("GameState.new_game"); //$NON-NLS-1$
		GameModule.getGameModule().getGameState().addGameComponent(this);
	}

	public void removeFrom(Buildable parent) {
		if (parent instanceof GameModule) {
			MenuManager.getInstance()
			.removeFromSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
		}

		GameModule.getGameModule().getGameState().removeGameComponent(this);
	}

	public Class<?>[] getAllowableConfigureComponents() {
		return new Class<?>[0];
	}

	public static String getConfigureTypeName() {
		return "Memoir '44 Scenario Chooser";
	}

	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("GameModule.htm", "M44Scenarios"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Command getRestoreCommand() {
		return null;
	}

	public void setup(boolean gameStarting) {
		launchAction.setEnabled(true); //!gameStarting);
	}
}
