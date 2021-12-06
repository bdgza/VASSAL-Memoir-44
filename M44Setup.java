package memoir44;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.BadDataReport;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.ErrorDialog;

public class M44Setup extends AbstractConfigurable implements GameComponent {
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String FRONT = "front"; //$NON-NLS-1$
	public static final String OPERATION = "operation"; //$NON-NLS-1$
	public static final String TOURNAMENT = "tournament"; //$NON-NLS-1$
	public static final String SET = "set"; //$NON-NLS-1$
	public static final String MONTH = "month"; //$NON-NLS-1$
	public static final String YEAR = "year"; //$NON-NLS-1$
	public static final String M44CODE = "m44code"; //$NON-NLS-1$
	public static final String DOWID = "dowid"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	public static final String OFFICIAL = "official"; //$NON-NLS-1$
	public static final String AUTHOR = "author"; //$NON-NLS-1$
	public static final String CLASSIFIED = "classified"; //$NON-NLS-1$

	protected String fileName;
	protected String front;
	protected String operation;
	protected String tournament;
	protected String set;
	protected String month;
	protected String year;
	protected String m44code;
	protected String dowid;
	protected Boolean official;
	protected String scentype;
	protected String author = "";
	protected Boolean classified = false;

	protected AbstractAction launchAction;

	public M44Setup() {
		launchAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

			}
		};
	}


	public String[] getAttributeDescriptions() {
		return new String[]{
				Resources.getString(Resources.NAME_LABEL),
				"Files: ","Board Type: ", "Front: ","Operation: ","Tournament: ","Set: ","Month (mmm): ","Year (yy): ","Memoir '44 Code: ","DOW ID #","Official: ","Author: ", "Classified: "
		};
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[]{
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				Boolean.class,
				String.class,
				Boolean.class
		};
	}

	public String[] getAttributeNames() {
		return new String[]{
				NAME,
				FILE,
				TYPE,
				FRONT,
				OPERATION,
				TOURNAMENT,
				SET,
				MONTH,
				YEAR,
				M44CODE,
				DOWID,
				OFFICIAL,
				AUTHOR,
				CLASSIFIED
		};
	}

	public String getAttributeValueString(String key) {
		if (NAME.equals(key)) {
			return getConfigureName();
		}
		else if (FILE.equals(key)) {
			return fileName;
		}
		else if (TYPE.equals(key)) {
			return scentype;
		}
		else if (FRONT.equals(key)) {
			return front;
		}
		else if (OPERATION.equals(key)) {
			return operation;
		}
		else if (TOURNAMENT.equals(key)) {
			return tournament;
		}
		else if (SET.equals(key)) {
			return set;
		}
		else if (MONTH.equals(key)) {
			return month;
		}
		else if (YEAR.equals(key)) {
			return year;
		}
		else if (M44CODE.equals(key)) {
			return m44code;
		}
		else if (DOWID.equals(key)) {
			return dowid;
		}
		else if (OFFICIAL.equals(key)) {
			return String.valueOf(official);
		}
		else if (AUTHOR.equals(key)) {
			return author;
		}
		else if (CLASSIFIED.equals(key)) {
			return String.valueOf(classified);
		}
		else {
			return null;
		}
	}

	public void setAttribute(String key, Object value) {
		if (NAME.equals(key)) {
			setConfigureName((String) value);
		}
		else if (FILE.equals(key)) {
			if (value instanceof File) {
				value = ((File) value).getName();
			}
			fileName = (String) value;
		}
		else if (TYPE.equals(key)) {
			if (value instanceof String)
				scentype = (String)value;
		}
		else if (FRONT.equals(key)) {
			if (value instanceof String)
				front = (String)value;
		}
		else if (OPERATION.equals(key)) {
			if (value instanceof String)
				operation = (String)value;
		}
		else if (TOURNAMENT.equals(key)) {
			if (value instanceof String)
				tournament = (String)value;
		}
		else if (SET.equals(key)) {
			if (value instanceof String)
				set = (String)value;
		}
		else if (MONTH.equals(key)) {
			if (value instanceof String)
				month = (String)value;
		}
		else if (YEAR.equals(key)) {
			if (value instanceof String)
				year = (String)value;
		}
		else if (M44CODE.equals(key)) {
			if (value instanceof String)
				m44code = (String)value;
		}
		else if (DOWID.equals(key)) {
			if (value instanceof String)
				dowid = (String)value;
		}
		else if (OFFICIAL.equals(key)) {
			if (value instanceof String) {
				official = Boolean.valueOf((String) value);
			}
		}
		else if (AUTHOR.equals(key)) {
			if (value instanceof String)
				author = (String)value;
		}
		else if (CLASSIFIED.equals(key)) {
			if (value instanceof String) {
				classified = Boolean.valueOf((String) value);
			}
		}
	}
	
	public String getDate() {
		String date = "";
		
		if ((month == null) && (year == null))
			return date;
		
		if (month != null)
			return month+" '"+year;
		else
			return "'"+year;
	}

	public VisibilityCondition getAttributeVisibility(String name) {
		return super.getAttributeVisibility(name);
	}

	public boolean launch()
	{
		GameState gameState = GameModule.getGameModule().getGameState();
		
		if (classified == true)
		{
			try {
				gameState.loadGameInBackground(getConfigureName(), getSavedGameContents(getFileName(true)));
			}
			catch (Exception e0)
			{				
				try
				{
					gameState.loadGameInBackground(getConfigureName(), getSavedGameContents(getFileName(false)));
				}
				catch (IOException e) {
					ErrorDialog.dataWarning(new BadDataReport(this, Resources.getString("Error.not_found", "Setup"),getFileName(),e)); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}
		}
		else
		{
			try {
				gameState.loadGameInBackground(getConfigureName(), getSavedGameContents(getFileName()));
			}
			catch (IOException e) {
				ErrorDialog.dataWarning(new BadDataReport(this, Resources.getString("Error.not_found", "Setup"),getFileName(),e)); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}

		return true;
	}

	public InputStream getSavedGameContents(String fileName) throws IOException {
		return GameModule.getGameModule().getDataArchive().getInputStream(fileName);
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
		return new Class<?>[]{M44Setup.class};
	}

	public static String getConfigureTypeName() {
		return "Memoir '44 Scenario";
	}

	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("GameModule.htm", "M44Setup"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getFileName() {
		return fileName + ".vsav";
	}
	
	public String getFileName(Boolean classified) {
		if (!classified)
		{
			return getFileName();
		}
		else
		{
			return fileName + "_c.vsav";
		}
	}

	public Command getRestoreCommand() {
		return null;
	}

	public void setup(boolean gameStarting) {
		launchAction.setEnabled(!gameStarting);
	}
}
