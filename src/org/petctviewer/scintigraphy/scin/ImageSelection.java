package org.petctviewer.scintigraphy.scin;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents an image selected by the user.<br>
 * This class enhance an ImagePlus by adding some key-values properties.
 * 
 * @author Titouan QUÉMA
 *
 */
public class ImageSelection implements Cloneable {
	private ImagePlus imp;
	private final HashMap<String, String> columnsValues;

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

	public Set<String> getColumns() {
		return columnsValues.keySet();
	}

	public Collection<String> getValues() {
		return columnsValues.values();
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
	 * @see Library_Dicom#getDateAcquisition
	 * @return date of acquisition of this image
	 */
	public Date getDateAcquisition() {
		return Library_Dicom.getDateAcquisition(imp);
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
	 * @return image isotope or UNKNOWN if no isotope was set during image
	 *         selection
	 */
	public Isotope getImageIsotope() {
		Isotope i = Isotope.parse(this.columnsValues.get(Column.ISOTOPE.getName()));
		return i == null ? Isotope.UNKNOWN : i;
	}
	
	/**
	 * @return image organ in a string or UNKNOWN if no organ was set during image
	 *         selection
	 */
	public String getImageOrgan() {
		String i = this.columnsValues.get(Column.ORGAN.getName());
		return i == null ? "UNKNOWN" : i;
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
		for (Entry<String, String> e : this.columnsValues.entrySet())
			img.columnsValues.put(e.getKey(), e.getValue());
		return img;
	}

	public ImageSelection clone(Orientation o) {
		ImageSelection img = this.clone();
		img.columnsValues.replace(Column.ORIENTATION.getName(), o.toString());
		return img;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnsValues == null) ? 0 : columnsValues.hashCode());
		result = prime * result + ((imp == null) ? 0 : imp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageSelection other = (ImageSelection) obj;
		if (columnsValues == null) {
			if (other.columnsValues != null)
				return false;
		} else if (!columnsValues.equals(other.columnsValues))
			return false;
		if (imp == null) {
			return other.imp == null;
		} else return imp.equals(other.imp);
	}
	
	public void close() {
		this.getImagePlus().close();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Image: ");
		s.append(imp.getTitle());
		s.append('\n');
		for (Entry<String, String> entry : this.columnsValues.entrySet()) {
			s.append("[");
			s.append(entry.getKey());
			s.append("] --> ");
			s.append(entry.getValue());
			s.append('\n');
		}
		return s.toString();
	}
}
