package memoir44;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.tools.FormattedString;

public class VassalTools35 {
	
	@SuppressWarnings("deprecation")
	static void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString("- " + msgLine);
		Chatter chatter = GameModule.getGameModule().getChatter();
		
		if (chatter == null) return;
		
		final Command cc = new Chatter.DisplayText(chatter, cStr.getLocalizedText());
		
		cc.execute();
		GameModule.getGameModule().sendAndLog(cc);
	}
	
	@SuppressWarnings("deprecation")
	static void SendLine(String msgLine) {
		final GameModule mod = GameModule.getGameModule();
		
		FormattedString cStr = new FormattedString("- " + msgLine);
		Chatter chatter = GameModule.getGameModule().getChatter();
		
		if (chatter == null) return;
		
		final Command cc = new Chatter.DisplayText(chatter, cStr.getLocalizedText());
				
		cc.execute();
		mod.getServer().sendToOthers(cc);
	}
	
}
