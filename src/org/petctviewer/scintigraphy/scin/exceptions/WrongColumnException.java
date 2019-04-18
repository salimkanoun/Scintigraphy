package org.petctviewer.scintigraphy.scin.exceptions;

import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;

public class WrongColumnException extends WrongInputException {
	private static final long serialVersionUID = 1L;

	private Column error;

	/**
	 * @param error            Column of the error
	 * @param message Bad value on the column
	 */
	public WrongColumnException(Column error, String message) {
		super("On column " + error.getName() + ": " + message);
		this.error = error;
	}

	/**
	 * @return column of the error
	 */
	public Column getColumnError() {
		return this.error;
	}

}
