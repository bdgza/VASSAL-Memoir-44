package memoir44;

import VASSAL.build.Buildable;
import VASSAL.build.module.ChartWindow;

public class M44CampaignChartWindow extends ChartWindow {
	public void addTo(Buildable b) {
		launch.setName(name);
		
		super.addTo(b);
	  }
}
