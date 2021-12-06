package memoir44;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.tools.FormattedString;

public class VassalTools36 {
	
	static void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString("- " + msgLine);
		final Command cc = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), cStr.getLocalizedText(cStr, msgLine));
		cc.execute();
		GameModule.getGameModule().sendAndLog(cc);
	}
	
	static void SendLine(String msgLine) {
		final GameModule mod = GameModule.getGameModule();
		
		FormattedString cStr = new FormattedString("- " + msgLine);
		final Command cc = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), cStr.getLocalizedText(cStr, msgLine));
		cc.execute();
		mod.getServer().sendToOthers(cc);
	}

}
