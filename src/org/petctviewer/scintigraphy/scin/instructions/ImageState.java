package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.Orientation;

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
	public static final int ID_NONE = -1;
	/**
	 * Use the slice of the previous instruction
	 */
	public static final int SLICE_NONE = -1;

	/**
	 * Number of the image: if there is multiple images, then this ID is used to
	 * differentiate each image
	 * <ul>
	 * <li>starting at 0</li>
	 * <li>negative values mean no id</li>
	 * </ul>
	 * <br>
	 * {@link #ID_NONE} for no id
	 */
	public final int idImage;
	/**
	 * Slice of the image
	 * <ul>
	 * <li>starting at 1</li>
	 * <li>negative values mean to use the previous image slice</li>
	 * </ul>
	 * {@link #SLICE_NONE} for previous image slice
	 */
	public final int slice;
	/**
	 * Facing orientation of the image. Ant or Post allowed only.
	 */
	public final Orientation facingOrientation;

	public ImageState() {
		this(ID_NONE, SLICE_NONE);
	}

	/**
	 * Instantiates a state for an image.
	 * 
	 * @param idImage Number of the image (if there is multiple images, then this ID
	 *                is used to differentiate each image)
	 * @param slice   Slice of the image (starting at 1)
	 */
	public ImageState(int idImage, int slice) {
		this(null, idImage, slice);
	}
	
	/**
	 * Instantiates a state for an image.
	 * 
	 * @param facingOrientation Facing orientation of the image (Ant or Post only)
	 * @param slice             Number of the slice to display (if multiple slices)
	 * @throws IllegalArgumentException if the facingOrientation is different than
	 *                                  Ant or Post
	 */
	public ImageState(Orientation facingOrientation, int slice) {
		this(facingOrientation, ID_NONE, slice);
	}

	/**
	 * Instantiates a state for an image.
	 * 
	 * @param facingOrientation Facing orientation of the image (Ant or Post only)
	 * @param idImage           Number of the image (if multiple images)
	 * @param slice             Number of the slice to display (if multiple slices)
	 * @throws IllegalArgumentException if the facingOrientation is different than
	 *                                  Ant or Post
	 */
	public ImageState(Orientation facingOrientation, int idImage, int slice) throws IllegalArgumentException {
		if (facingOrientation != Orientation.ANT && facingOrientation != Orientation.POST && facingOrientation != null)
			throw new IllegalArgumentException("The orientation " + facingOrientation
					+ " is nonsense here, it should be one of " + Orientation.ANT + ", " + Orientation.POST + "!");

		this.facingOrientation = facingOrientation;
		this.idImage = idImage;
		this.slice = slice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facingOrientation == null) ? 0 : facingOrientation.hashCode());
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
		if (facingOrientation != other.facingOrientation)
			return false;
		if (idImage != other.idImage)
			return false;
		if (slice != other.slice)
			return false;
		return true;
	}
}
