package org.petctviewer.scintigraphy.scin.exceptions;

public class WrongNumberImagesException extends WrongInputException {
	private static final long serialVersionUID = 1L;

	private final int provided;
	private final int minRequired;
	private int maxRequired;

	/**
	 * @param provided Number of images provided
	 * @param required Exact number of images required
	 */
	public WrongNumberImagesException(int provided, int required) {
		super("Can only accept " + required + " images. (" + provided + " provided)");
		this.provided = provided;
		this.minRequired = required;
		this.maxRequired = required;
	}

	/**
	 * @param provided    Number of images provided
	 * @param minRequired Minimum number of images required
	 * @param maxRequired Maximum number of images required
	 */
	public WrongNumberImagesException(int provided, int minRequired, int maxRequired) {
		super("Can only accept " + minRequired + " to " + (maxRequired == Integer.MAX_VALUE ? "Infinity" : maxRequired)
				+ " images. (" + provided + " provided)");
		this.provided = provided;
		this.maxRequired = maxRequired;
		this.minRequired = minRequired;
	}

	/**
	 * It is preferable to use {@link #WrongNumberImagesException(int, int)}.
	 * 
	 * @param provided Number of images provided
	 */
	public WrongNumberImagesException(int provided) {
		super("Cannot accept " + provided + "images");
		this.provided = provided;
		this.minRequired = -1;
		this.maxRequired = -1;
	}

	/**
	 * This method is not recommended.<br>
	 * Please use {@link #WrongNumberImagesException(int, int)} instead.
	 * 
	 * @param message Detail message
	 */
	public WrongNumberImagesException(String message) {
		super(message);
		this.provided = -1;
		this.minRequired = -1;
	}

	/**
	 * @return minimum number of images required or -1 if none provided
	 */
	public int getMinNumberRequired() {
		return this.minRequired;
	}

	/**
	 * @return maximum number of images required or -1 if none provided
	 */
	public int getMaxNumberRequired() {
		return this.maxRequired;
	}

	/**
	 * @return number of images provided that causes this exception to be thrown or
	 *         -1 if none provided
	 */
	public int getNumberProvided() {
		return this.provided;
	}

}
