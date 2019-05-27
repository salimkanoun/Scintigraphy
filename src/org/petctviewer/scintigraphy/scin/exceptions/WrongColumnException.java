package org.petctviewer.scintigraphy.scin.exceptions;

import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;

import java.util.Arrays;

public class WrongColumnException extends WrongInputException {
	private static final long serialVersionUID = 1L;

	private final Column columnError;
	private final int rowError;

	/**
	 * @param columnError Column of the error
	 * @param rowError    Row of the error (first row is 1)
	 * @param message     Bad value on the column
	 */
	public WrongColumnException(Column columnError, int rowError, String message) {
		super("On column " + columnError.getName() + " at row " + rowError + ":\n" + message);
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

	// ORIENTATION COLUMN
	public static class OrientationColumn extends WrongColumnException {
		private static final long serialVersionUID = 1L;

		public OrientationColumn(int rowError, Orientation badProvided, Orientation[] expected) {
			super(Column.ORIENTATION, rowError, "[" + badProvided + "] is not supported. Please use "
					+ (expected.length > 1 ? "one of" : "") + ":\n" + Arrays.toString(expected));
		}

	}

}
