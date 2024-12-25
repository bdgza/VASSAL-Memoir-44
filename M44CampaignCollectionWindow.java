package memoir44;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import VASSAL.build.BadDataReport;
import VASSAL.build.GameModule;
import VASSAL.build.module.ChartWindow;
import VASSAL.build.module.GameComponent;
import VASSAL.i18n.Resources;
import VASSAL.preferences.PositionOption;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.LaunchButton;
import VASSAL.tools.ScrollPane;
import VASSAL.tools.ToolBarComponent;
import VASSAL.tools.WrapLayout;
import VASSAL.tools.filechooser.FileChooser;
import VASSAL.tools.filechooser.PNGFileFilter;
import VASSAL.tools.image.ImageUtils;
import VASSAL.tools.swing.ProgressDialog;
import VASSAL.tools.swing.SwingUtils;

public class M44CampaignCollectionWindow extends ChartWindow implements ListSelectionListener {
	private Vector<M44CampaignCollectionItem> campaigns = new Vector<M44CampaignCollectionItem>();
	private JList<M44CampaignCollectionItem> campaignList;
	protected JPanel campaignPanel;
	protected JToolBar toolBar = new JToolBar();
	protected JTabbedPane pageTabs = null;
	
	public M44CampaignCollectionWindow()
	{
		super();
				
	    final ActionListener al = new ActionListener() {
	      boolean initialized;

	      @Override
	      public void actionPerformed(ActionEvent e) {
	        if (!initialized) {
	        	final String key = PositionOption.key + id;
	        	GameModule.getGameModule().getPrefs().addOption(new PositionOption(key, frame));
			  
	        	Collection<GameComponent> comps = GameModule.getGameModule().getGameState().getGameComponents();
				
	        	Iterator<GameComponent> it = comps.iterator();
			
	        	do {
	        		GameComponent comp = it.next();
				
	        		if (comp instanceof M44CampaignCollectionItem) {
	        			M44CampaignCollectionItem campaignItem = (M44CampaignCollectionItem) comp;
					
	        			// add to our list
	        			campaigns.add(campaignItem);
	        		}
	        	} while (it.hasNext());
	        	
	        	// sort campaigns by name
	        	Collections.sort(campaigns);
	        	
	        	// set up UI
	        	CreateCampaignsUI();
	          
	        	initialized = true;
	        }
	        frame.setVisible(!frame.isVisible());
	      }
	    };
	    launch = new LaunchButton(null, TOOLTIP, BUTTON_TEXT, HOTKEY, ICON, al);
	    setAttribute(NAME, Resources.getString("Editor.ChartWindow.component_type"));
	    setAttribute(BUTTON_TEXT, Resources.getString("Editor.ChartWindow.component_type"));
	    launch.setAttribute(TOOLTIP, Resources.getString("Editor.ChartWindow.component_type"));
	}
	
	protected void CreateCampaignsUI() {
		root.setLayout(new BorderLayout());
		
		toolBar.setLayout(new WrapLayout(WrapLayout.RIGHT, 0, 0));
	    toolBar.setAlignmentX(0.0F);
	    toolBar.setFloatable(false);
	    
	    try {
			var cameraIcon = VassalTools.LoadImage("images/camera.png");
		
			JButton btnCamera = new JButton(new ImageIcon(cameraIcon));
			btnCamera.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
//					if (pageTabs == null) return;
//					
//					var item = campaignList.getSelectedValue();
//					var pageIndex = pageTabs.getSelectedIndex();
//					var page = item.pages[pageIndex];
//					var pageName = item.getAttributeValueString(M44CampaignCollectionItem.NAME) + "_" + (pageIndex + 1);
//					
//					// prompt user for image filename
//				    final FileChooser fc = GameModule.getGameModule().getFileChooser();
//				    fc.setSelectedFile(
//				      new File(fc.getCurrentDirectory(),
//				      pageName + ".png")
//				    );
//				    fc.addChoosableFileFilter(new PNGFileFilter());
//
//				    if (fc.showSaveDialog(frame) != FileChooser.APPROVE_OPTION) return;
//
//				    final File file = fc.getSelectedFile();
//				    
//				    Image pageImage;
//					try {
//						pageImage = VassalTools.LoadImage("images/" + page);
//					} catch (IOException e1) {
//						return;
//					}
//
//				    int width = pageImage.getWidth(null); 
//				    int height = pageImage.getHeight(null);
//				    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//				    Graphics g = bi.getGraphics(); 
//				    try { 
//				        g.drawImage(pageImage, 0, 0, null);
//				        ImageIO.write(bi, "png", file); 
//				    } catch (IOException e1) { 
//				        VassalTools.WriteLine("Error attempting to save image, " + e1.getMessage());
//				    }
					
					if (pageTabs == null) return;
					
					var item = campaignList.getSelectedValue();
					var pageIndex = pageTabs.getSelectedIndex();
					var page = item.file + "-" + (pageIndex+1) + ".campaign";
					var pageName = item.getAttributeValueString(M44CampaignCollectionItem.NAME) + "_" + (pageIndex + 1);
					
					// prompt user for image filename
				    final FileChooser fc = GameModule.getGameModule().getFileChooser();
				    fc.setSelectedFile(
				      new File(fc.getCurrentDirectory(),
				      pageName + ".png")
				    );
				    fc.addChoosableFileFilter(new PNGFileFilter());

				    if (fc.showSaveDialog(frame) != FileChooser.APPROVE_OPTION) return;

				    final File file = fc.getSelectedFile();
				    
				    Image pageImage;
					try {
						pageImage = VassalTools.LoadImage(page);
					} catch (IOException e1) {
						return;
					}

				    int width = pageImage.getWidth(null); 
				    int height = pageImage.getHeight(null);
				    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
				    Graphics g = bi.getGraphics(); 
				    try { 
				        g.drawImage(pageImage, 0, 0, null);
				        ImageIO.write(bi, "png", file); 
				    } catch (IOException e1) { 
				        VassalTools.WriteLine("Error attempting to save image, " + e1.getMessage());
				    }
				}
			});
			
			toolBar.add(btnCamera);
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    root.add(toolBar, BorderLayout.NORTH);
	    		
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	    split.setResizeWeight(0.25);
	    
	    campaignList = new JList<>(campaigns);
	    campaignList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    campaignList.setCellRenderer(new CampaignCellRenderer());
	    campaignList.addListSelectionListener(this);
	    
		final JScrollPane scroll = new ScrollPane(campaignList);
		campaignList.repaint();
		split.setLeftComponent(scroll);
		
		campaignPanel = new JPanel();
		campaignPanel.setLayout(new BorderLayout());
		  
		split.setRightComponent(campaignPanel);
		  
		root.add(split, BorderLayout.CENTER);
		
		if (campaigns.size() > 0)
	    {
	    	campaignList.setSelectedIndex(0);
	    }
	}

	private void PrintComponents(Container container, String prefix)
	{
		var count = container.getComponentCount();
		
		for (int i = 0; i < count; i++)
    	{
    		var component = container.getComponent(i);
    		
    		VassalTools.WriteLine(prefix + "-C = (" + i + ") " + component.getName() + ", " + component.getWidth() + ", " + component.getHeight() + "; " + component.toString());
    		
    		if (component instanceof Container)
    		{
    			var childContainer = (Container)component;
    			
    			PrintComponents(childContainer, prefix + "-XX");
    		}
    	}
	}
	
