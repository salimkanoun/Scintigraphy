package org.petctviewer.scintigraphy.scin;

import java.util.HashMap;
import java.util.Map.Entry;

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
	 * Sets the ImagePlus to the specified imp.
	 * 
	 * @param imp ImagePlus to set
	 */
	public void setImagePlus(ImagePlus imp) {
		this.imp = imp;
	}

	/**
	 * @return image orientation or UNKNOWN if no orientation was set during image
	 *         selection
	 */
	public Orientation getImageOrientation() {
		Orientation o = Orientation.parse(this.columnsValues.get(Column.ORIENTATION.getName()));
		return o == null ? Orientation.UNKNOWN : o;
	}

	/**
	 * @return row of the image in the FenSelectionDicom (from 1 to
	 *         selectedImages.length)
	 */
	public int getRow() {
		return Integer.parseInt(this.columnsValues.get(Column.ROW.getName()));
	}

	/**
	 * Sets the row of this image.
	 * 
	 * @param row Row in the FenSelectionDicom
	 */
	public void setRow(int row) {
		this.columnsValues.put(Column.ROW.getName(), row + "");
	}

	@Override
	public ImageSelection clone() {
		ImageSelection img = new ImageSelection(this.imp.duplicate(), null, null);
		// Deep copy of all values in the map
		for(Entry<String, String> e : this.columnsValues.entrySet())
			img.columnsValues.put(e.getKey(), e.getValue());
		return img;
	}
}
