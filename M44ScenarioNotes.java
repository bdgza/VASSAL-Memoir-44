/*
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package memoir44;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.lang3.ArrayUtils;

import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.command.NullCommand;
import VASSAL.i18n.Resources;
import VASSAL.tools.DataArchive;
import VASSAL.tools.KeyStrokeSource;
import VASSAL.tools.ScrollPane;
import VASSAL.tools.swing.DataArchiveHTMLEditorKit;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.text.Document;
import VASSAL.build.module.GameState;
import VASSAL.counters.GamePiece;

/**
 * This is a copy of the NotesWindow in VASSAL adapted specifically for showing Scenario notes in Memoir '44
 */
public class M44ScenarioNotes extends M44AbstractToolbarItem
		implements GameComponent, CommandEncoder {

    public static final String VIEWER = "viewer"; //$NON-NLS-1$

    protected Boolean isHtmlViewer = Boolean.FALSE;

	public static final String BUTTON_TEXT = "buttonText"; //NON-NLS // non-standard legacy difference from AbstractToolbarItem

	protected JDialog frame;
	public String value;

	protected boolean hasInitialized = false;
	protected M44ScenarioTextConfigurer scenarioNotes;
	protected String lastSavedScenarioNotes;

	protected static final String SCENARIO_NOTE_COMMAND_PREFIX = "NOTES\t"; //$NON-NLS-1$

	protected JEditorPane jEditorPane;
	protected String subtitle = null;
    protected String lastmaintitle = "";
    protected ScrollPane scroller;

    protected boolean blnSolexMedium = false;
    protected boolean blnHeadache = false;
    protected boolean blnArmy = false;
    protected boolean blnGunplay = false;

	public M44ScenarioNotes() {
		frame = new M44ScenarioNotesDialog();
		frame.setTitle("Scenario");
		setNameKey("");								// No description or name configured
		setButtonTextKey(BUTTON_TEXT); // Legacy different button text key

		setLaunchButton(makeLaunchButton(
			Resources.getString("Notes.notes"),
			Resources.getString("Notes.notes"),
			"/images/notes.gif", //NON-NLS
			e -> {
				captureState();
				frame.setVisible(!frame.isShowing());
			}
		));
		launch = getLaunchButton();

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
	 * Capture this object's state, to be restored if the user hits "Close"
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
		final Command c = new NullCommand();
		if (!lastSavedScenarioNotes.equals(scenarioNotes.getValue())) {
			c.append(new SetScenarioNote(scenarioNotes.getValueString()));
			
			GameModule.getGameModule().sendAndLog(c);
		
			captureState();
		}
	}
	
	@Override
	public String[] getAttributeNames() {
		return ArrayUtils.addAll(super.getAttributeNames(),
			VIEWER
		);
	}

	@Override
	public String[] getAttributeDescriptions() {
	  return ArrayUtils.addAll(super.getAttributeDescriptions(),
	    "Scenario HTML Viewer"
	  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<?>[] getAttributeTypes() {
	  return ArrayUtils.addAll(super.getAttributeTypes(),
	    Boolean.class
	  );
	}
	  
	@Override
	public void setAttribute(String key, Object o) {
	  if (VIEWER.equals(key)) {
	    if (o instanceof String) {
	      o = Boolean.valueOf((String) o);
	    }
	    isHtmlViewer = (Boolean) o;
	    	    
    	((M44ScenarioNotesDialog)frame).initPackageContents();
    	
    	hasInitialized = true;
	  }
	  else {
	    super.setAttribute(key, o);
	  }
	}

	@Override
	public String getAttributeValueString(String key) {
	  if (VIEWER.equals(key)) {
	    return String.valueOf(isHtmlViewer);
	  }
	  else {
	    return super.getAttributeValueString(key);
	  }
	}

	protected class M44ScenarioNotesDialog extends JDialog {

		private static final long serialVersionUID = 1L;

		protected M44ScenarioNotesDialog() {
			super(GameModule.getGameModule().getPlayerWindow());
			initComponents();
			setLocationRelativeTo(getOwner());
		}

		protected void initComponents() {
			setLayout(new BorderLayout());
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
                    if (!isHtmlViewer)
                    {
					    cancel();
                    }
					setVisible(false);
				}
			});
			
			scenarioNotes = new M44ScenarioTextConfigurer(null, null, "", true);
		}
		
		public void initPackageContents()
		{
			GameModule gameModule = GameModule.getGameModule();
			DataArchive dataArchive = gameModule.getDataArchive();

            if (!isHtmlViewer)
            {
                add(scenarioNotes.getControls(), BorderLayout.CENTER);

                final JPanel p = new JPanel();
                final JButton saveButton = new JButton("Apply");
                p.add(saveButton);
                saveButton.addActionListener(e -> {
                    save();
                });
                final JButton cancelButton = new JButton(Resources.getString(Resources.CLOSE));
                cancelButton.addActionListener(e -> {
                    cancel();
                    setVisible(false);
                });
                p.add(cancelButton);
                add(p, BorderLayout.SOUTH);
            }
            else
            {
                // create jeditorpane
                jEditorPane = new JEditorPane();

                // make it read-only
                jEditorPane.setEditable(false);

                // add an html editor kit
                DataArchiveHTMLEditorKit kit = new DataArchiveHTMLEditorKit(dataArchive);
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
            }
		}
	}

    public class ScenarioHyperlinkListener implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }

            final String desc = event.getDescription();
            if ((!isURL() && desc.indexOf('/') < 0) || event.getURL() == null) {
                
            }
            else {					
                openWebpage(event.getURL());
            }
        }

        private boolean isURL() {
            return jEditorPane.getDocument().getProperty("stream") != null;
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


	@Override
	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("GameModule.html", "NotesWindow"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String encode(Command c) {
		if (c instanceof SetScenarioNote) {
            if (isHtmlViewer)
                setScenarioText(((SetScenarioNote) c).msg);
            else
			    return SCENARIO_NOTE_COMMAND_PREFIX + ((SetScenarioNote) c).msg;
		}
		return null;
	}

	@Override
	public Command decode(String command) {
		Command comm = null;
	    	    
	    if (command.startsWith(SCENARIO_NOTE_COMMAND_PREFIX)) {
            if (isHtmlViewer)
                setScenarioText(command.substring(SCENARIO_NOTE_COMMAND_PREFIX.length()));
            else
	    	    comm = new SetScenarioNote(command.substring(SCENARIO_NOTE_COMMAND_PREFIX.length()));
	    }
	    return comm;
	}

    private static String nthfield(String str, String delim, int field) {
		if (!str.matches(".*" + delim + ".*"))
			return str;
		String[] f = str.split(delim);
		if (f.length < field) {
			return "";
		} else {
			return f[field-1];
		}
	}

    protected static String flagForNation(String nation, Boolean isCommunist) {
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
		if (nation.equals("americans") || nation.equals("usa"))
			return "us";
		if (nation.equals("usmc") || nation.equals("united states marine corps") || nation.equals("us marine corps") || nation.equals("us marines") || nation.equals("united states marines"))
			return "usmc";
		if (nation.equals("france - free forces") || nation.equals("french - resistance") || nation.equals("france - resistance") || nation.equals("france - sas") || nation.equals("french resistance") || nation.equals("free french") || nation.equals("free france"))
			return "freefrench";
		if (nation.equals("vichy france") || nation.equals("france-vichy"))
			return "vichyfrench";
		if (nation.equals("netherlands") || nation.equals("dutch") || nation.equals("dutch east indies"))
			return "netherlands";
		if (nation.equals("china"))
			return isCommunist ? "communistchina" : "china";
		if (nation.equals("chinese nationalist") || nation.equals("nationalist china"))
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
		
		if (nation.equals("south korea") || nation.equals("south korean"))
			return "southkorea";
		if (nation.equals("north korea") || nation.equals("north korean"))
			return "northkorea";
		if (nation.equals("communist china") || nation.equals("republic china") || nation.equals("republic of china") || nation.equals("china republic"))
			return "communistchina";
		if (nation.equals("united nations"))
			return "un";
		
		if (nation.equals("nationalist spain")) return "spainrep";
		if (nation.equals("republican spain")) return "spainnat";
		if (nation.equals("new zealand")) return "newzealand";
		if (nation.equals("independent state of croatia") || nation.equals("ndh")) return "croatiaaxis";
		
		if ( (nation.equals("canada")) || (nation.equals("finland"))
				|| (nation.equals("slovakia")) || (nation.equals("italy")) || (nation.equals("poland")) || (nation.equals("belgium"))
				|| (nation.equals("romania")) || (nation.equals("vichy france")) || (nation.equals("hungary"))
				|| (nation.equals("australia")) || (nation.equals("albania")) || (nation.equals("norway")) || (nation.equals("greece"))
				|| (nation.equals("thailand")) || (nation.equals("croatia")) || (nation.equals("philippines")) || (nation.equals("bulgaria"))
				|| (nation.equals("southafrica")) || (nation.equals("brazil")
				|| (nation.equals("southkorea") || (nation.equals("northkorea") || nation.equals("communistchina") || nation.equals("un"))))
				) return nation;

		return "";
	}

    protected static String medalForFlag(String flag) {
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

	protected static String filterCompendium(String input) {
		input = input.replaceAll("\\((Actions .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Troops .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Nations .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Terrain .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((SWAs .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Airplanes .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Air Rules .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		input = input.replaceAll("\\((Battle of Nations .*?)\\)", "\\(<span class=\"compendium\">$1</span>\\)");
		
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

    private void setScenarioText(String msg) {
		String html = "";
		String scenarioNotesLines = "";
		String[] notes;
		try {
			if (msg.length() > 0) {
				scenarioNotesLines = msg.replace("|", "\n");
				notes = scenarioNotesLines.split("\n");

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
				
				if (notes.length > 0 && notes[0].startsWith("<html>"))
				{
					html = scenarioNotesLines;
				}
				else if (notes.length > 0)
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
	
						if (iter >= notes.length || !notes[iter-1].toLowerCase().startsWith("historical background"))
						{
							VassalTools.WriteLine("Warning: did not encounter Historical Background section where expected. Please fix.");
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
	
						if (iter >= notes.length || !notes[iter].trim().toLowerCase().startsWith("briefing"))
						{
							VassalTools.WriteLine("Warning: did not encounter Briefing section where expected. Please fix.");
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
							
							for (int start = 1; i < brief.length && start <= 2; start++) {
								String nation = "";
								String flag = "";
								String medal = "";
								Boolean isCommunist = brief[i].startsWith("Communist");
								switch (start) {
								case 1:
									nation = "Axis";
									flag = "germany";
									medal = isCommunist ? "medal-communist" : "medal-axis";
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
								
								String newFlag = flagForNation(nation, isCommunist);
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
								
								if (j < notes.length)
								{
									if (notes[j].startsWith("Axis: ") || notes[j].startsWith("Communist: ")) {
										if (start == 1) {
											//if (notes[j].trim().endsWith(" medal.") || notes[j].trim().endsWith(" medals.") ||
											//		notes[j].trim().endsWith(" Medal.") || notes[j].trim().endsWith(" Medals."))
											//{
												medals = notes[j].replaceAll("(Axis|Communist): ([0-9\\*]{1,3}).*", "$2");
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
								} else {
									VassalTools.WriteLine("ERROR: Problem parsing briefing, could not find Axis label. Please fix.");
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
							
							VassalTools.WriteLine("ERROR: Problem parsing briefing. Please report. " + ex.getMessage() + "\n" + sw.toString() + "\n\n" + html);
							
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
						
						if (iter >= notes.length || !notes[iter].trim().toLowerCase().startsWith("conditions of victory"))
						{
							VassalTools.WriteLine("Warning: did not encounter Conditions of Victory section where expected. Please fix.");
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
						
						if (iter >= notes.length || !notes[iter].trim().toLowerCase().startsWith("special rules"))
						{
							VassalTools.WriteLine("Warning: did not encounter Special Rules section where expected. Please fix.");
						}
						
						html += "<h3>Special Rules</h3>";
						
						iter++;
						blank = 0;
	
						while (iter < notes.length && !multiMap && blank < 2) {
							if (notes[iter].length() > 0 && notes[iter].charAt(0) == '#') {
								iter++;
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
						
						blank = 0;
						
						if (iter < notes.length && !multiMap && notes[iter].trim().toLowerCase().startsWith("notes") && blank < 2)
						{
							html += "<h3>Notes</h3>";
						
							iter++;
		
							while (iter < notes.length && !multiMap && blank < 2) {
								if (notes[iter].length() > 0 && notes[iter].charAt(0) == '#') {
									iter++;
								} else if (notes[iter].equals("==========***=========="))
								{
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
						}
						
						blank = 0;
						
						if (iter < notes.length && !multiMap && notes[iter].trim().toLowerCase().startsWith("designer") && blank < 2)
						{
							html += "<h3>Designer</h3>";
						
							iter++;
		
							while (iter < notes.length && !multiMap && blank < 2) {
								if (notes[iter].equals("==========***=========="))
								{
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
						}
						
						blank = 0;
						
						if (iter < notes.length && !multiMap && notes[iter].trim().toLowerCase().startsWith("contributor") && blank < 2)
						{
							html += "<h3>Scenario Contributor</h3>";
						
							iter++;
		
							while (iter < notes.length && !multiMap && blank < 2) {
								if (notes[iter].equals("==========***=========="))
								{
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
						}
						
						blank = 0;
	
						if (dowid.length() > 0) {
							html += "<br><table width=\"100%\" align=\"center\">";
							html += "<td align=\"center\"><a href=\"https://www.daysofwonder.com/memoir-44/archives/" + dowid + "\"><img src=\"m44-www-archives.png\" /></a></td>";
							html += "<td align=\"center\"><a href=\"https://dowlegacy.daysofwonder.com/memoir44/en/editor/view/?id=" + dowid + "\"><img src=\"m44-www-legacy.png\" /></a></td>";
							html += "</table>\n";
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
			
			VassalTools.WriteLine("Error parsing the scenario description into HTML, this might be a faulty scenario setup file. Please report.\n" + ex.getMessage() + "\n" + sw.toString() + "\n\n" + html);
		}
	}


	@Override
	public Configurable[] getConfigureComponents() {
		return new Configurable[0];
	}

	@Override
	public Class<?>[] getAllowableConfigureComponents() {
		return new Class<?>[0];
	}

	public static String getConfigureTypeName() {
		return "Memoir '44 Scenario Window";
	}

	/**
	 * Expects to be added to a {@link VASSAL.build.GameModule}.	Adds a button to
	 * the controls window toolbar to show the window containing the
	 * notes */
	@Override
	public void addTo(Buildable b) {
		super.addTo(b);
		getLaunchButton().setAlignmentY(0.0F);
		final GameModule gm = GameModule.getGameModule();
		gm.addCommandEncoder(this);
		gm.getGameState().addGameComponent(this);
		gm.addKeyStrokeSource(new KeyStrokeSource(frame.getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW));
	}

	@Override
	public void removeFrom(Buildable b) {
		super.removeFrom(b);
		final GameModule gm = GameModule.getGameModule();
		gm.removeCommandEncoder(this);
		gm.getGameState().removeGameComponent(this);	
    }

	@Override
	public void setup(boolean show) {
		getLaunchButton().setEnabled(show);
		try
		{
			if (!show && hasInitialized && !isHtmlViewer) {
				scenarioNotes.setValue(""); //$NON-NLS-1$
			}
		}
		catch (NullPointerException ex)
		{
			// oh well?
		}
	}
			

	@Override
	public Command getRestoreCommand() {
		return new SetScenarioNote(scenarioNotes.getValueString());
	}

	protected class SetScenarioNote extends Command {
		protected String msg;

		protected SetScenarioNote(String s) {
			msg = s;
		}

		@Override
		protected void executeCommand() {
			scenarioNotes.setValue(msg);
		}

		@Override
		protected Command myUndoCommand() {
			return null;
		}
	}
}