//	@Override
//	public void addTo(Buildable b) {
//		super.addTo(b);
//	}
	
	private class CampaignCellRenderer extends DefaultListCellRenderer {
	    private static final long serialVersionUID = 1L;
	    private Font standardFont;
	    private Font highlightFont;

	    @Override
	    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

	      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	      if (standardFont == null) {
	    	  var font = getFont();
	    	  standardFont = new Font(
	    		font.getFamily(),
	    		Font.PLAIN,
	    		(int)(font.getSize() * 1.5)
			  );
	    	  highlightFont = new Font(
			  	standardFont.getFamily(),
	    		Font.BOLD,
	    		standardFont.getSize()
			  );
	      }

	      if (value instanceof M44CampaignCollectionItem) {
	        final M44CampaignCollectionItem e = (M44CampaignCollectionItem) value;
	        setText(e.getAttributeValueString(M44CampaignCollectionItem.NAME));
	        
	        var iconName = e.getAttributeValueString(M44CampaignCollectionItem.ICON);
	        
			try {
				Image icon = VassalTools.LoadImage("images/" + iconName);
				
				setIcon(new ImageIcon(icon));
			} catch (IOException e1) {
				VassalTools.WriteLine("M44CampaignCollectionWindow Icon IOException = " + e1.getMessage());
			}
			
			if (isSelected)
			{
				setFont(highlightFont);
			}
			else
			{
				setFont(standardFont);
			}
	      }
	      return this;
	    }
	  }

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		
		var item = campaignList.getSelectedValue();
		
		campaignPanel.removeAll();
				
		pageTabs = new JTabbedPane();
		
		for (int i = 0; i < item.pages; i++)
		{
			var tabPanel = new JPanel();
			tabPanel.setLayout(new BorderLayout());
			tabPanel.setName(String.valueOf(i + 1));
			
			var page = item.file + "-" + (i+1);
			
			JLabel pageImageLabel = new JLabel();
			
//			try {
//				Image pageImage = VassalTools.LoadImage("images/" + page);				
//				ImageIcon pageImageIcon = new ImageIcon(pageImage);
				
				String imageName = page + ".campaign";
				
				try {
					InputStream stream = GameModule.getGameModule().getDataArchive().getInputStream(imageName);
					BufferedImage image = ImageUtils.getImage("", stream);
													
					if (image != null) {
						pageImageLabel.setIcon(new ImageIcon(image));
					}
				} catch (Exception e1) {
					ErrorDialog.dataWarning(new BadDataReport(Resources.getString("Error.not_found", "Image"), imageName, e1));
				}
				
//				var pageImageLabel = new JLabel();
//				pageImageLabel.setIcon(image);
//				pageImageLabel.setIcon(pageImageIcon);
				
				var pageImageScroll = new ScrollPane(pageImageLabel);
				
				tabPanel.add(pageImageScroll, BorderLayout.CENTER);
//			} catch (IOException e1) {
//				VassalTools.WriteLine("M44CampaignCollectionWindow Page IOException = " + e1.getMessage());
//			}
			
			pageTabs.add(tabPanel);
		}
		
		campaignPanel.add(pageTabs, BorderLayout.CENTER);
				
		campaignPanel.getParent().revalidate();
		campaignPanel.getParent().repaint();
	}
}
