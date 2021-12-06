package memoir44;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;

public class M44Map extends Map {
	public void revalidateMap()
	{
		GameModule.getGameModule().warn("revalidateMap()");
		this.theMap.revalidate();
		this.repaint(true);
		this.setBoardBoundaries();
	}
}
