package org.petctviewer.scintigraphy.scin.exceptions;

public class WrongInputException extends Exception {
	private static final long serialVersionUID = 1L;

	public WrongInputException() {
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message Detail message
	 */
	public WrongInputException(String message) {
		super(message);
	}

}
