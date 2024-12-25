package memoir44;

import java.io.File;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.StringArrayConfigurer;

public class M44CampaignCollectionItem extends AbstractConfigurable implements GameComponent, Comparable<M44CampaignCollectionItem> {
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String PAGES = "pages"; //$NON-NLS-1$
	
	protected String icon;
	protected String file;
	protected Integer pages;
	
	@Override
	public String[] getAttributeNames() {
		return new String[]{
				NAME,
				ICON,
				FILE,
				PAGES
		};
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (NAME.equals(key)) {
			setConfigureName((String) value);
		}
		else if (ICON.equals(key)) {
			if (value instanceof File) {
				value = ((File) value).getName();
			}
			icon = (String) value;
		}
		else if (FILE.equals(key)) {
//			if (value instanceof String) {
//				value = StringArrayConfigurer.stringToArray((String) value);
//		    }
		    file = (String)value;
		}
		else if (PAGES.equals(key))
		{
			if (value instanceof String)
			{
				pages = Integer.parseInt((String)value);
			}
			else
			{
				pages = (Integer)value;
			}
		}
	}

	@Override
	public String getAttributeValueString(String key) {
		if (NAME.equals(key)) {
			return getConfigureName();
		}
		else if (ICON.equals(key)) {
			return icon;
		}
		else if (FILE.equals(key)) {
//			return StringArrayConfigurer.arrayToString(pages);
			return file;
		}
		else if (PAGES.equals(key))
		{
			return Integer.toString(pages);
		}
		else {
			return null;
		}
	}

	@Override
	public void removeFrom(Buildable parent) {
		GameModule.getGameModule().getGameState().removeGameComponent(this);
	}

	@Override
	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("GameModule.htm", "M44CampaignCollectionItem"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Class[] getAllowableConfigureComponents() {
		return new Class<?>[]{M44CampaignCollectionItem.class};
	}

	@Override
	public void addTo(Buildable parent) {
		GameModule.getGameModule().getGameState().addGameComponent(this);
	}

	@Override
	public String[] getAttributeDescriptions() {
		return new String[]{
				"Campaign Name: ","Campaign Icon: ", "Page files: ", "Page count: "
		};
	}

	@Override
	public Class<?>[] getAttributeTypes() {
		return new Class<?>[]{
				String.class,
				String.class,
				String.class,
				Integer.class
		};
	}

	@Override
	public void setup(boolean gameStarting) {
		
	}

	@Override
	public Command getRestoreCommand() {
		return null;
	}

	@Override
	public int compareTo(M44CampaignCollectionItem o) {
		return this.getAttributeValueString(NAME).compareTo(o.getAttributeValueString(NAME));
	}
}
