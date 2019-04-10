package org.petctviewer.scintigraphy.shunpo;

public enum Orientation {

	ANT("Ant"), POST("Post"), ANT_POST("Ant/Post"), POST_ANT("Post/Ant"), DYNAMIC_ANT("Dynamic Ant"),
	DYNAMIC_POST("Dynamic Post"), DYNAMIC_ANT_POST("Dynamic A/P"), DYNAMIC_POST_ANT("Dynamic P/A"), UNKNOWN("Unknown");

	private String s;

	private Orientation(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return this.s;
	}

	public static String[] allOrientations() {
		Orientation[] o = Orientation.values();
		String[] s = new String[o.length];
		for (int i = 0; i < o.length; i++) {
			s[i] = o[i].toString();
		}
		return s;
	}

}
