package memoir44;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameState;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.LaunchButton;
import VASSAL.tools.NamedKeyStroke;

/**
 * This component places a button into the controls window toolbar.
 * Pressing the button displays a message, plays a sound and/or sends hotkeys */
public class M44ChecksumButton extends AbstractConfigurable implements CommandEncoder {
  public static final String BUTTON_TEXT = "text"; //$NON-NLS-1$
  public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
  public static final String NAME = "name"; //$NON-NLS-1$
  public static final String HOTKEY = "hotkey"; //$NON-NLS-1$
  public static final String ICON = "icon"; //$NON-NLS-1$
  public static final String SILENTCHECK = "silent_check"; //$NON-NLS-1$

  protected LaunchButton launch;
  protected boolean silentCheck = false;
  
  public M44ChecksumButton() {
    ActionListener rollAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  final GameModule mod = GameModule.getGameModule();
		  final GameState state = mod.getGameState();
		  final String side = PlayerRoster.getMySide();
		  
    	  if (state.isGameStarted() && side != null && !side.equals("<observer>"))
    	  {
    		  doActions();
    	  }
      }
    };

    final String description = Resources.getString("Editor.DoAction.component_type"); //$NON-NLS-1$
    launch = new LaunchButton(description, TOOLTIP, BUTTON_TEXT, HOTKEY, ICON, rollAction);
    setAttribute(NAME, description);
    setAttribute(TOOLTIP, description);
    launch.setAttribute(BUTTON_TEXT, description);
  }

  public static String getConfigureTypeName() {
    return Resources.getString("Editor.DoAction.component_type"); //$NON-NLS-1$
  }

  public String[] getAttributeNames() {
    return new String[]{
      NAME,
      BUTTON_TEXT,
      TOOLTIP,
      ICON,
      HOTKEY,
      SILENTCHECK
    };
  }

  public String[] getAttributeDescriptions() {
    return new String[]{
      Resources.getString(Resources.DESCRIPTION),
      Resources.getString(Resources.BUTTON_TEXT),
      Resources.getString(Resources.TOOLTIP_TEXT),
      Resources.getString(Resources.BUTTON_ICON),
      Resources.getString(Resources.HOTKEY_LABEL),
      "Do a silent check"
    };
  }

  public static class IconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, null);
    }
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{
      String.class,
      String.class,
      String.class,
      IconConfig.class,
      NamedKeyStroke.class,
      Boolean.class
    };
  }

  public void addTo(Buildable parent) {
    GameModule.getGameModule().getToolBar().add(getComponent());
    GameModule.getGameModule().addCommandEncoder(this);
  }

  /**
   * The component to be added to the control window toolbar
   */
  protected Component getComponent() {
    return launch;
  }

  public void setAttribute(String key, Object o) {
    if (NAME.equals(key)) {
    	setConfigureName((String) o);
    }
    else if (SILENTCHECK.equals(key))
    {
    	if (o instanceof String) {
			o = Boolean.valueOf((String) o);
    	}
    	silentCheck = ((Boolean) o).booleanValue();
    }
    else {
      launch.setAttribute(key, o);
    }
  }

  public String getAttributeValueString(String key) {
    if (NAME.equals(key)) {
      return getConfigureName();
    }
    else if (SILENTCHECK.equals(key)) {
        return String.valueOf(silentCheck);
    }
    else {
      return launch.getAttributeValueString(key);
    }
  }

  public VisibilityCondition getAttributeVisibility(String name) {
    return null;
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[0];
  }

  public void removeFrom(Buildable b) {
	GameModule.getGameModule().removeCommandEncoder(this);
    GameModule.getGameModule().getToolBar().remove(getComponent());
    GameModule.getGameModule().getToolBar().revalidate();
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("DoActionButton.htm"); //$NON-NLS-1$
  }

  protected void doActions() {
	  final GameModule mod = GameModule.getGameModule();
	  final Command c = new M44ChecksumCommand(M44ChecksumCommand.calculateChecksum(), silentCheck);
	  
	  mod.getServer().sendToOthers(c);
  }
  
  public String getComponentTypeName () {
	  return getConfigureTypeName();
  }

  public String getComponentName() {
	  return getConfigureName();
  }

  /**
   * Implement PropertyNameSource - Expose loop index property if looping turned on
   */
  public List<String> getPropertyNames() {
	  return super.getPropertyNames();
  }

  public Command decode(String command) {
	 final String side = PlayerRoster.getMySide();
	 
	 Command comm = null;
	 if (command.startsWith(M44ChecksumCommand.COMMAND_PREFIX) && side != null && !side.equals("<observer>")) {
		 String[] pieces = command.substring(M44ChecksumCommand.COMMAND_PREFIX.length()).split("\\t");
		 String commside = pieces[pieces.length - 1];
		 
		 if (!commside.equals(side))
		 {
			 comm = new M44ChecksumCommand(command.substring(M44ChecksumCommand.COMMAND_PREFIX.length()));
			// DEBUG
//			 M44ChecksumCommand.WriteLine("decode = " + command, false);
		 }
	 }
	 return comm;
  }
  
  public String encode(Command c) {
	  final String side = PlayerRoster.getMySide();
	  
	  String s = null;
	  if (c instanceof M44ChecksumCommand) {
		  M44ChecksumCommand chk = (M44ChecksumCommand) c;
		  s = M44ChecksumCommand.COMMAND_PREFIX + chk.getChecksumValue() + '\t' + String.valueOf(chk.silentCheck) + '\t' + side;
		// DEBUG
//		  M44ChecksumCommand.WriteLine("encode = " + s, false);
	  }
	  return s;
  }
}