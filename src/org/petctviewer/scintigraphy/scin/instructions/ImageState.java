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
	 * Orientation of the image.
	 */
	public final Orientation orientation;
	public final int idImage;

	public ImageState() {
		this(null, 0);
	}

	/**
	 * Instantiate a state for an image.
	 * 
	 * @param orientation Orientation the image should have (expecting ANT or POST
	 *                    only)
	 * @throw {@link IllegalArgumentException} if the orientation is different from
	 *        ANT or POST
	 */
	public ImageState(Orientation orientation) throws IllegalArgumentException {
		this(orientation, 0);
	}

	/**
	 * Instantiate a state for an image.
	 * 
	 * @param orientation Orientation the image should have (expecting ANT or POST
	 *                    only)
	 * @param idImage     number of the image (if there is multiple images, then
	 *                    this ID is used to differentiate each image)
	 * @throw {@link IllegalArgumentException} if the orientation is different from
	 *        ANT or POST
	 */
	public ImageState(Orientation orientation, int idImage) throws IllegalArgumentException {
		if (orientation != null && orientation != Orientation.ANT && orientation != Orientation.POST)
			throw new IllegalArgumentException("The orientation " + orientation + " is nonsense here!");
		this.orientation = orientation;
		this.idImage = idImage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idImage;
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
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
		if (orientation != other.orientation)
			return false;
		return true;
	}
}
