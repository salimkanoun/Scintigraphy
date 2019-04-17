package org.petctviewer.scintigraphy.scin;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;

import ij.ImagePlus;

/**
 * Represents an image selected by the user.<br>
 * This class enhance an ImagePlus by adding some key-values properties.
 * 
 * @author Titouan QUÉMA
 *
 */
public class ImageSelection implements Cloneable {
	private ImagePlus imp;
	private HashMap<String, String> columnsValues;

	/**
	 * Creates a selected image.<br>
	 * The key[i] must have the value[i].
	 * 
	 * @param imp    Image selected
	 * @param keys   Columns specified by the table
	 * @param values Values for each column
	 * @throws IllegalArgumentException if the keys and values don't have the same
	 *                                  length
	 */
	public ImageSelection(ImagePlus imp, String[] keys, String[] values) {
		this.imp = imp;
		this.columnsValues = new HashMap<>();
		
		if (keys == null || values == null) {
			return;
		}
		if (keys.length != values.length)
			throw new IllegalArgumentException("Arrays size must be equals! (" + keys.length + " != " + values.length);
		for (int i = 0; i < keys.length; i++) {
			this.columnsValues.put(keys[i], values[i]);
		}
	}

	/**
	 * @param col Name of the column to get the value from
	 * @return value of the specified column
	 */
	public String getValue(String col) {
		return this.columnsValues.get(col);
	}

	/**
	 * @return selected image
	 */
	public ImagePlus getImagePlus() {
		return this.imp;
	}

	/**
	 * @return image orientation or UNKNOWN if no orientation was set during image
	 *         selection
	 */
	public Orientation getImageOrientation() {
		Orientation o = Orientation.parse(this.columnsValues.get(Column.ORIENTATION.getName()));
		return o == null ? Orientation.UNKNOWN : o;
	}

	@Override
	protected ImageSelection clone() {
		ImageSelection img = new ImageSelection(this.imp.duplicate(), null, null);
		img.columnsValues = this.columnsValues;
		return img;
	}
}
