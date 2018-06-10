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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.StyleSheet;

import com.memoir44.vassal.M44ScenarioNotes.SetScenarioNote;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.HtmlChart.HtmlChartHyperlinkListener;
import VASSAL.build.widget.HtmlChart.XTMLEditorKit;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.counters.GamePiece;
import VASSAL.i18n.Resources;
import VASSAL.tools.FormattedString;
import VASSAL.tools.LaunchButton;
import VASSAL.tools.ReadErrorDialog;
import VASSAL.tools.ScrollPane;

/**
 * This is a copy of the NotesWindow in VASSAL with less elements in the UI specifically suited to showing Scenario notes for Memoir '44
 */
public class M44ScenarioNotesViewer extends AbstractConfigurable
implements GameComponent, CommandEncoder {

	protected JDialog frame;
	protected LaunchButton launch;
	protected String scenarioNotes;
	protected static final String SCENARIO_NOTE_COMMAND_PREFIX = "NOTES\t"; //$NON-NLS-1$
	protected static final String PUBLIC_NOTE_COMMAND_PREFIX = "PNOTES\t"; //$NON-NLS-1$

	public static final String HOT_KEY = "hotkey"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String BUTTON_TEXT = "buttonText"; //$NON-NLS-1$
	public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$

	protected JEditorPane jEditorPane;
	protected String subtitle = null;
	protected String lastmaintitle = "";
	public ScrollPane scroller;

	protected boolean blnSolexMedium = false;
	protected boolean blnHeadache = false;
	protected boolean blnArmy = false;
	protected boolean blnGunplay = false;

	public M44ScenarioNotesViewer() {
		frame = new M44NotesViewerDialog();
		frame.setTitle("Scenario");
		ActionListener al = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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

	protected static void chat(String msg) {
		final GameModule mod = GameModule.getGameModule();
		FormattedString fStr = new FormattedString(msg);
		final Command c = new Chatter.DisplayText(mod.getChatter(), fStr.getLocalizedText());
		c.execute();
		mod.sendAndLog(c);
	}

	protected class M44NotesViewerDialog extends JDialog {

		private static final long serialVersionUID = 1L;

		protected M44NotesViewerDialog() {
			super(GameModule.getGameModule().getFrame());
			initComponents();
			setLocationRelativeTo(getOwner());
		}
		
		private boolean isURL() {
			return jEditorPane.getDocument().getProperty("stream") != null;
		}

		public class ScenarioHyperlinkListener implements HyperlinkListener {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
					return;
				}

				final String desc = event.getDescription();
				if ((!isURL() && desc.indexOf('/') < 0) || event.getURL() == null) {
					/*final int hash = desc.lastIndexOf("#");
					if (hash < 0) {
						// no anchor
						setFile(desc);
					}
					else if (hash > 0) {
						// browse to the part before the anchor
						setFile(desc.substring(0, hash));
					}

					if (hash != -1) {
						// we have an anchor
						htmlWin.scrollToReference(desc.substring(hash+1));
					}*/
				}
				else {
					/*try {
						jEditorPane.setPage(event.getURL());
					}
					catch (IOException ex) {
						ReadErrorDialog.error(ex, event.getURL().toString());
					}
					htmlWin.revalidate();*/
					
					openWebpage(event.getURL());
				}
			}
		}

		public void openWebpage(URI uri) {
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(uri);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void openWebpage(URL url) {
			try {
				openWebpage(url.toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		protected void initComponents() {
			setLayout(new BorderLayout());

			// create jeditorpane
			jEditorPane = new JEditorPane();

			// make it read-only
			jEditorPane.setEditable(false);

			// create a scrollpane; modify its attributes as desired
			JScrollPane scrollPane = new JScrollPane(jEditorPane);

			// add an html editor kit
			XTMLEditorKit kit = new XTMLEditorKit();
			jEditorPane.setEditorKit(kit);
			
			jEditorPane.addHyperlinkListener(new ScenarioHyperlinkListener());

			// add some styles to the html
			StyleSheet styleSheet = kit.getStyleSheet();
			
			// fonts detection
			GraphicsEnvironment g = null;
			g = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String[] fonts = g.getAvailableFontFamilyNames();
			for (int i = 0; i < fonts.length; i++) {
				//chat("Font: " + fonts[i]);
				
				if (fonts[i].equals("SolexMedium"))
					blnSolexMedium = true;
				if (fonts[i].equals("Headache"))
					blnHeadache = true;
				if (fonts[i].equals("Army"))
					blnArmy = true;
				if (fonts[i].equals("Gunplay"))
					blnGunplay = true;
			}
			
			String bodyFamily = blnSolexMedium ? "SolexMedium, sans-serif" : "Headache, sans-serif";
			String h1Family = blnArmy ? "Army, serif" : "Times new Roman, serif";
			String h3Family = blnGunplay ? "Gunplay, serif" : "Times new Roman, serif";
			
			//chat("F1: " + bodyFamily);
			//chat("F2: " + h1Family);
			//chat("F3: " + h3Family);
			
			styleSheet.addRule("body {color: #000; font-family: " + bodyFamily + "; font-size: 15pt; margin: 4px; background-color: #fdf6cf; background-image: url(\"http://www.vassalengine.org/mediawiki/images/0/0b/M44-scenario-background.png\"); }");
			
			styleSheet.addRule("h1 {margin: 0; margin-bottom: 4px; color: #7f0019; font-family: " + h1Family + "; font-size: 22pt; }");
			styleSheet.addRule("h2 {margin: 0; margin-bottom: 4px; color: #000000; font-family: " + h1Family + "; font-size: 16pt; }");
			
			styleSheet.addRule("h2.subtitle {color: #666666; font-size: 15pt; }");
			styleSheet.addRule("h2.front {color: #7f0019; }");
			styleSheet.addRule("span.compendium {color: #0000ff; text-decoration: underline; }");
			styleSheet.addRule("span.objective {color: #790000; text-decoration: underline; }");
			styleSheet.addRule("span.nation1 {color: #2e3192; font-weight: 600; }");
			styleSheet.addRule("span.nation2 {color: #00a651; font-weight: 600; }");
			
			styleSheet.addRule("a.online {color: #3333ff; text-decoration: underline; font-family: " + h1Family + "; font-size: 14pt; }");
			
			styleSheet.addRule("span.nationtitle1 {color: #2e3192; font-family: " + h3Family + "; font-size: 18pt; }");
			styleSheet.addRule("span.nationtitle2 {color: #00a651; font-family: " + h3Family + "; font-size: 18pt; }");
			styleSheet.addRule("span.cards {color: #000000; font-family: " + h3Family + "; font-size: 32pt; }");
			styleSheet.addRule("h3 {margin: 0; color: #934B22; font-family: " + h3Family + "; font-size: 16pt; }");

			// create some simple html as a string
			String htmlString = "<html>\n"
				+ "<body>\n"
				+ "<h1>Welcome!</h1>\n"
				+ "<h2>This is an H2 header</h2>\n"
				+ "<p>This is some sample text</p>\n"
				+ "</body>\n";

			// create a document, set it on the jeditorpane, then add the html
			Document doc = kit.createDefaultDocument();
			jEditorPane.setDocument(doc);
			jEditorPane.setText(htmlString);

			scroller = new ScrollPane(jEditorPane);
			scroller.getViewport().setPreferredSize(jEditorPane.getPreferredSize());
			scroller.getViewport().setAlignmentY(0.0F);

			add(scroller, BorderLayout.CENTER);

			//scenarioNotes = new M44ScenarioTextConfigurer(null, null, "", true);
			//add(scenarioNotes.getControls(), BorderLayout.CENTER);
		}
	}

	private void setScenarioText(String msg) {
		String html = "";
		String[] notes;
		try {
			if (msg.length() > 0) {
				scenarioNotes = msg.replace("|", "\n");
				notes = scenarioNotes.split("\n");

				final GameModule mod = GameModule.getGameModule();

				//if (subtitle == null || (notes.length > 0 && !notes[0].equals(lastmaintitle))) {
				GameState myGameState = mod.getGameState();

				Collection<GamePiece> pieces = myGameState.getAllPieces();

				Iterator<GamePiece> iterator = pieces.iterator();

				while (iterator.hasNext()) {
					GamePiece p = iterator.next();

					String name = p.getName();

					if (name.startsWith("ScenarioSubtitle"))
					{
						subtitle = name;
					}
				}
				
				if (notes[0].startsWith("<html>"))
				{
					html = scenarioNotes;
				}
				else
				{
					lastmaintitle = notes[0];
					//}
	
					html = "<html>\n<body>\n";
					
					if (!blnSolexMedium && !blnHeadache)
						html += "<table cols=\"2\"><tr><td><img src=\"battle-star-20.png\" /></td><td><h3>Missing font -- for best results please install the \"Solex Medium\" or <a href=\"http://www.fonts2u.com/headache-normal.font\">\"Headache Normal\"</a> font.</h3></td></tr></table>";
					if (!blnArmy)
						html += "<table cols=\"2\"><tr><td><img src=\"battle-star-20.png\" /></td><td><h3>Missing font -- for best results please install the <a href=\"http://www.fonts4free.net/army-font.html\">\"Army\"</a> font.</h3></td></tr></table>";
					if (!blnGunplay)
						html += "<table cols=\"2\"><tr><td><img src=\"battle-star-20.png\" /></td><td><h3>Missing font -- for best results please install the <a href=\"http://www.fonts4free.net/gunplay-font.html\">\"Gunplay\"</a> font.</h3></td></tr></table>";
	
					if (!blnSolexMedium || !blnArmy || !blnGunplay)
						html += "<hr>";
					
					int iter = 0;
					String front = "";
					String dowid = "";
	
					boolean multiMap = false;
	
					do
					{
						multiMap = false;
	
						if (notes.length >= 1) {
							
							String maintitle = notes[iter++];
							String maintitle2 = "";
							if (notes.length >= 2)
								maintitle2 = notes[iter++];
							if (maintitle2.length() > 0)
								iter++;
							iter++;
	
							String date = maintitle.replaceAll(".*? \\- ((January|February|March|April|May|June|July|August|September|October|November|December).*)", "$1");
							String title = maintitle;
							if (date.equals(title))
								date = "";
							if (date.length() > 0)
								title = title.substring(0, title.length() - date.length() - 3);
	
							String operation = "";
							if (title.endsWith(")")) {
								operation = nthfield(title, " \\(", 2);
								operation = operation.substring(0, operation.length() - 1);
								if (title.length() - operation.length() - 3 > 0)
									title = title.substring(0, title.length() - operation.length() - 3);
							}
							
							if (subtitle != null) {
								String stitle = nthfield(subtitle, "\\(", 2);
								stitle = stitle.substring(0, stitle.length() - 1);
	
								if (stitle.toLowerCase().startsWith("west")) {
									front = "Western";
								} else if (stitle.toLowerCase().startsWith("east")) {
									front = "Eastern";
								} else if (stitle.toLowerCase().startsWith("med")) {
									front = "Mediterranean";
								} else if (stitle.toLowerCase().startsWith("pac")) {
									front = "Pacific";
								}
	
								int id = subtitle.lastIndexOf("#");
								if (id > -1 && (id+1) < subtitle.length()) {
									dowid = subtitle.substring(id + 1);
									dowid = dowid.substring(0, dowid.length() - 1);
								}
							}
	
							html += "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" valign=\"bottom\"><td><h1>" + title + "</h1></td>\n";
							html += "<td align=\"right\"><h2>" + date + "</h2></td></table>\n";
							if (front.length() > 0 || operation.length() > 0 || maintitle2.length() > 0) {
								html += "<table cellspacing=\"0\" cellpadding=\"0\" valign=\"bottom\">";
								if (front.length() > 0) {
									html += "<td><h2 class=\"front\">" + front + "&nbsp;&nbsp;</h2></td>\n";
								}
								if (operation.length() > 0) {
									html += "<td><h2>" + operation + "&nbsp;&nbsp;</h2></td>\n";
								}
								if (maintitle2.length() > 0) {
									html += "<td><h2 class=\"subtitle\">" + maintitle2 + "</h2></td>\n";
								}
								html += "</table>\n";
							}
						} else {
							iter = 2;
						}
	
						html += "<br>";
						html += "<h3>Historical Background</h3>";
	
						int blank = 0;
	
						while (iter < notes.length && blank < 2) {
							if (notes[iter].length() == 0)
								blank++;
							else
								blank = 0;
	
							if (blank < 2)
								html += notes[iter] + "<br>";
	
							iter++;
						}
	
						html += "<h3>Briefing</h3>";
	
						try {
							String briefing = "<table width=\"100%\" cols=\"4\" border=\"1\">";
							briefing += "<tr valign=\"top\">";
							
							int local = iter;
							local++;
							List<String> briefList = new ArrayList<String>();
							
							blank = 0;
	
							while (local < notes.length && blank < 2) {
								if (notes[local].length() == 0)
									blank++;
								else
									blank = 0;
	
								if (blank < 2)
									briefList.add(notes[local]);
	
								local++;
							}
							
							String[] brief = briefList.toArray(new String[briefList.size()]);
							
							int i = 0;
							
							for (int start = 1; start <= 2; start++) {
								String nation = "";
								String flag = "";
								String medal = "";
								switch (start) {
								case 1:
									nation = "Axis";
									flag = "germany";
									medal = "medal-axis";
									break;
								case 2:
									nation = "Allies";
									flag = "us";
									medal = "medal-allies";
								}
								if (brief[i].contains("]:")) {
									nation = nthfield(nthfield(brief[i], "\\[", 2), "\\]: ", 1);
								}
								brief[i] = nthfield(brief[i], ": ", 2);
	
								String cards = brief[i].replaceAll("Take ([0-9]{1,2}).*", "$1");
								int period = brief[i].indexOf(".");
								if (period == -1 || (period+1) == brief[i].length()) {
									brief[i] = "";
								} else {
									brief[i] = brief[i].substring(period+1).trim();
								}
								
								String cc = "";
								
								if (brief[i].length() > 0)
								{
									cc = brief[i].replaceAll("Take ([0-9]{1,2}) (\\w+) Combat card.*", "$1/$2");
									period = brief[i].indexOf(".");
									if (!cc.equals(brief[i]))
									{
										cc = cc.toLowerCase();
										if (period == -1 || (period+1) == brief[i].length()) {
											brief[i] = "";
										} else {
											brief[i] = brief[i].substring(period+1).trim();
										}
									}
								}
	
								if (brief[i].length() == 0)
									i++;
								
								String newFlag = flagForNation(nation);
								if (newFlag.length() > 0)
									flag = newFlag;
								
								// find victory medals
								int j = iter + 1;
								blank = 0;
								
								while (j < notes.length && blank < 2) {
									if (notes[j].length() == 0)
										blank++;
									else
										blank = 0;
									j++;
								}
								j++;
								
								String medals = "";
								String newMedal = "";
															
								if (notes[j].startsWith("Axis: ")) {
									if (start == 1) {
										//if (notes[j].trim().endsWith(" medal.") || notes[j].trim().endsWith(" medals.") ||
										//		notes[j].trim().endsWith(" Medal.") || notes[j].trim().endsWith(" Medals."))
										//{
											medals = notes[j].replaceAll("Axis: ([0-9\\*]{1,3}).*", "$1");
										//}
									} else {
										j++;
										int limit = 0;
										while (!notes[j].startsWith("Allies: ") && limit < 10) {
											limit++;
											j++;
										}
										if (notes[j].startsWith("Allies: ")) {
										//j++;
										//if (notes[j].trim().endsWith(" medal.") || notes[j].trim().endsWith(" medals.") ||
										//		notes[j].trim().endsWith(" Medal.") || notes[j].trim().endsWith(" Medals."))
										//{
											medals = notes[j].replaceAll("Allies: ([0-9\\*]{1,3}).*", "$1");
										}
									}
								} else {
									if (notes[j].trim().endsWith(" medal.") || notes[j].trim().endsWith(" medals.") ||
											notes[j].trim().endsWith(" Medal.") || notes[j].trim().endsWith(" Medals."))
									{
										medals = notes[j].replaceAll("([0-9\\*]{1,3}).*", "$1");
									}
								}
								
								//if (brief[i].endsWith(" medal.") || brief[i].endsWith(" medals.")) {
								if (medals.length() > 0) {
									//medals = brief[i].replaceAll("([0-9]{1,2}).*?\\.", "$1");
									newMedal = medalForFlag(newFlag);
									if (newMedal.length() > 0)
										medal = newMedal;
									//i++;
								}
	
								briefing += "<td valign=\"top\" width=\"120\">";
								briefing += "<img src=\"flag-"+flag+".png\" /></td>";
								briefing += "<td width=\"260\"><span class=\"nationtitle"+start+"\">"+nation.replace("/", ", ")+"</span><br>";
								briefing += "<table border=\"0\" valign=\"top\">";
								briefing += "<tr valign=\"top\"><td>";
								briefing += "<img src=\"mm_preview_command_card.jpg\" />";
								briefing += "</td><td><span class=\"cards\">"+cards+"</span></td>";
								if (cc.length() > 0)
								{
									String[] ccparts = cc.split("/");
									if (ccparts.length > 1)
									{
										String ccimage = "UC-tiny";
										if (ccparts[1].equals("urban"))
											ccimage = "UC-tiny";
										else if (ccparts[1].equals("winter"))
											ccimage = "WC-tiny";
										else if (ccparts[1].equals("jungle"))
											ccimage = "JC-tiny";
										else if (ccparts[1].equals("desert"))
											ccimage = "DC-tiny";
										
										briefing += "<td width=\"20\">&nbsp;</td>";
										briefing += "<td><img src=\"" + ccimage + ".png\" /></td>";
										briefing += "<td><span class=\"cards\">" + ccparts[0] + "</span></td>";
									}
								}
								if (medals.length() > 0) {
									briefing += "<td width=\"20\">&nbsp;</td>";
									briefing += "<td><img src=\"" + medal + ".png\" /></td>";
									briefing += "<td><span class=\"cards\">" + medals + "</span></td>";
								}
								briefing += "</tr>";
								briefing += "</table>";
								
								blank = 0;
	
								while (i < brief.length && blank == 0) {
									if (brief[i].length() == 0) {
										blank++;
									} else {
										if (brief[i].startsWith("You move first.")) {
											briefing += "<table border=\"0\" valign=\"middle\"><tr valign=\"middle\"><td>";
											briefing += "<img src=\"mm_preview_first.png\" />";
											briefing += "</td><td>"+brief[i]+"</td></tr></table>";
										} else {
											briefing += brief[i] + "<br>";
										}
									}
									i++;
								}
								briefing += "</td>";
							}
													
							briefing += "</tr></table><br>";
							
							iter = local;
							html += briefing;
							
						} catch (Exception ex) {
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							ex.printStackTrace(pw);
							
							chat("ERROR: Problem parsing briefing. Please report. " + ex.getMessage() + "\n" + sw.toString() + "\n\n" + html);
							
							iter++;
							blank = 0;
	
							while (iter < notes.length && blank < 2) {
								if (notes[iter].length() == 0)
									blank++;
								else
									blank = 0;
	
								if (blank < 2)
									html += filterCompendium(notes[iter]) + "<br>";
	
								iter++;
							}
						}
						
						html += "<h3>Conditions of Victory</h3>";
	
						iter++;
						blank = 0;
	
						while (iter < notes.length && blank < 2) {
							if (notes[iter].length() == 0)
								blank++;
							else
								blank = 0;
	
							if (blank < 2)
								html += filterCompendium(notes[iter]) + "<br>";
	
							iter++;
						}
						
						html += "<h3>Special Rules</h3>";
						
						iter++;
						blank = 0;
	
						while (iter < notes.length && !multiMap && blank < 2) {
							if (notes[iter].length() > 0 && notes[iter].charAt(0) == '#') {
								
							} else if (notes[iter].equals("==========***==========")) {
								multiMap = true;
								iter++;
							} else {
								if (notes[iter].length() == 0)
								{
									blank++;
								} else {
									blank = 0;
								}
								if (blank < 2)
									html += filterCompendium(notes[iter]) + "<br>";
							}
							iter++;
						}
	
						if (iter < notes.length)
						{
							html += "<h3>Notes</h3>";
						
							iter++;
		
							while (iter < notes.length && !multiMap) {
								if (notes[iter].length() > 0 && notes[iter].charAt(0) == '#') {
									
								} else if (notes[iter].equals("==========***=========="))
								{
									multiMap = true;
									iter++;
								} else {
									html += filterCompendium(notes[iter]) + "<br>";
								}
								iter++;
							}
						}
	
						if (dowid.length() > 0) {
							//html += "<a class=\"online\" href=\"http://www.daysofwonder.com/memoir44/en/editor/view/?id=" + dowid + "\">View Online</a><br>";
							//html += subtitle + "<br>";
							html += "<br><table width=\"100%\" align=\"center\"><td align=\"center\"><a href=\"http://www.daysofwonder.com/memoir44/en/editor/view/?id=" + dowid + "\"><img src=\"m44-www.png\" /></a></td></table>\n";
						}
	
						if (multiMap) {
							html += "<hr><br>";
						}
	
					} while (multiMap);
	
					//TODO: use subtitle for DoW ID and Front: html += "<br>" + subtitle + "<br>";
					html += "</body>\n</html>\n";
				}
			}

			jEditorPane.setText(html);

			jEditorPane.setSelectionStart(0);
			jEditorPane.setSelectionEnd(0);

			Rectangle visible = jEditorPane.getVisibleRect();
			visible.y = 0;
			scroller.scrollRectToVisible(visible);
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			
			chat("Error parsing the scenario description into HTML, this might be a faulty scenario setup file. Please report.\n" + ex.getMessage() + "\n" + sw.toString() + "\n\n" + html);
		}
	}
	
	protected String flagForNation(String nation) {
		nation = nation.toLowerCase();
		nation = nthfield(nation, "/", 1);
		
		if (nation.equals("britain") || nation.equals("united kingdom") || nation.equals("great britain") || nation.equals("british") || nation.equals("commonwealth"))
			return "uk";
		if (nation.equals("soviet union") || nation.equals("russia") || nation.equals("russians"))
			return "russia";
		if (nation.equals("japan") || nation.equals("japanese imperial army") || nation.equals("japanese kwantung army"))
			return "japan";
		if (nation.equals("germans"))
			return "germany";
		if (nation.equals("americans"))
			return "us";
		if (nation.equals("usmc") || nation.equals("united states marine corps") || nation.equals("us marine corps") || nation.equals("us marines") || nation.equals("united states marines"))
			return "usmc";
		if (nation.equals("france - free forces") || nation.equals("french - resistance") || nation.equals("france - resistance") || nation.equals("france - sas") || nation.equals("french resistance") || nation.equals("free french") || nation.equals("free france"))
			return "freefrench";
		if (nation.equals("vichy france") || nation.equals("france-vichy"))
			return "vichyfrench";
		if (nation.equals("netherlands") || nation.equals("dutch"))
			return "netherlands";
		if (nation.equals("china") || nation.equals("chinese nationalist"))
			return "china";
		if (nation.equals("france") || nation.equals("french"))
			return "france";
		if (nation.equals("scotland"))
			return "scotland";
		if (nation.equals("india"))
			return "india";
		if (nation.equals("south africa"))
			return "southafrica";
		if (nation.equals("yugoslavia") || nation.equals("yugoslav resistance") || nation.equals("yugoslav partisans"))
			return "yugoslavia";
		
		if (nation.equals("nationalist spain")) return "spainrep";
		if (nation.equals("republican spain")) return "spainnat";
		if (nation.equals("new zealand")) return "newzealand";
		if (nation.equals("independent state of croatia") || nation.equals("ndh")) return "croatiaaxis";
		
		if ( (nation.equals("canada")) || (nation.equals("finland"))
				|| (nation.equals("slovakia")) || (nation.equals("italy")) || (nation.equals("poland")) || (nation.equals("belgium"))
				|| (nation.equals("romania")) || (nation.equals("vichy france")) || (nation.equals("hungary"))
				|| (nation.equals("australia")) || (nation.equals("albania")) || (nation.equals("norway")) || (nation.equals("greece"))
				|| (nation.equals("thailand")) || (nation.equals("croatia")) || (nation.equals("philippines")) || (nation.equals("bulgaria"))
				|| (nation.equals("southafrica"))
				) return nation;

		return "";
	}
	
	protected String medalForFlag(String flag) {
		if (flag.equals("uk") || flag.equals("canada") || flag.equals("australia") || flag.equals("newzealand")) {
			return "medal-british";
		} else if (flag.equals("russia")) {
			return "medal-russian";
		} else if (flag.equals("japan")) {
			return "medal-japan";
		} else if (flag.equals("italy")) {
			return "medal-italy";
		}
		
		return "";
	}

	protected String filterCompendium(String input) {
		input = input.replaceAll("\\((Actions .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Troops .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Nations .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Terrain .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((SWAs .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Airplanes .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Air Rules .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		
		input = input.replaceAll("(?!>)(Temporary Majority Medal Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Majority Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Medal Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Permanent Medal Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Permanent Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Permanent Majority Medal Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Permanent Majority Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Sole Control, Last to Occupy Medal Objective[s]? \\\\(Turn Start\\\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Sole Control, Last to Occupy Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Sole Control Medal Objective[s]? \\\\(Turn Start\\\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Temporary Sole Control Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Last to Occupy Medal Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Last to Occupy Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Sole Control Medal Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Sole Control Medal Objective[s]?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Sudden Death Objective Exit( hex)?(es)?)", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Sudden Death Objective[s]? \\(Turn Start\\))", "<span class=\"objective\">$1</span>");
		input = input.replaceAll("(?!>)(Sudden Death Objective[s]?)", "<span class=\"objective\">$1</span>");
		
		input = input.replaceAll("Axis [pP]layer \\[(.*?)\\]", "Axis Player \\[<span class=\"nation1\">$1</span>\\]");
		input = input.replaceAll("Allied [pP]layer \\[(.*?)\\]", "Allied Player \\[<span class=\"nation2\">$1</span>\\]");
		
		return input;
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
		if (c instanceof SetScenarioNote) {
			setScenarioText(((SetScenarioNote) c).msg);
		}

		return null;
	}

	private String nthfield(String str, String delim, int field) {
		if (!str.matches(".*" + delim + ".*"))
			return str;
		String[] f = str.split(delim);
		if (f.length < field) {
			return "";
		} else {
			return f[field-1];
		}
	}

	public Command decode(String command) {
		Command comm = null;
		if (command.startsWith(SCENARIO_NOTE_COMMAND_PREFIX)) {
			setScenarioText(command.substring(SCENARIO_NOTE_COMMAND_PREFIX.length()));
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
			setScenarioText("");
		}
	}

	public Command getRestoreCommand() {
		return null;
	}
}
