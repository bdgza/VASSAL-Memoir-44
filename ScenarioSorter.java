package memoir44;

import java.util.Comparator;

public class ScenarioSorter implements Comparator<M44Setup> {
	public int compare(M44Setup arg0, M44Setup arg1) {
		return (arg0.getConfigureName().compareToIgnoreCase(arg1.getConfigureName()));
	}
}
