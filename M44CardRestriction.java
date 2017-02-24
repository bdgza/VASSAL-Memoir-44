/*
 * $Id: Restricted.java 5891 2009-08-02 13:09:39Z swampwallaby $
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
/*
 * Created by IntelliJ IDEA.
 * User: rkinney
 * Date: Jun 13, 2002
 * Time: 9:52:40 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.memoir44.vassal;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.PlayerRoster.PlayerInfo;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.StringArrayConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.PieceVisitor;
import VASSAL.counters.PieceVisitorDispatcher;
import VASSAL.counters.Properties;
import VASSAL.counters.Stack;
import VASSAL.launch.Player;
import VASSAL.tools.SequenceEncoder;

/**
 * A GamePiece with the Restricted trait can only be manipulated by the player playing a specific side
 */
public class M44CardRestriction extends Decorator implements EditablePiece {
	public static final String ID = "restrictM44;";

	public M44CardRestriction() {
		this(ID, null);
	}

	public M44CardRestriction(String type, GamePiece p) {
		setInner(p);
		mySetType(type);
	}

	public String getDescription() {
		return "M44 Restricted Card Access";
	}

	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("RestrictedAccess.htm");
	}

	public void mySetType(String type) {
		type = type.substring(ID.length());
		SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type,';');
	}

	public Shape getShape() {
		return piece.getShape();
	}

	public Rectangle boundingBox() {
		return piece.boundingBox();
	}

	public void draw(Graphics g, int x, int y, Component obs, double zoom) {
		piece.draw(g, x, y, obs, zoom);
	}

	public String getName() {
		return piece.getName();
	}

	protected KeyCommand[] myGetKeyCommands() {
		return new KeyCommand[0];
	}

	public boolean isRestricted() {
		boolean restricted = false;

		String gameMap = "";

		if (getMap() != null)
			gameMap = getMap().getMapName();

		//if (owningPlayer.length() == 0)
		//	owningPlayer = GlobalOptions.getInstance().getPlayerId();

		//if (gameMap.equals("Axis") || gameMap.equals("Allies")) {
		if (gameMap.length() > 0) {
			String ownervalue = piece.getProperty("OwnerValue").toString();
			if (ownervalue.length() <= 1)
				ownervalue = GlobalOptions.getInstance().getPlayerId();

			String hidden = piece.getProperty("ObscuredToOthers").toString();

			if (hidden.equals("true") && !ownervalue.equals(GlobalOptions.getInstance().getPlayerId()))
				//if (hidden.equals("true") && !owningPlayer.equals(GlobalOptions.getInstance().getPlayerId()))
				restricted = true;
			//if (hidden.equals("true") && (!ownervalue.equals(PlayerRoster.getMySide())))

			//Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(),"RESTRICT: 1,"+ownervalue+" // 2,"+hidden+" // 3,"+owningPlayer+" // 4,"+GameModule.getUserId()+" // 5,"+PlayerRoster.getMySide()+" // 6,"+
			//		GlobalOptions.getInstance().getPlayerId() + " // h," + hidden + " // r," + restricted);
			//c.execute();
			//GameModule.getGameModule().sendAndLog(c);
		}

		return restricted;
	}

	/*  @Override
  public void setMap(Map m) {
    if (m != null && restrictByPlayer && owningPlayer.length() == 0) {
      owningPlayer = GameModule.getUserId();
    }
    super.setMap(m);
  }
	 */  
	@Override
	public void setProperty(Object key, Object val) {
		super.setProperty(key, val);
	}

	protected KeyCommand[] getKeyCommands() {
		if (!isRestricted()) {
			return super.getKeyCommands();
		}
		else {
			return new KeyCommand[0];
		}
	}

	@Override
	public Object getLocalizedProperty(Object key) {
		if (Properties.RESTRICTED.equals(key)) {
			return Boolean.valueOf(isRestricted());
		}
		else {
			return super.getLocalizedProperty(key);
		}    
	}

	public Object getProperty(Object key) {
		if (Properties.RESTRICTED.equals(key)) {
			return Boolean.valueOf(isRestricted());
		}
		else {
			return super.getProperty(key);
		}
	}

	public String myGetState() {
		return "";
	}

	public String myGetType() {
		return ID + new SequenceEncoder(';');
	}

	public Command myKeyEvent(KeyStroke stroke) {
		return null;
	}

	public Command keyEvent(KeyStroke stroke) {
		if (!isRestricted()) {
			return super.keyEvent(stroke);
		}
		else {
			return null;
		}
	}

	public void mySetState(String newState) {

	}

	public PieceEditor getEditor() {
		return new Ed(this);
	}

	public static class Ed implements PieceEditor {
		private Box box;

		public Ed(M44CardRestriction r) {

		}

		public Component getControls() {
			box = Box.createVerticalBox();
			return box;
		}

		public String getState() {
			return "";
		}

		public String getType() {
			return ID + new SequenceEncoder(';');
		}
	}

}
