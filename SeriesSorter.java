package com.memoir44.vassal;

import java.util.Comparator;

public class SeriesSorter implements Comparator<M44Series> {
	public int compare(M44Series arg0, M44Series arg1) {
		return (arg0.getConfigureName().compareToIgnoreCase(arg1.getConfigureName()));
	}
}
