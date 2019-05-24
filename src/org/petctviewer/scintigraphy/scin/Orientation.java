package org.petctviewer.scintigraphy.scin;

import java.util.Arrays;

/**
 * Represents image orientation.
 *
 */
public enum Orientation {

	ANT("Ant"),
	POST("Post"),
	ANT_POST("Ant/Post"),
	POST_ANT("Post/Ant"),
	DYNAMIC_ANT("Dynamic Ant"),
	DYNAMIC_POST("Dynamic Post"),
	DYNAMIC_ANT_POST("Dynamic A/P"),
	DYNAMIC_POST_ANT("Dynamic P/A"),
	UNKNOWN("Unknown");

	private String s;

	Orientation(String s) {
		this.s = s;
	}

	/**
	 * Converts this orientation to an Ant or Post orientation.<br>
	 * This method returns <b>Ant</b> for the following orientations:
	 * <ul>
	 * <li>ANT</li>
	 * <li>ANT_POST</li>
	 * <li>DYNAMIC_ANT</li>
	 * <li>DYNAMIC_ANT_POST</li>
	 * </ul>
	 * This method returns <b>Post</b> for the following orientations:
	 * <ul>
	 * <li>POST</li>
	 * <li>POST_ANT</li>
	 * <li>DYNAMIC_POST</li>
	 * <li>DYNAMIC_POST_ANT</li>
	 * </ul>
	 * This method returns UNKNOWN for any other orientation.
	 * 
	 * @return Ant or Post orientation
	 */
	public Orientation getFacingOrientation() {
		switch (this) {
		case ANT:
		case ANT_POST:
		case DYNAMIC_ANT:
		case DYNAMIC_ANT_POST:
			return ANT;
		case DYNAMIC_POST:
		case POST:
		case DYNAMIC_POST_ANT:
		case POST_ANT:
			return POST;
		default:
			return UNKNOWN;
		}
	}

	/**
	 * Checks if the orientation is static.<br>
	 * This method returns TRUE for the following orientations:
	 * <ul>
	 * <li>ANT</li>
	 * <li>POST</li>
	 * <li>ANT_POST</li>
	 * <li>POST_ANT</li>
	 * </ul>
	 * This method returns FALSE for the following orientations:
	 * <ul>
	 * <li>DYNAMIC_ANT</li>
	 * <li>DYNAMIC_POST</li>
	 * <li>DYNAMIC_ANT_POST</li>
	 * <li>DYNAMIC_POST_ANT</li>
	 * <li>UNKNOWN</li>
	 * </ul>
	 * 
	 * @return TRUE if this orientation is static and FALSE if this orientation is
	 *         dynamic or Unknown
	 */
	public boolean isStatic() {
		return Arrays.asList(staticOrientations()).contains(this);
	}

	/**
	 * Checks if the orientation is dynamic.<br>
	 * This method returns TRUE for the following orientations:
	 * <ul>
	 * <li>DYNAMIC_ANT</li>
	 * <li>DYNAMIC_POST</li>
	 * <li>DYNAMIC_ANT_POST</li>
	 * <li>DYNAMIC_POST_ANT</li>
	 * </ul>
	 * This method returns FALSE for the following orientations:
	 * <ul>
	 * <li>ANT</li>
	 * <li>POST</li>
	 * <li>ANT_POST</li>
	 * <li>POST_ANT</li>
	 * <li>UNKNOWN</li>
	 * </ul>
	 * 
	 * @return TRUE if this orientation is dynamic and FALSE if this orientation is
	 *         static
	 */
	public boolean isDynamic() {
		return Arrays.asList(dynamicOrientations()).contains(this);
	}

