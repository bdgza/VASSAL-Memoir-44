package com.memoir44.vassal;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.map.boardPicker.Board;

public class M44Board extends Board implements PlayerRoster.SideChangeListener {
	private String playerSide = "<observer>";
	
	public M44Board()
	{
		super();
		
		PlayerRoster.addSideChangeListener(this);
	}
	
	public void sideChanged(String oldSide, String newSide) {
		GameModule.getGameModule().warn("Board Changed Side: " + newSide);
		playerSide = newSide;
		((M44Map)this.map).revalidateMap();
	}
	
	public void draw(Graphics g, int x, int y, double zoom, Component obs) {
		playerSide = PlayerRoster.getMySide();
		
		GameModule.getGameModule().warn("draw() - " + playerSide);
		
		if (playerSide != null)
			this.reversed = (playerSide.indexOf("Axis") != -1);
		
		StackTraceElement[] se = Thread.currentThread().getStackTrace();
		for (int i = 0; i < se.length; i++)
		{
			GameModule.getGameModule().warn(i+". "+se[i].toString());
		}
		
		super.draw(g, x, y, zoom, obs);
	}
}
