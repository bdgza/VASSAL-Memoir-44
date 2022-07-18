package memoir44;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.build.module.PredefinedSetup;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.WriteErrorDialog;
import VASSAL.tools.filechooser.FileChooser;
import VASSAL.tools.filechooser.LogAndSaveFileFilter;
import VASSAL.tools.menu.ChildProxy;
import VASSAL.tools.menu.MenuItemProxy;
import VASSAL.tools.menu.MenuManager;

public class M44ExportScenarioItem extends AbstractConfigurable implements GameComponent {
	public static final String NAME = "name"; //$NON-NLS-1$
	protected MenuItemProxy menuItem;

	protected AbstractAction launchAction;
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	public M44ExportScenarioItem() {
		launchAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				launch();
			}
		};
		menuItem = new MenuItemProxy(launchAction);
	}


	public String[] getAttributeDescriptions() {
		return new String[]{
				Resources.getString(Resources.NAME_LABEL)
		};
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[]{
				String.class
		};
	}

	public String[] getAttributeNames() {
		return new String[]{
				NAME
		};
	}

	public String getAttributeValueString(String key) {
		if (NAME.equals(key)) {
			return getConfigureName();
		} else {
			return null;
		}
	}

	public void setAttribute(String key, Object value) {
		if (NAME.equals(key)) {
			setConfigureName((String) value);
			menuItem.getAction().putValue(Action.NAME, (String) value);
		}
	}

	public VisibilityCondition getAttributeVisibility(String name) {
		return super.getAttributeVisibility(name);
	}

	public void launch() {
		InputStream stream;
			
		try {
			GameState gameState = GameModule.getGameModule().getGameState();
			
			if (!gameState.isGameStarted()) return;

		    final File saveFile = getSaveFile();
		    if (saveFile == null) {
		    	return;
		    }

   	      	try {
   	      		gameState.saveGame(saveFile);
		    }
		    catch (IOException e) {
		    	WriteErrorDialog.error(e, saveFile);
		    	return;
		    }
			
   	      	if (isWindows())
   	      	{
   	      		stream = GameModule.getGameModule().getDataArchive().getInputStream("m44export.exe");
   	      	}
   	      	else if (isMac())
   	      	{
   	      		stream = GameModule.getGameModule().getDataArchive().getInputStream("m44export");
   	      	}
   	      	else if (isUnix())
   	      	{
   	      		stream = GameModule.getGameModule().getDataArchive().getInputStream("m44export.o");
   	      	}
   	      	else
   	      	{
   	      		return;
   	      	}
   	      	
			File tempFile = File.createTempFile("m44vx_", ".exe");
			OutputStream tempStream = new FileOutputStream(tempFile);
			tempStream.write(stream.readAllBytes());
			tempStream.close();
			
			tempFile.setExecutable(true);
			
			String inputFile = saveFile.getAbsolutePath();
			int extIndex = saveFile.getAbsolutePath().lastIndexOf('.');
			String outputFile = saveFile.getAbsolutePath().substring(0, extIndex) + ".m44";
			
			inputFile = inputFile.replace("\"", "");
			outputFile = outputFile.replace("\"", "");
			
			String procPath = tempFile.getAbsolutePath();
			
			if (isMac() || isUnix())
   	      	{
   	      		procPath = "./" + procPath;
   	      	}
			
			procPath = "/Users/bdegroot/Projects/vassal/m44export/publish/mac/m44export";
			String[] envp = new String[0];
			
			Runtime rt = Runtime.getRuntime();
			
			Process proc = null;
			if (isWindows())
   	      	{
				proc = rt.exec(procPath + " export \"" + inputFile + "\" \"" + outputFile + "\"", envp, tempFile.getParentFile().getAbsoluteFile());
   	      	}
   	      	else if (isMac())
   	      	{
   	      		proc = rt.exec(procPath + " export \"" + inputFile + "\" \"" + outputFile + "\"", envp, tempFile.getParentFile().getAbsoluteFile());
   	      	}
   	      	else if (isUnix())
   	      	{
   	      		proc = rt.exec(procPath + " export \"" + inputFile + "\" \"" + outputFile + "\"", envp, tempFile.getParentFile().getAbsoluteFile());
   	      	}
	        
	        InputStream stdout = proc.getInputStream();
	        InputStream stderr = proc.getErrorStream();
	        InputStreamReader isr = new InputStreamReader(stdout);
	        InputStreamReader isr2 = new InputStreamReader(stderr);
	        BufferedReader br = new BufferedReader(isr);
	        BufferedReader br2 = new BufferedReader(isr2);
	        
	        String outString = "";
	        while (proc.isAlive())
	        {
	        	while (br.ready())
		        {
	        		outString = outString + ProcessReadLine(br) + "\n";
		        }
	        }
	        
	        while (br.ready())
	        {
	        	outString = outString + ProcessReadLine(br) + "\n";
	        }
	        
//			JOptionPane.showMessageDialog(null, OS + "\n" + procPath + "\n" + inputFile + "\n" + outputFile + "\n\n" + outString, "Path", JOptionPane.INFORMATION_MESSAGE);
			
			tempFile.delete();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "FileNotFoundException", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
		}
		
		//M44ScenarioChooser chooser = new M44ScenarioChooser(GameModule.getGameModule().getGameState());
		//chooser.setVisible(true);
	}
	
	private String ProcessReadLine(BufferedReader br) {
		String line;
		try {
			line = br.readLine();
		} catch (IOException e) {
			return "";
		}
		
		if (line.startsWith(">>> "))
		{
			VassalTools.WriteLine("! Export Scenario: " + line.substring(4));
		}
		else if (line.startsWith("!!! "))
		{
			VassalTools.WriteLine("~ Export Scenario: " + line.substring(4));
		}
		
		return line;
	}


	public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0
                || OS.indexOf("nux") >= 0
                || OS.indexOf("aix") > 0);
    }
	
	private File getSaveFile() {
	    final FileChooser fc = GameModule.getGameModule().getFileChooser();
	    fc.selectDotSavFile();
	    fc.addChoosableFileFilter(new LogAndSaveFileFilter());

	    if (fc.showSaveDialog() != FileChooser.APPROVE_OPTION) return null;

	    File file = fc.getSelectedFile();

	    // append .vsav if it's not there already
	    if (!file.getName().endsWith(".vsav")) {
	      file = new File(file.getParent(), file.getName() + ".vsav"); //NON-NLS
	    }

	    return file;
	  }

	public InputStream getSavedGameContents() throws IOException {
		return null;
	}

	private ChildProxy<?> getMenuInUse() {
		return menuItem;
	}

	public void addTo(Buildable parent) {
		if (parent instanceof GameModule) {
			MenuManager.getInstance().addToSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
		}
		else if (parent instanceof M44ParentMenu) {
	      final M44ParentMenu setup = (M44ParentMenu) parent;
	      setup.menu.add(getMenuInUse()); // NEED TO MAKE MY OWN COPY OF PredefinedSetup just for parent menu
	    }

		GameModule.getGameModule().getGameState().addGameComponent(this);
	}

	public void removeFrom(Buildable parent) {
		if (parent instanceof GameModule) {
			MenuManager.getInstance()
			.removeFromSection("PredefinedSetup", getMenuInUse()); //$NON-NLS-1$
		}
		else if (parent instanceof M44ParentMenu) {
	      final M44ParentMenu setup = (M44ParentMenu) parent;
	      setup.menu.remove(getMenuInUse());
	    }

		GameModule.getGameModule().getGameState().removeGameComponent(this);
	}

	public Class<?>[] getAllowableConfigureComponents() {
		return new Class<?>[0];
	}

	public static String getConfigureTypeName() {
		return "Memoir '44 Export Scenario";
	}

	public HelpFile getHelpFile() {
		return HelpFile.getReferenceManualPage("GameModule.htm", "M44ExportScenarioItem"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Command getRestoreCommand() {
		return null;
	}

	public void setup(boolean gameStarting) {
		launchAction.setEnabled(gameStarting);
	}
}
