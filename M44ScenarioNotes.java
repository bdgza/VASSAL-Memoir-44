/*
 * $Id: NotesWindow.java 7725 2011-07-31 18:51:43Z uckelman $
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package com.memoir44.vassal;

//import VASSAL.build.AbstractConfigurable;
//import VASSAL.build.AutoConfigurable;
//import VASSAL.build.Buildable;
//import VASSAL.build.Configurable;
//import VASSAL.build.GameModule;
//import VASSAL.build.module.documentation.HelpFile;
//import VASSAL.command.Command;
//import VASSAL.command.CommandEncoder;
//import VASSAL.command.NullCommand;
//import VASSAL.configure.Configurer;
//import VASSAL.configure.ConfigurerFactory;
//import VASSAL.configure.IconConfigurer;
//import VASSAL.i18n.Resources;
//import VASSAL.tools.LaunchButton;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.command.NullCommand;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.tools.LaunchButton;

/**
 * This is a copy of the NotesWindow in VASSAL with less elements in the UI specifically suited to showing Scenario notes for Memoir '44
 */
public class M44ScenarioNotes extends AbstractConfigurable
    implements GameComponent, CommandEncoder {

  protected JDialog frame;
  protected LaunchButton launch;
  protected M44ScenarioTextConfigurer scenarioNotes;
  protected static final String SCENARIO_NOTE_COMMAND_PREFIX = "NOTES\t"; //$NON-NLS-1$
  protected static final String PUBLIC_NOTE_COMMAND_PREFIX = "PNOTES\t"; //$NON-NLS-1$

  public static final String HOT_KEY = "hotkey"; //$NON-NLS-1$
  public static final String ICON = "icon"; //$NON-NLS-1$
  public static final String BUTTON_TEXT = "buttonText"; //$NON-NLS-1$
  public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$

  protected String lastSavedScenarioNotes;

  public M44ScenarioNotes() {
    frame = new M44NotesDialog();
    frame.setTitle("Scenario");
    ActionListener al = new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        captureState();
        frame.setVisible(!frame.isShowing());
      }
    };
    launch = new LaunchButton(Resources.getString("Notes.notes"), TOOLTIP, BUTTON_TEXT, HOT_KEY, ICON, al); //$NON-NLS-1$
    launch.setAttribute(ICON, "/images/notes.gif"); //$NON-NLS-1$
    launch.setToolTipText(Resources.getString("Notes.notes")); //$NON-NLS-1$
    frame.pack();
    setup(false);
    
    int x = frame.getX();
    int y = frame.getY();
    x = x + ((frame.getWidth() - 700) / 2);
    y = y + ((frame.getHeight() - 650) / 2);
    if (x < 15)
    	x = 15;
    if (y < 15)
    	y = 15;
    frame.setSize(700, 650);
    frame.setLocation(x, y);
  }

  /**
   * Capture this object's state, to be restored if the user hits "Cancel"
   */
  protected void captureState() {
    lastSavedScenarioNotes = (String) scenarioNotes.getValue();
  }

  public void cancel() {
    restoreState();
  }

  protected void restoreState() {
    scenarioNotes.setValue(lastSavedScenarioNotes);
  }

  protected void save() {
    Command c = new NullCommand();
    if (!lastSavedScenarioNotes.equals(scenarioNotes.getValue())) {
      c.append(new SetScenarioNote(scenarioNotes.getValueString()));
      
      GameModule.getGameModule().sendAndLog(c);
      
      captureState();
    }
  }

  protected class M44NotesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    protected M44NotesDialog() {
      super(GameModule.getGameModule().getPlayerWindow());
      initComponents();
      setLocationRelativeTo(getOwner());
    }

    protected void initComponents() {
      setLayout(new BorderLayout());
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          cancel();
          setVisible(false);
        }
      });

      scenarioNotes = new M44ScenarioTextConfigurer(null, null, "", true);
      add(scenarioNotes.getControls(), BorderLayout.CENTER);

      JPanel p = new JPanel();
      JButton saveButton = new JButton("Apply");
      p.add(saveButton);
      saveButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          save();
        }
      });
      JButton cancelButton = new JButton(Resources.getString(Resources.CLOSE));
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancel();
          setVisible(false);
        }
      });
      p.add(cancelButton);
      add(p, BorderLayout.SOUTH);
    }
  }


  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("GameModule.htm", "NotesWindow"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public String[] getAttributeNames() {
    return new String[] {BUTTON_TEXT, TOOLTIP, ICON, HOT_KEY};
  }

  public void setAttribute(String name, Object value) {
    launch.setAttribute(name, value);
  }

  public String getAttributeValueString(String name) {
    return launch.getAttributeValueString(name);
  }

  public String encode(Command c) {
    String s = null;
    if (c instanceof SetScenarioNote) {
      s = SCENARIO_NOTE_COMMAND_PREFIX + ((SetScenarioNote) c).msg;
    }
    return s;
  }

  public Command decode(String command) {
    Command comm = null;
    if (command.startsWith(SCENARIO_NOTE_COMMAND_PREFIX)) {
      comm = new SetScenarioNote(command.substring(SCENARIO_NOTE_COMMAND_PREFIX.length()));
    }
    return comm;
  }

  public String[] getAttributeDescriptions() {
    return new String[] {
        Resources.getString(Resources.BUTTON_TEXT),
          Resources.getString(Resources.TOOLTIP_TEXT),
            Resources.getString(Resources.BUTTON_ICON)/*,
            Resources.getString(Resources.HOTKEY_LABEL)*/
    };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[] {
      String.class,
      String.class,
      IconConfig.class/*,
      NamedKeyStroke.class*/
    };
  }

  public static class IconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, ((M44ScenarioNotes) c).launch.getAttributeValueString(ICON));
    }
  }

  public Configurable[] getConfigureComponents() {
    return new Configurable[0];
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[0];
  }

  public static String getConfigureTypeName() {
    return Resources.getString("Editor.NotesWindow.component_type"); //$NON-NLS-1$
  }

  /**
   * Expects to be added to a {@link VASSAL.build.GameModule}.  Adds a button to
   * the controls window toolbar to show the window containing the
   * notes */
  public void addTo(Buildable b) {
    GameModule.getGameModule().getToolBar().add(launch);
    launch.setAlignmentY(0.0F);
    GameModule.getGameModule().addCommandEncoder(this);
    GameModule.getGameModule().getGameState().addGameComponent(this);
  }

  public void removeFrom(Buildable b) {
    GameModule.getGameModule().getToolBar().remove(launch);
    GameModule.getGameModule().removeCommandEncoder(this);
    GameModule.getGameModule().getGameState().removeGameComponent(this);
  }

  public void setup(boolean show) {
    launch.setEnabled(show);
    if (!show) {
      scenarioNotes.setValue(""); //$NON-NLS-1$
    }
  }

  public Command getRestoreCommand() {
    Command c = new SetScenarioNote(scenarioNotes.getValueString());
    return c;
  }

  protected class SetScenarioNote extends Command {
    protected String msg;

    protected SetScenarioNote(String s) {
      msg = s;
    }

    protected void executeCommand() {
      scenarioNotes.setValue(msg);
    }

    protected Command myUndoCommand() {
      return null;
    }
  }
}
