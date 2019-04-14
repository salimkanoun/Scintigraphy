package org.petctviewer.scintigraphy.scin;

/**
 * Represents image orientation.
 *
 */
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

	/**
	 * @return array of all possible orientations
	 */
	public static String[] allOrientations() {
		Orientation[] o = Orientation.values();
		String[] s = new String[o.length];
		for (int i = 0; i < o.length; i++) {
			s[i] = o[i].toString();
		}
		return s;
	}

	/**
	 * Parses a string to retrieve orientation.<br>
	 * The orientation is returned if toString().equals(display).
	 * 
	 * @param display String to parse
	 * @return orientation of the string or null if no orientation matches
	 */
	public static Orientation parse(String display) {
		for (Orientation o : Orientation.values()) {
			if (o.toString().equals(display))
				return o;
		}
		return null;
	}

}
