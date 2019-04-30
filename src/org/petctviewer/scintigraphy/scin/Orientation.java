package org.petctviewer.scintigraphy.scin;

import java.util.Arrays;

import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;

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

	private Orientation(String s) {
		this.s = s;
	}

	/**
	 * Converts this orientation to an Ant or Post orientation.<br>
	 * This method returns Ant for the following orientations:
	 * <ul>
	 * <li>ANT</li>
	 * <li>ANT_POST</li>
	 * <li>DYNAMIC_ANT</li>
	 * <li>DYNAMIC_ANT_POST</li>
	 * </ul>
	 * This method returns Post for the following orientations:
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
	 * </ul>
	 * 
	 * @return TRUE if this orientation is static and FALSE if this orientation is
	 *         dynamic
	 * @throws WrongOrientationException if this orientation is UNKNOWN
	 */
	public boolean isStatic() throws WrongOrientationException {
		if (this == UNKNOWN)
			throw new WrongOrientationException(UNKNOWN);

		return Arrays.stream(staticOrientations()).anyMatch(o -> o.equals(this));
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
	 * </ul>
	 * 
	 * @return TRUE if this orientation is dynamic and FALSE if this orientation is
	 *         static
	 * @throws WrongOrientationException if this orientation is UNKNOWN
	 */
	public boolean isDynamic() throws WrongOrientationException {
		return !this.isStatic();
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
	 * @return abreviation of this Orientation
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
