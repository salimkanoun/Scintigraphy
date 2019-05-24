package org.petctviewer.scintigraphy.scin.exceptions;

public class ReadTagException extends Exception {
	private static final long serialVersionUID = -5238541680298775011L;

	private String tagCode, tagName;

	/**
	 * The message passed can explain to the user what happened or what he can do to prevent this exception from
	 * occurring.
	 *
	 * @param tagCode Code of the tag missing (not null)
	 * @param tagName Readable name of the tag missing (not null)
	 * @param message Optional message giving detailed information to the user(can be null)
	 */
	public ReadTagException(String tagCode, String tagName, String message) {
		super(message);
		this.tagCode = tagCode;
		this.tagName = tagName;
	}

	/**
	 * General exception instantiation.<br>
	 * Please consider using {@link #ReadTagException(String, String, String)} instead!
	 */
	public ReadTagException(String message) {
		super(message);
	}

	public String getTagCode() {
		return this.tagCode;
	}

	public String getTagName() {
		return this.tagName;
	}

}
