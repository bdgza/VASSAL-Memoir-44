package com.memoir44.vassal;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.ChartWindow;
import VASSAL.tools.KeyStrokeSource;

public class M44CampaignChartWindow extends ChartWindow {
	public void addTo(Buildable b) {
		launch.setName(name);
		
		super.addTo(b);
	  }
}
