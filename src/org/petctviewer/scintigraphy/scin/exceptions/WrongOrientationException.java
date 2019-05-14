package org.petctviewer.scintigraphy.scin.exceptions;

import java.util.Arrays;

import org.petctviewer.scintigraphy.scin.Orientation;

/**
 * Thrown to indicate that the orientation is not supported.
 * 
 * @author Titouan QUÉMA
 *
 */
public class WrongOrientationException extends WrongInputException {
	private static final long serialVersionUID = 1L;

	private Orientation badOrientation;
	private Orientation[] expected;

	/**
	 * @param badProvided Orientation that is not supported
	 * @param expected    Orientations that are valid
	 */
	public WrongOrientationException(Orientation badProvided, Orientation[] expected) {
		super("Orientation [" + badProvided + "] is not supported. Please use " + (expected.length > 1 ? "one of" : "")
				+ ":\n" + Arrays.toString(expected));
		this.expected = expected;
		this.badOrientation = badProvided;
	}

	/**
	 * It is preferable to use
	 * {@link #WrongOrientationException(Orientation, Orientation[])}.
	 * 
	 * @param badProvided Orientation that is not supported
	 */
	public WrongOrientationException(Orientation badProvided) {
		super("Orientation [" + badProvided + "] is not supported");
		this.badOrientation = badProvided;
	}

	/**
	 * This method is not recommended.<br>
	 * Please use {@link #WrongOrientationException(Orientation, Orientation[])}
	 * instead.
	 * 
	 * @param msg Detail message
	 */
	public WrongOrientationException(String msg) {
		super(msg);
	}

	/**
	 * @return Orientations that are valid or null if none were provided
	 */
	public Orientation[] getExpectedOrientations() {
		return this.expected;
	}

	/**
	 * @return bad orientation that threw this exception or null if none were
	 *         provided
	 */
	public Orientation getBadOrientationProvided() {
		return this.badOrientation;
	}

}
