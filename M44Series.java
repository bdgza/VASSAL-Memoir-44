package com.memoir44.vassal;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;

public class M44Series extends AbstractConfigurable implements GameComponent {
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String PREFIX = "prefix"; //$NON-NLS-1$

	protected String prefix;

	protected AbstractAction launchAction;

	public M44Series() {
		launchAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

			}
		};
	}

	public M44Series(String name, String prefix) {
		setConfigureName(name);
		this.prefix = prefix;
	}

	public String[] getAttributeDescriptions() {
		return new String[]{
				Resources.getString(Resources.NAME_LABEL),
				"Series Name: ","Prefix code: "
		};
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[]{
				String.class,
				String.class
		};
	}

	public String[] getAttributeNames() {
		return new String[]{
				NAME,
				PREFIX
		};
	}

	public String getAttributeValueString(String key) {
		if (NAME.equals(key)) {
			return getConfigureName();
		}
		else if (PREFIX.equals(key)) {
			return prefix;
		}
		else {
			return null;
		}
	}

	public void setAttribute(String key, Object value) {
		if (NAME.equals(key)) {
			setConfigureName((String) value);
		}
		else if (PREFIX.equals(key)) {
			if (value instanceof String)
				prefix = (String)value;
		}
	}
	
	public VisibilityCondition getAttributeVisibility(String name) {
		return super.getAttributeVisibility(name);
	}

	public void addTo(Buildable parent) {
		if (parent instanceof GameModule) {
			//MenuManager.getInstance().addToSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
		}

		//MenuManager.getInstance().removeAction("GameState.new_game"); //$NON-NLS-1$
		GameModule.getGameModule().getGameState().addGameComponent(this);
		//GameModule.getGameModule().getWizardSupport().addPredefinedSetup(this);
	}

	public void removeFrom(Buildable parent) {
		if (parent instanceof GameModule) {
			//MenuManager.getInstance()
			//          .removeFromSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
		}

		GameModule.getGameModule().getGameState().removeGameComponent(this);
		//GameModule.getGameModule().getWizardSupport().removePredefinedSetup(this);
	}

	public Class<?>[] getAllowableConfigureComponents() {
		return new Class<?>[]{M44Series.class};
	}

	public static String getConfigureTypeName() {
		return "Memoir '44 Series";
	}

	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("GameModule.htm", "M44Series"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public Command getRestoreCommand() {
		return null;
	}

	public void setup(boolean gameStarting) {
		launchAction.setEnabled(!gameStarting);
	}
}
