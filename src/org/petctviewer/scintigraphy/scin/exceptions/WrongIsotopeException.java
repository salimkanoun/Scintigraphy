package org.petctviewer.scintigraphy.scin.exceptions;

import java.util.Arrays;

import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

public class WrongIsotopeException extends WrongInputException {

	private static final long serialVersionUID = 1L;

	private final Column columnError;
	private final int rowError;

	/**
	 * @param columnError Column of the error
	 * @param rowError    Row of the error (first row is 1)
	 * @param message     Bad value on the column
	 */
	public WrongIsotopeException(Column columnError, int rowError, String message) {
		super("On isotope " + columnError.getName() + " at row " + rowError + ":\n" + message);
		this.columnError = columnError;
		this.rowError = rowError;
	}

	/**
	 * @return column of the error
	 */
	public Column getColumnError() {
		return this.columnError;
	}

	/**
	 * @return row of the error
	 */
	public int getRowError() {
		return this.rowError;
	}

	// ISOTOPE COLUMN
	public static class IsotopeColumn extends WrongColumnException {
		private static final long serialVersionUID = 1L;

		public IsotopeColumn(int rowError, Isotope badProvided, Isotope[] expected) {
			super(Column.ISOTOPE, rowError,
					"[" + badProvided + "] is not supported. Please use " + (expected.length > 1 ? "one of" : "") +
							":\n" + Arrays.toString(expected));
		}

		public IsotopeColumn(int rowError, Isotope badProvided, Isotope[] expected, String hintForUser) {
			super(Column.ISOTOPE, rowError,
					"[" + badProvided + "] is not supported. Please use " + (expected.length > 1 ? "one of" : "") +
							":\n" + Arrays.toString(expected) + "\nHint: " + hintForUser);
		}

	}
}
