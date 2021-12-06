package memoir44;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import VASSAL.build.BadDataReport;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.i18n.Resources;
import VASSAL.tools.DataArchive;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.image.ImageUtils;

public class M44ScenarioChooser extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JDialog thisFrame;
	
	private JPanel mainPanel;
	//private JPanel listPanel;
	private JPanel filterPanel;
	private JPanel buttonPanel;
	
	private JTable scenarioList;
	private ScenarioTableModel scenarioModel;
	
	private JPanel jpText;
	private JTextField jtText;
	private String filterText = "";
	
	private JPanel jpFront;
	private JCheckBox[] jcFront;
	private String[] frontNames = {"Western", "Eastern", "Pacific", "Mediterranean"};
	private boolean[] filterFronts = new boolean[frontNames.length + 1];
	
	private JPanel jpExpansions;
	//private JCheckBox[] jcExpansions;
	private JComboBox<String> jcExpansions;
	private int indexAirPackExpansion = -1;
//	private String[] expansionNames = {"Base Game", "Terrain Pack", "Eastern", "Pacific", "Air Pack", "Mediterranean", "Hedgerow Hell", "Tigers in the Snow", "Sword of Stalingrad", "Disaster at Dieppe", "Breakthrough", "Winter Wars", "Campaign Book Vol. 1", "Campaign Book Vol. 2", "Vercors Campaign", "Audie Murphy", "Invasion of Crete", "D-Day Landings", "Through Jungle and Desert", "New Flight Plan"};
//	private String[] expansionCodes = {"ST", "TP", "EF", "PT", "AA", "MT", "HH", "TS", "SS", "DD", "BT", "WW", "CB", "C2", "VC", "DM", "IC", "DY", "JD", "NFP"};
	private String[] expansionMenu; // = {"All", "Base Game", "Terrain Pack", "Eastern", "Pacific", "Air Pack", "Mediterranean", "Hedgerow Hell", "Tigers in the Snow", "Sword of Stalingrad", "Disaster at Dieppe", "Breakthrough", "Winter Wars", "Campaign Book Vol. 1", "Campaign Book Vol. 2", "Vercors Campaign", "Audie Murphy", "Invasion of Crete", "D-Day Landings", "Through Jungle and Desert", "New Flight Plan"};
	private boolean[] filterExpansions; // = new boolean[expansionNames.length + 2];
	
	private JPanel jpType;
	private JCheckBox[] jcType;
	private String breaklordLabel = "OverThrough";
	private String[] typeNames = {"Inland", "Beach", "Winter", "Desert", "BT-Inland", "BT-Beach", "BT-Winter", "BT-Desert", "OL-Inland", "OL-Beach", "OL-Winter", "OL-Desert", "BT-Extended", breaklordLabel, "MultiMap", "HexMap"};
	private boolean[] filterTypes = new boolean[typeNames.length + 1];
	
	private JPanel jpYear;
	private JCheckBox[] jcYear;
	private String[] yearNames = {"1936", "1937", "1938", "1939", "1940", "1941", "1942", "1943", "1944", "1945"};
	private String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	private boolean[] filterYears = new boolean[yearNames.length + 1];
	
	private JCheckBox jcOfficialScenarios;
	private boolean filterOfficial = false;
	private JCheckBox jcSFTF;
	private boolean filterSFTF = false;
	
	private JPanel numScenPanel;
	private JLabel jlNumScen;
	
	private JButton viewOnlineButton;
	private JButton randomButton;
	private JButton cancelButton;
	private JButton selectButton;
	
	private JPanel previewPanel;
	private JComponent previewPane;
	private Image previewImage = null;
	
	private JSplitPane splitPane;
	
	private static boolean checkedSetups = false;
	
	private GameState myGameState;
	private Vector<M44Setup> setups = new Vector<M44Setup>();
	
	private Vector<M44Series> series = new Vector<M44Series>();
	
	public M44ScenarioChooser(GameState gameState) {
		myGameState = gameState;
		
		jbInit();
	}
	
	private void jbInit() {
		thisFrame = this;
		
		Dimension dim = getToolkit().getScreenSize();
		
		int width, height;
		
		width = dim.width - 50;
		height = dim.height - 30;
		
		if (width > 1900)
			width = 1900;
		
		this.setTitle("Choose a Memoir '44 Scenario");
		this.setSize(width, height);
		this.setLocation((dim.width - width) / 2, (dim.height - height) / 2);
		this.setResizable(true);
		
		mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		//listPanel = new JPanel(new BorderLayout());
		
		filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		Collection<GameComponent> comps = myGameState.getGameComponents();
		
		Iterator<GameComponent> it = comps.iterator();
		
		do {
			GameComponent comp = it.next();
			if (comp instanceof M44Setup) {
				M44Setup setup = (M44Setup) comp;
				
				// add to our list
				setups.add(setup);
			}
			if (comp instanceof M44Series) {
				series.add((M44Series) comp);
			}
		} while (it.hasNext());
		
		// process classifieds
		
		for (int i = 0; i < setups.size(); i++)
		{
			M44Setup setup1 = setups.get(i);
			
			if (setup1.classified)
			{
				for (int j = 0; j < setups.size(); j++)
				{
					M44Setup setup2 = setups.get(j);
					
					if (!setup1.equals(setup2) && setup1.fileName.equals(setup2.fileName))
					{
						setups.remove(i);
						i--;
						break;
					}
				}
			}
		}
		
		// check for missing files
		
		if (!checkedSetups) {
			Runnable checkRunnable = new Runnable() {
				public void run() {
					GameModule gameModule = GameModule.getGameModule();
					DataArchive dataArchive = gameModule.getDataArchive();
					
					for (int i = 0; i < setups.size(); i++) {
						M44Setup setup = setups.get(i);
						
						if (!setup.classified)
						{
							// test to see if this setup is valid
							try {
								if (dataArchive.getURL(setup.getFileName()) == null) {
									//WriteLine("Error in module, could not find pre-defined setup file " + setup.getFileName());
									ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Setup"), setup.getFileName()));
								}
							} catch (Exception e1) {
								//WriteLine("Error in module, could not find pre-defined setup file " + setup.getFileName());
								ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Setup"), setup.getFileName(), e1));
							}
							
							String imageName = setup.fileName + ".preview"; // "previews/" +
							
							try {
								InputStream stream = GameModule.getGameModule().getDataArchive().getInputStream(imageName);
								BufferedImage image = ImageUtils.getImage("", stream);
																
								if (image == null || image.getHeight() != 500) {
									ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Image with correct size"), imageName));
								}
							} catch (Exception e1) {
								//WriteLine("Error in module, could not find proper pre-defined image preview file " + imageName);
								ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Image"), imageName, e1));
							}
						}
					}
				}
			};
			
			checkedSetups = true;
		
			Thread checkThread = new Thread(checkRunnable);
			checkThread.start();
		}
		
		filterExpansions = new boolean[series.size()];
		
		Comparator<M44Setup> sortScenarios = new ScenarioSorter();
		
		Collections.sort(setups, sortScenarios);
		
		Comparator<M44Series> sortSeries = new SeriesSorter();
		
		Collections.sort(series, sortSeries);
		
		for (int i = series.size() - 1; i > 0; i--)
		{
			if (series.get(i-1).getConfigureName().equals(series.get(i).getConfigureName()))
				series.remove(i);
		}
		
		for (int i = 0; i < series.size(); i++)
			if (series.get(i).prefix.equals("AA"))
				indexAirPackExpansion = i;
		
		expansionMenu = new String[series.size()+1];
		expansionMenu[0] = "All";
		int num = series.size();
		for (int i = 0; i < num; i++)
			expansionMenu[i+1] = series.get(i).getAttributeValueString(M44Series.NAME);
		
		filterExpansions = new boolean[expansionMenu.length + 1];
		
		numScenPanel = new JPanel();
		numScenPanel.setBorder(BorderFactory.createTitledBorder("Scenarios"));
		numScenPanel.setMaximumSize(new Dimension(32000, 24));
		jlNumScen = new JLabel("Number-of-Scenarios");
		numScenPanel.add(jlNumScen);
		
		filterPanel.add(numScenPanel);
		
		jpText = new JPanel();
		jpText.setBorder(BorderFactory.createTitledBorder("Filter"));
		jpText.setLayout(new BoxLayout(jpText, BoxLayout.Y_AXIS));
		//jpText.setLayout(new GridLayout(3, 1));
		
		JPanel jpTextField = new JPanel(new GridLayout(3, 1));
		
		jtText = new JTextField("                                                        ");
		jtText.setMinimumSize(new Dimension(200, 25));
		jtText.setMaximumSize(new Dimension(32000, 25));
		jtText.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				filterText = jtText.getText().toLowerCase();

				filterScenarioList();
			}
			
			public void insertUpdate(DocumentEvent e) {
				filterText = jtText.getText().toLowerCase();

				filterScenarioList();
			}
			
			public void changedUpdate(DocumentEvent e) {
				filterText = jtText.getText().toLowerCase();

				filterScenarioList();
			}
		});
		jpTextField.add(jtText);
		
		//jpText.add(jtText); //jpTextField);
		
		JPanel pOff = new JPanel();
		pOff.setLayout(new BoxLayout(pOff, BoxLayout.Y_AXIS));
		pOff.setMaximumSize(new Dimension(32000, 24));
		
		jcOfficialScenarios = new JCheckBox("Official Scenarios");
