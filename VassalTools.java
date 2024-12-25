package memoir44;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.tools.FormattedString;
import VASSAL.tools.image.ImageUtils;

public class VassalTools
{
	public static void WriteLine(String msgLine)
	{
		if (msgLine == null)
		{
			msgLine = "_null_";
		}
		
		FormattedString cStr = new FormattedString("- " + msgLine);
		final Command cc = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), cStr.getLocalizedText(cStr, msgLine));
		cc.execute();
		GameModule.getGameModule().sendAndLog(cc);
	}
	
	public static void WriteLine(String msgLine, boolean logMsg)
	{
		if (msgLine == null)
		{
			msgLine = "_null_";
		}
		
		if (!logMsg)
		{
			WriteLine(msgLine);
			return;
		}
		
		final GameModule mod = GameModule.getGameModule();
		
		FormattedString cStr = new FormattedString("- " + msgLine);
		final Command cc = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), cStr.getLocalizedText(cStr, msgLine));
		cc.execute();
		mod.getServer().sendToOthers(cc);
	}
	
	public static Image LoadImage(String file) throws FileNotFoundException, IOException
	{
		InputStream stream;
		
		stream = GameModule.getGameModule().getDataArchive().getInputStream(file);
		return ImageUtils.getImage(file, stream);
	}
}