	/**
	 * This method will return the inverse of an Orientation with the following
	 * rules:<br>
	 * 
	 * <pre>
	 * ANT <-> POST
	 * ANT_POST <-> POST_ANT
	 * DYNAMIC_ANT_POST <-> DYNAMIC_POST_ANT
	 * DYNAMIC_ANT <-> DYNAMIC_POST
	 * UNKNOWN <-> UNKNOWN
	 * </pre>
	 * 
	 * @return inverse of the Orientation
	 */
	public Orientation invert() {
		switch (this) {
		case ANT:
			return POST;
		case POST:
			return ANT;
		case ANT_POST:
			return POST_ANT;
		case POST_ANT:
			return ANT_POST;
		case DYNAMIC_ANT:
			return DYNAMIC_POST;
		case DYNAMIC_POST:
			return DYNAMIC_ANT;
		case DYNAMIC_ANT_POST:
			return DYNAMIC_POST_ANT;
		case DYNAMIC_POST_ANT:
			return DYNAMIC_ANT_POST;
		default:
			return UNKNOWN;
		}
	}

	/**
	 * The abbreviation is set according to the following rules:
	 * <table border=1>
	 * <tr>
	 * <td><code>ANT</code></td>
	 * <td>A</td>
	 * </tr>
	 * <tr>
	 * <td><code>POST</code></td>
	 * <td>P</td>
	 * </tr>
	 * <tr>
	 * <td><code>ANT_POST</code></td>
	 * <td>A/P</td>
	 * </tr>
	 * <tr>
	 * <td><code>POST_ANT</code></td>
	 * <td>P/A</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_ANT</code></td>
	 * <td>DynA</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_POST</code></td>
	 * <td>DynP</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_ANT_POST</code></td>
	 * <td>DynA/P</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_POST_ANT</code></td>
	 * <td>DynP/A</td>
	 * </tr>
	 * <tr>
	 * <td>All other orientations</td>
	 * <td>Unknown</td>
	 * </tr>
	 * </table>
	 * 
	 * @return abbreviation of this Orientation
	 */
	public String abrev() {
		switch (this) {
		case ANT:
			return "A";
		case POST:
			return "P";
		case ANT_POST:
			return "A/P";
		case POST_ANT:
			return "P/A";
		case DYNAMIC_ANT:
			return "DynA";
		case DYNAMIC_POST:
			return "DynP";
		case DYNAMIC_ANT_POST:
			return "DynA/P";
		case DYNAMIC_POST_ANT:
			return "DynP/A";
		default:
			return "Unknown";
		}
	}

	/**
	 * Returns a string matching this orientation. The following rules are applied:
	 * <table border=1>
	 * <tr>
	 * <td><code>ANT</code></td>
	 * <td>Ant</td>
	 * </tr>
	 * <tr>
	 * <td><code>POST</code></td>
	 * <td>Post</td>
	 * </tr>
	 * <tr>
	 * <td><code>ANT_POST</code></td>
	 * <td>Ant/Post</td>
	 * </tr>
	 * <tr>
	 * <td><code>POST_ANT</code></td>
	 * <td>Post/Ant</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_ANT</code></td>
	 * <td>Dynamic Ant</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_POST</code></td>
	 * <td>Dynamic Post</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_ANT_POST</code></td>
	 * <td>Dynamic A/P</td>
	 * </tr>
	 * <tr>
	 * <td><code>DYNAMIC_POST_ANT</code></td>
	 * <td>Dynamic P/A</td>
	 * </tr>
	 * <tr>
	 * <td>All other orientations</td>
	 * <td>Unknown</td>
	 * </tr>
	 * </table>
	 */
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
	 * @return Post and Ant orientation (in this order)
	 */
	public static Orientation[] postAntOrder() {
		return new Orientation[] { Orientation.POST, Orientation.ANT };
	}

	/**
	 * @return Ant and Post orientation (in this order)
	 */
	public static Orientation[] antPostOrder() {
		return new Orientation[] { Orientation.ANT, Orientation.POST };
	}

	/**
	 * @return array of all static orientations
	 */
	public static Orientation[] staticOrientations() {
		return new Orientation[] { Orientation.ANT, Orientation.POST, Orientation.ANT_POST, Orientation.POST_ANT };
	}

	/**
	 * @return array of all dynamic orientations
	 */
	public static Orientation[] dynamicOrientations() {
		return new Orientation[] { Orientation.DYNAMIC_ANT, Orientation.DYNAMIC_POST, Orientation.DYNAMIC_ANT_POST,
				Orientation.DYNAMIC_POST_ANT };
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