//		jcOfficialScenarios.setSelected(true);
		jcOfficialScenarios.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterOfficial = jcOfficialScenarios.isSelected();
				
				filterScenarioList();
			}
		});
		
		//pOff.add(jcOfficialScenarios);
		//jpText.add(jcOfficialScenarios); //pOff);
		jpTextField.add(jcOfficialScenarios);
		
		pOff = new JPanel();
		pOff.setLayout(new BoxLayout(pOff, BoxLayout.Y_AXIS));
		pOff.setMaximumSize(new Dimension(32000, 24));
		
		jcSFTF = new JCheckBox("SFTF (Unofficial)");
//		jcSFTF.setSelected(true);
		jcSFTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterSFTF = jcSFTF.isSelected();
				
				filterScenarioList();
			}
		});
		
		//pOff.add(jcSFTF);
		//jpText.add(jcSFTF); //pOff);
		jpTextField.add(jcSFTF);
		
		jpText.add(jpTextField);
		jpText.add(Box.createVerticalGlue());
		
		filterPanel.add(jpText);
		
		jpFront = new JPanel();
		jpFront.setBorder(BorderFactory.createTitledBorder("Front"));
		jpFront.setLayout(new BoxLayout(jpFront, BoxLayout.Y_AXIS));
		
		jcFront = new JCheckBox[frontNames.length];
		for (int i = 0; i < frontNames.length; i++) {
			final int j = i;
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.setMaximumSize(new Dimension(32000, 24));
			jcFront[i] = new JCheckBox(frontNames[i]);
//			jcFront[i].setSelected(true);
//			filterFronts[i] = true;
			jcFront[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					filterFronts[j] = jcFront[j].isSelected();
					
					filterScenarioList();
				}
			});
			p.add(jcFront[i]);
			jpFront.add(p);
		}
		
		filterPanel.add(jpFront);
		
		jpExpansions = new JPanel();
		jpExpansions.setBorder(BorderFactory.createTitledBorder("Series")); // Expansions
		jpExpansions.setLayout(new BoxLayout(jpExpansions, BoxLayout.Y_AXIS));
		
		jcExpansions = new JComboBox<String>(expansionMenu);
		jcExpansions.setEditable(false);
		jcExpansions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int idx = jcExpansions.getSelectedIndex();
				
				for (int i = 0; i < filterExpansions.length; i++)
					filterExpansions[i] = ((idx-1) == i);
				
				filterScenarioList();
			}
		});
		jpExpansions.add(jcExpansions);
		
		filterPanel.add(jpExpansions);
		
		jpType = new JPanel();
		jpType.setBorder(BorderFactory.createTitledBorder("Type"));
		jpType.setLayout(new BoxLayout(jpType, BoxLayout.Y_AXIS));
		
		jcType = new JCheckBox[typeNames.length];
		for (int i = 0; i < typeNames.length; i++) {
			final int j = i;
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.setMaximumSize(new Dimension(32000, 24));
			jcType[i] = new JCheckBox(typeNames[i]);
//			jcType[i].setSelected(true);
//			filterTypes[i] = true;
			jcType[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					filterTypes[j] = jcType[j].isSelected();
					
					filterScenarioList();
				}
			});
			p.add(jcType[i]);
			jpType.add(p);
		}
		
		filterPanel.add(jpType);
		
		jpYear = new JPanel();
		jpYear.setBorder(BorderFactory.createTitledBorder("Year"));
		jpYear.setLayout(new BoxLayout(jpYear, BoxLayout.Y_AXIS));
		
		jcYear = new JCheckBox[yearNames.length];
		for (int i = 0; i < yearNames.length; i++) {
			final int j = i;
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.setMaximumSize(new Dimension(32000, 24));
			jcYear[i] = new JCheckBox(yearNames[i]);
//			jcYear[i].setSelected(true);
//			filterYears[i] = true;
			jcYear[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					filterYears[j] = jcYear[j].isSelected();
					
					filterScenarioList();
				}
			});
			p.add(jcYear[i]);
			jpYear.add(p);
		}
		
		filterPanel.add(jpYear);
				
		filterPanel.add(Box.createVerticalGlue());
		
		JScrollPane filterScroller = new JScrollPane(filterPanel);
		filterScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		filterScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		scenarioModel = new ScenarioTableModel();
		scenarioList = new JTable(scenarioModel);
		filterScenarioList();
		
		JTableHeader header = scenarioList.getTableHeader();
	    header.setUpdateTableInRealTime(true);
	    header.addMouseListener(scenarioModel.new ColumnListener(scenarioList));
	    header.setReorderingAllowed(true);
		
		scenarioList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
        	put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            	"pressSelectScenario");
		
		scenarioList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int row = scenarioList.getSelectedRow();
				String primaryImageName = "";
				String imageName = "";
				
				if (row >= 0)
				{
					// load preview pane image
					M44Setup setup = scenarioModel.getRow(row);
					if (setup.fileName != null)
					{
						imageName = scenarioModel.getRow(row).getAttributeValueString(M44Setup.FILE) + ".preview"; // "previews/" +
						if (!setup.classified)
						{
							primaryImageName = imageName;
						}
						else
						{
							primaryImageName = scenarioModel.getRow(row).getAttributeValueString(M44Setup.FILE) + "_c.preview"; // "previews/" +
						}
					}
				}
				
				if (imageName.length() > 0)
				{
					try
					{
						previewImage = LoadPreviewImage(primaryImageName);
					}
					catch (Exception e0)
					{
						try {
							previewImage = LoadPreviewImage(imageName);
						} catch (FileNotFoundException e1) {
							ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Preview Image"), imageName));
//							VassalTools.WriteLine("Preview Error: File Not Found; " + imageName);
//							e1.printStackTrace();
							previewImage = null;
						} catch (IOException e1) {
							ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Preview Image"), imageName));
//							VassalTools.WriteLine("Preview Error: File Not Found; " + imageName);
//							e1.printStackTrace();
							previewImage = null;
						}
					}
				}
				else
					previewImage = null;
				
				previewPane.repaint();
			}
		});
		scenarioList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = ((JTable) e.getSource()).getSelectedRow();
					M44Setup setup = scenarioModel.getRow(row);
					
					launchSetup(setup);
				}
			}
		});
		int[] widths = {85, 0, 0, 0, 105, 70, 0, 120, 105};
		
		for (int i = 0; i < widths.length; i++)
			if (widths[i] > 0) {
				scenarioList.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
				scenarioList.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
			}
		scenarioList.getColumnModel().getColumn(1).setMinWidth(250);
		
		JScrollPane scScenarioList = new JScrollPane(scenarioList);
		//listPanel.add(scScenarioList, BorderLayout.CENTER);
		
		viewOnlineButton = new JButton("View Online");
		viewOnlineButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pressViewOnline();
			}
		});
		randomButton = new JButton("Pick Random");
		randomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pressRandomScenario();
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				thisFrame.dispose();
			}
		});
		selectButton = new JButton("Load");
		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pressSelectScenario();
			}
		});
		selectButton.setDefaultCapable(true);
		//selectButton.setEnabled(!GameModule.getGameModule().getGameState().isGameStarted());
		
		JButton btCheckNone = new JButton("Select None");
		btCheckNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < frontNames.length; i++)
					if (jcFront[i].isSelected()) {
						jcFront[i].setSelected(false);
						filterFronts[i] = false;
					}
				
				for (int i = 0; i < typeNames.length; i++)
					if (jcType[i].isSelected()) {
						jcType[i].setSelected(false);
						filterTypes[i] = false;
					}
				
				for (int i = 0; i < filterExpansions.length; i++)
					//if (jcExpansions[i].isSelected()) {
					//	jcExpansions[i].setSelected(false);
						filterExpansions[i] = false;
					//}
				jcExpansions.setSelectedIndex(0);
				
				for (int i = 0; i < yearNames.length; i++)
					if (jcYear[i].isSelected()) {
						jcYear[i].setSelected(false);
						filterYears[i] = false;
					}
				
				filterScenarioList();
			}
		});
		
		JButton btCheckAll = new JButton("Select All");
		btCheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < frontNames.length; i++)
					if (!jcFront[i].isSelected()) {
						jcFront[i].setSelected(true);
						filterFronts[i] = true;
					}
				
				for (int i = 0; i < typeNames.length; i++)
					if (!jcType[i].isSelected()) {
						jcType[i].setSelected(true);
						filterTypes[i] = true;
					}
				
				for (int i = 0; i < filterExpansions.length; i++)
					//if (!jcExpansions[i].isSelected()) {
						//jcExpansions[i].setSelected(true);
						filterExpansions[i] = false; //true;
					//}
				jcExpansions.setSelectedIndex(0);
				
				for (int i = 0; i < yearNames.length; i++)
					if (!jcYear[i].isSelected()) {
						jcYear[i].setSelected(true);
						filterYears[i] = true;
					}
				
				filterScenarioList();
			}
		});
		
		buttonPanel.add(btCheckNone);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(btCheckAll);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(viewOnlineButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(randomButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(selectButton);
		
		previewPanel = new JPanel(new BorderLayout());
		previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
		
		previewPane = new JComponent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				if (previewImage != null) {
					int h = previewImage.getHeight(null);
					int w = previewImage.getWidth(null);
					int ph = this.getHeight();
					
					double ratio = (double)ph / (double)h;
					
					int nh = ph > h ? h : ph;
					int nw = ph > h ? w : (int)Math.round((double)w * ratio);
					
					g.drawImage(previewImage, 0, 0, nw, nh, null);
				}
			}
		};
		
		previewPane.setPreferredSize(new Dimension(1500, 500));
		previewPane.setMinimumSize(new Dimension(350, 250));
		previewPane.setMaximumSize(new Dimension(1500,500));
		//previewPanel.add(previewPane, BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scScenarioList, previewPane);
		splitPane.setOneTouchExpandable(false);
		
		//listPanel.add(previewPanel, BorderLayout.SOUTH);
	
		//mainPanel.add(listPanel, BorderLayout.CENTER);
		mainPanel.add(splitPane, BorderLayout.CENTER);
		mainPanel.add(filterScroller, BorderLayout.WEST);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		thisFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		thisFrame.getRootPane().setDefaultButton(selectButton);
		
		jtText.setText("");
		
		splitPane.setDividerLocation((height < 1050) ? height - 340 : height - 590);
	}
	
	protected Image LoadPreviewImage(String file) throws FileNotFoundException, IOException
	{
		InputStream stream;
		
		stream = GameModule.getGameModule().getDataArchive().getInputStream(file);
		return ImageUtils.getImage("", stream);
	}
	
	protected BufferedImage ReadImageFromURL(URL image)
	{
		
		
//		ImageInputStream stream = ImageIO.createImageInputStream(input);
//		ImageReader reader = ImageIO.getImageReaders(stream).next(); // TODO: Test hasNext()
//		reader.setInput(stream);
//
//		int width = reader.getWidth(0);
//		int height = reader.getHeight(0);
//		ImageTypeSpecifier spec = reader.getImageTypes(0).next(); // TODO: Test hasNext(); 
//
//		BufferedImage image = MappedImageFactory.createCompatibleMappedImage(width, height, spec)
//
//		ImageReadParam param = reader.getDefaultReadParam();
//		param.setDestination(image);

//		image = reader.read(0, param); // Will return same image as created above
		
		return null;
	}
	
	protected void pressSelectScenario() {
		int row = scenarioList.getSelectedRow();
		if (row >= 0)
			launchSetup(scenarioModel.getRow(row));
	}
	
	protected void filterScenarioList() {
		Vector<M44Setup> list = new Vector<M44Setup>();
		
		boolean doOriginFilter = (filterOfficial || filterSFTF);
		
		boolean doFrontFilter = false;
		
		for (int j = 0; j < frontNames.length; j++)
			if (filterFronts[j] == true)
				doFrontFilter = true;
		
		boolean doExpansionFilter = false;
		
		for (int j = 0; j < series.size(); j++)
			if (filterExpansions[j] == true)
				doExpansionFilter = true;

		boolean doTypeFilter = false;
		
		for (int j = 0; j < typeNames.length; j++)
			if (filterTypes[j] == true)
				doTypeFilter = true;

		boolean doYearFilter = false;
		
		for (int j = 0; j < yearNames.length; j++)
			if (filterYears[j] == true)
				doYearFilter = true;
		
//		boolean doFrontFilter = true;
//		boolean doExpansionFilter = true;
//		boolean doTypeFilter = true;
//		boolean doYearFilter = true;
		
		for (int i = 0; i < setups.size(); i++) {
			M44Setup item = setups.get(i);
			
			String frontName = item.front;
			String yearName = item.year;
			String typeName = item.scentype;
			String preCode = "";
			if (item.m44code.length() >= 2)
				preCode = item.m44code.substring(0, 2);
			
//			String errorLogPath = new File(Info.getConfDir(), "errorLog").getAbsolutePath();
			
			boolean isAirPack = false;
			if (preCode.equals("AW") || preCode.equals("AE") || preCode.equals("AM") || preCode.equals("AP")) isAirPack = true;
			boolean filterAirPack = false;
			
			boolean isOfficial = false;
			
			if (item.official != null) isOfficial = item.official;
			
			boolean found = false;
			
			for (int j = 0; j < frontNames.length; j++)
				if (frontNames[j].equals(frontName))
					found = true;
			// No "Other" Front
			//if (!found)
			//	frontName = frontNames[frontNames.length-1];
			
			found = false;
			int foundLength = -1;
			
			for (int j = 0; j < series.size(); j++) {
				M44Series serie = series.get(j);
				
				int pLength = serie.prefix.length();
				
				String seriesCode = item.m44code;
				if (seriesCode.length() > pLength)
				{
					seriesCode = seriesCode.substring(0, pLength);
				}
				
				if (serie.prefix.equals(seriesCode))
				{
					found = true;
					if (foundLength < pLength)
					{
						preCode = seriesCode;
						foundLength = pLength;
					}
				}
			}
			if (!found)
				if (preCode.length() >= 1 && preCode.substring(0, 1).equals("A"))
					filterAirPack = true;
				else
					preCode = "";
			
			found = false;
			yearName = "19"+yearName;
			
			for (int j = 0; j < yearNames.length; j++)
				if (yearNames[j].equals(yearName))
					found = true;
			// No "Other" year
			//if (!found)
			//	yearName = yearNames[yearNames.length-1];
			
			boolean keepItem = true;
			
			if (doOriginFilter && isOfficial && !filterOfficial)
				keepItem = false;
			
			if (doOriginFilter && !isOfficial && !filterSFTF)
				keepItem = false;
			
			if (keepItem && doFrontFilter) {
				keepItem = false;
				for (int j = 0; j < frontNames.length; j++)
					if ((frontNames[j].equals(frontName)) && (filterFronts[j] == true))
						keepItem = true;
			}
			
			if (keepItem && doExpansionFilter) {
				keepItem = false;
				
				if (filterAirPack && filterExpansions[indexAirPackExpansion] == true) {
					if (isAirPack)
						keepItem = true;
				} else {
					for (int j = 0; j < series.size(); j++)
						if ((series.get(j).prefix.equals(preCode)) && (filterExpansions[j] == true))
							keepItem = true;
				}
			}
			
			if (keepItem)
				if (doTypeFilter) {
					keepItem = false;
					String[] breaklordArray = {"OT-Inland", "OT-Beach", "OT-Winter", "OT-Desert"};
					for (int j = 0; j < typeNames.length; j++)
					{
						if ((typeNames[j].equals(typeName)) && (filterTypes[j] == true))
							keepItem = true;
						if ((typeNames[j].equals(breaklordLabel)) && (filterTypes[j] == true))
						{
							for (int k = 0; k < breaklordArray.length; k++)
								if ((breaklordArray[k].equals(typeName)))
									keepItem = true;
						}
					}
				}
			
			if (keepItem)
				if (doYearFilter) {
					keepItem = false;
					for (int j = 0; j < yearNames.length; j++)
						if ((yearNames[j].equals(yearName)) && (filterYears[j] == true))
							keepItem = true;
				}
			
			if (keepItem)
				if (filterText.length() > 0) {
					keepItem = false;
					
					String name = item.getConfigureName();
					String set = "";
					if (item.set != null) set = item.set;
					String tournament = "";
					if (item.tournament != null) tournament = item.tournament;
					String operation = ""; if (item.operation != null) operation = item.operation;
					String author = ""; if (item.author != null) author = item.author;
					
					if (name.toLowerCase().indexOf(filterText) >= 0) keepItem = true;
					if (tournament.toLowerCase().indexOf(filterText) >= 0) keepItem = true;
					if (set.toLowerCase().indexOf(filterText) >= 0) keepItem = true;
					if (operation.toLowerCase().indexOf(filterText) >= 0) keepItem = true;
					if (author.toLowerCase().indexOf(filterText) >= 0) keepItem = true;
				}
			
			if (keepItem)
				list.add(item);
		}
		
		scenarioModel.setData(list);
		scenarioModel.sortData(scenarioList);
		
		jlNumScen.setText("" + list.size() + " of " + setups.size());
	}
	
	public int tryParse(String value, int defaultVal) {
	    try {
	        return Integer.parseInt(value);
	    } catch (NumberFormatException e) {
	        return defaultVal;
	    }
	}
	
	protected void pressViewOnline() {
		int row = scenarioList.getSelectedRow();
		if (row >= 0)
		{
			M44Setup scen = scenarioModel.getRow(row);
			if (scen.dowid != "")
			{
				try {
					openWebpage(new URL("https://www.daysofwonder.com/memoir44/en/editor/view/?id=" + scen.dowid));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					openWebpage(new URL("https://www.daysofwonder.com/memoir44/en/content/scenariofans/"));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	public static void openWebpage(URL url) {
	    try {
	        openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	}
	
	protected void pressRandomScenario() {
		Random r = new Random();
		
		int max = scenarioModel.getRowCount();
		int choice = r.nextInt(max);
		scenarioList.getSelectionModel().setSelectionInterval(choice, choice);
		scenarioList.scrollRectToVisible(new Rectangle(scenarioList.getCellRect(choice, 0, true)));
	}

	protected void launchSetup(M44Setup setup) {
//		if (setup.classified)
//		{
//			
//		}
//			JOptionPane.showMessageDialog(null, "This scenario is 'Classified' and is not permitted to be included by Days of Wonder.\n\n" + 
//					"Classified scenarios are usually official scenarios published in print but not on the Web. This is why their content\n" +
//					"is not displayed. However, you may still rate them and write After Action Reports.\n\n" +
//					"The game components required to implement this scenario are available in the module.", "Classified Scenario", JOptionPane.INFORMATION_MESSAGE);
		if (setup.launch())
			thisFrame.dispose();
	}

	private class ScenarioTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private Vector<M44Setup> data = new Vector<M44Setup>();
		private String[] columnNames = {"Type", "Title", "Set", "Tournament", "Front", "Date", "Operation", "Author", "Code"};
		
		protected int sortCol = 1;
		protected boolean isSortAsc = true;
		protected boolean sortByID = false;
		
		public void setData(Vector<M44Setup> newData) {
			data.clear();
			data = newData;
			
			fireTableDataChanged();
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}
		
		public int getColumnCount() {
			return 9;
		}

		public int getRowCount() {
			return data.size();
		}
		
		public M44Setup getRow(int row) {
			return data.get(row);
		}

		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return data.get(row).scentype;
			case 1:
				String name = data.get(row).getConfigureName();
//				if (data.get(row).airpack)
//					name += " [AIR PACK]";
				return name;
			case 2:
				if ((data.get(row).set == null) || (data.get(row).set.length() == 0))
					return "";
				else
					return data.get(row).set;
			case 3:
				if ((data.get(row).tournament == null) || (data.get(row).tournament.length() == 0))
					return "";
				else
					return data.get(row).tournament;
			case 4:
				return data.get(row).front;
			case 5:
				return data.get(row).getDate();
			case 6:
				return data.get(row).operation;
			case 7:
				return data.get(row).author;
			case 8:
				String code = "";
				if (data.get(row).m44code != null)
					code = data.get(row).m44code;
				String dow = "";
				if (data.get(row).dowid != null)
					dow = data.get(row).dowid;
				
				if ((code.length() > 0) && (dow.length() > 0)) {
					code = code + " #" + dow;
				} else if (dow.length() > 0)
					code = "#" + dow;
				
				return code;
			default:
				return "";
			}
		}
		
		public void sortData(JTable table) {
			Collections.sort(data,new MyComparator(isSortAsc, sortCol));
	    	table.tableChanged(new TableModelEvent(ScenarioTableModel.this));
	    	table.repaint();
		}
		
		class ColumnListener extends MouseAdapter {
		    protected JTable table;

		    public ColumnListener(JTable t) {
		      table = t;
		    }

		    public void mouseClicked(MouseEvent e) {
		    	TableColumnModel colModel = scenarioList.getColumnModel();
		    	int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
		    	int modelIndex = colModel.getColumn(columnModelIndex)
		    	.getModelIndex();

		    	if (modelIndex < 0)
		    		return;
		    	if (sortCol == modelIndex) {
		    		if ((sortCol == 8) && !isSortAsc)
		    			sortByID = !sortByID;
		    		isSortAsc = !isSortAsc;
		    	} else {
		    		sortCol = modelIndex;
		    		isSortAsc = true;
		    		sortByID = false;
		    	}

		    	for (int i = 0; i < getColumnCount(); i++) { 
		    		TableColumn column = colModel.getColumn(i);
		    		column.setHeaderValue(getColumnName(column.getModelIndex()));
		    	}
		    	table.getTableHeader().repaint();

		    	sortData(table);
		    }
		}

		class MyComparator implements Comparator<M44Setup> {
			protected boolean isSortAsc;
			protected int col;

			public MyComparator(boolean sortAsc, int col) {
				this.isSortAsc = sortAsc;
				this.col = col;
			}

			public int compare(M44Setup o1, M44Setup o2) {
				int result = 0;
				
				try {
					switch (col) {
					case 0:
						result = getScenTypeNumeric(o1.scentype) - getScenTypeNumeric(o2.scentype);
						break;
					case 1:
						result = o1.getConfigureName().compareTo(o2.getConfigureName()); break;
					case 2:
						String set1 = "";
						String set2 = "";
						if (o1.set != null)
							set1 = o1.set;
						if (o2.set != null)
							set2 = o2.set;
						int s1l = set1.length();
						int s2l = set2.length();
						if (s1l != 0 && s2l != 0)
						{
							result = set1.compareTo(set2);
						}
						else if (s1l == 0 && s2l == 0)
						{
							result = 0;
						}
						else if (s1l == 0)
						{
							result = isSortAsc ? 1 : -1;
						}
						else
						{
							result = isSortAsc ? -1 : 1;
						}
						break;
					case 3:
						String tournament1 = "";
						String tournament2 = "";
						if (o1.tournament != null)
							tournament1 = o1.tournament;
						if (o2.tournament != null)
							tournament2 = o2.tournament;
						int t1l = tournament1.length();
						int t2l = tournament2.length();
						if (t1l != 0 && t2l != 0)
						{
							result = tournament1.compareTo(tournament2);
						}
						else if (t1l == 0 && t2l == 0)
						{
							result = 0;
						}
						else if (t1l == 0)
						{
							result = isSortAsc ? 1 : -1;
						}
						else
						{
							result = isSortAsc ? -1 : 1;
						}
						break;
					case 4:
						int f1 = getFrontNumeric(o1.front);
						int f2 = getFrontNumeric(o2.front);
						result = f1 - f2;
						break;
					case 5:
						result = o1.year.compareTo(o2.year);
						if (result == 0) {
							int m1 = getNumericMonth(o1.month);
							int m2 = getNumericMonth(o2.month);
							
							result = m1 - m2;
						}
						 break;
					case 6:
						String operation1 = "";
						String operation2 = "";
						if (o1.operation != null)
							operation1 = o1.operation;
						if (o2.operation != null)
							operation2 = o2.operation;
						int o1l = operation1.length();
						int o2l = operation2.length();
						if (o1l != 0 && o2l != 0)
						{
							result = operation1.compareTo(operation2);
						}
						else if (o1l == 0 && o2l == 0)
						{
							result = 0;
						}
						else if (o1l == 0)
						{
							result = isSortAsc ? 1 : -1;
						}
						else
						{
							result = isSortAsc ? -1 : 1;
						}
						break;
					case 7:
						if (o1.author == "" && o2.author != "")
							result = isSortAsc ? 1 : -1;
						else if (o1.author != "" && o2.author == "")
							result = isSortAsc ? -1 : 1;
						else
							result = o1.author.toLowerCase().compareTo(o2.author.toLowerCase());
						break;
					case 8:
						String code1 = o1.m44code;
						String code2 = o2.m44code;
						int c1 = code1.length();
						int c2 = code2.length();
						if (!sortByID && ((c1 != 0) && (c2 != 0)))
						{
							if (c1 == c2)
							{
								result = code1.compareTo(code2);
							}
							else
							{
								result = c1 < c2 ? -1 : 1;
							}
						}
						else if (!sortByID && ((c1 != 0) && (c2 == 0)))
							result = -1;
						else if (!sortByID && ((c1 == 0) && (c2 != 0)))
							result = 1;
						else {
							try {
								c1 = Integer.parseInt(o1.dowid);
							} catch (NumberFormatException ex) {
								c1 = 99999999;
							}
							try {
								c2 = Integer.parseInt(o2.dowid);
							} catch (NumberFormatException ex) {
								c2 = 99999999;
							}
							result = c1 - c2;
						}
						
						break;
					}
				} catch (NullPointerException ex) {

				}
				
				if (!isSortAsc)
					result = -result;
				
				return result;
			}

			private int getFrontNumeric(String front) {
				int value = 999;
				
				for (int i = 0; i < frontNames.length; i++)
					if (front.equals(frontNames[i]))
						value = i;
				
				return value;
			}

			public boolean equals(Object obj) {
				if (obj instanceof MyComparator) {
					MyComparator compObj = (MyComparator) obj;
					return compObj.isSortAsc == isSortAsc;
				}
				return false;
			}
			
			public int getScenTypeNumeric(String scentype) {
				int value = 999;
				
				for (int i = 0; i < typeNames.length; i++)
					if (scentype.equals(typeNames[i]))
						value = i;
				
				return value;
			}
			
			public int getNumericMonth(String month) {
				int value = 999;
				
				for (int i = 0; i < monthNames.length; i++)
					if (month.equals(monthNames[i]))
						value = i;
				
				return value;
			}
		}
	}
	
	public class URLCellRenderer extends JLabel implements TableCellRenderer {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// This method is called each time a cell in a column
	    // using this renderer needs to be rendered.
	    public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
	        // 'value' is value contained in the cell located at
	        // (rowIndex, vColIndex)

	    	setForeground(new Color(0, 0, 255));
	    	
	        if (isSelected) {
	            // cell (and perhaps other cells) are selected
	        }

	        if (hasFocus) {
	            // this cell is the anchor and the table has the focus
	        }

	        // Configure the component with the specified value
	        setText(value.toString());

	        // Set tool tip if desired
	        setToolTipText((String)value);

	        // Since the renderer is a component, return itself
	        return this;
	    }

	    // The following methods override the defaults for performance reasons
	    public void validate() {}
	    public void revalidate() {}
	    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
	    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
	}
}
