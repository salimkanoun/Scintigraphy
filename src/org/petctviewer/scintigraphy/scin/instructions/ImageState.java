package org.petctviewer.scintigraphy.scin.instructions;

/**
 * Represents a state of an ImagePlus.<br>
 * If a field is set to null, then it will be interpreted as no changes.
 * 
 * @author Titouan QUÉMA
 *
 */
public class ImageState {
	/**
	 * No id defined
	 */
	public static final int ID_NONE = 0;
	/**
	 * Use the slice of the previous instruction
	 */
	public static final int SLICE_NONE = -1;

	/**
	 * Number of the image (if there is multiple images, then this ID is used to
	 * differentiate each image)
	 */
	public final int idImage;
	/**
	 * Slice of the image (starting at 1)
	 */
	public final int slice;

	public ImageState() {
		this(0);
	}

	/**
	 * Instantiate a state for an image.
	 * 
	 * @param idImage number of the image (if there is multiple images, then this ID
	 *                is used to differentiate each image)
	 */
	public ImageState(int idImage) {
		this(idImage, -1);
	}

	/**
	 * Instantiate a state for an image.
	 * 
	 * @param idImage Number of the image (if there is multiple images, then this ID
	 *                is used to differentiate each image)
	 * @param slice   Slice of the image (starting at 1) (negative value is ignored)
	 */
	public ImageState(int idImage, int slice) {
		this.idImage = idImage;
		this.slice = slice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idImage;
		result = prime * result + slice;
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
		ImageState other = (ImageState) obj;
		if (idImage != other.idImage)
			return false;
		if (slice != other.slice)
			return false;
		return true;
	}
}
