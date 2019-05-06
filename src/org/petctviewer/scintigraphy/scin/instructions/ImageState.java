package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.Orientation;

/**
 * Represents a state of an ImagePlus.<br>
 * The interpretation of the fields is left to the classes that uses this state.
 * 
 * @author Titouan QUÉMA
 *
 */
public class ImageState {
	/**
	 * No id defined.
	 */
	public static final int ID_NONE = -1;
	/**
	 * Id should be the same as the previous image.
	 */
	public static final int ID_PREVIOUS = -2;
	/**
	 * No slice specified.
	 */
	public static final int SLICE_PREVIOUS = 0;
	/**
	 * Lateralisation RL: Right-Left, LR: Left-Right
	 */
	public static final boolean LAT_RL = true, LAT_LR = false;

	/**
	 * Id of the image: if there is multiple images, then this ID is used to
	 * differentiate each image
	 * <ul>
	 * <li>starting at 0</li>
	 * <li>negative values mean no id</li>
	 * </ul>
	 * <br>
	 * {@link #ID_NONE} for no id
	 */
	private int idImage;
	/**
	 * Slice of the image
	 * <ul>
	 * <li>starting at 1</li>
	 * </ul>
	 * {@link #SLICE_PREVIOUS} for previous image slice
	 */
	private int slice;
	/**
	 * Facing orientation of the image. Ant or Post allowed only.
	 * <ul>
	 * <li><code>null</code> means to use the previous image orientation</li>
	 * </ul>
	 */
	private Orientation facingOrientation;
	/**
	 * Right-Left or Left-Right lateralisation. If <code>TRUE</code>: Right-Left
	 * lateralisation. If <code>FALSE</code>: Left-Right lateralisation.
	 * <ul>
	 * <li>default is <code>TRUE</code></li>
	 * </ul>
	 */
	private boolean lateralisation;

	/**
	 * Instantiates a state for an image.
	 * 
	 * @param facingOrientation Facing orientation of the image (Ant or Post only)
	 * @param slice             Number of the slice to display (if multiple slices)
	 * @param lateralisation    TRUE for a Right-Left lateralisation and FALSE for a
	 *                          Left-Right lateralisation
	 * @param idImage           ID of the image to display (index of the image in
	 *                          the model)
	 * @throws IllegalArgumentException if the facingOrientation is different than
	 *                                  Ant or Post
	 */
	public ImageState(Orientation facingOrientation, int slice, boolean lateralisation, int idImage)
			throws IllegalArgumentException {
		this.setFacingOrientation(facingOrientation);
		this.setSlice(slice);
		this.setLateralisation(lateralisation);
		this.setIdImage(idImage);
	}

	public int getIdImage() {
		return idImage;
	}

	public int getSlice() {
		return slice;
	}

	public Orientation getFacingOrientation() {
		return facingOrientation;
	}

	public boolean isLateralisationRL() {
		return lateralisation;
	}

	public boolean isLateralisationLR() {
		return !this.lateralisation;
	}

	/**
	 * @param idImage Number of the image (if multiple images)
	 */
	public void setIdImage(int idImage) {
		this.idImage = idImage;
	}

	/**
	 * @param slice Number of the slice to display (if multiple slices)
	 */
	public void setSlice(int slice) {
		this.slice = slice;
	}

	/**
	 * @param facingOrientation Facing orientation of the image (Ant or Post only)
	 * @throws IllegalArgumentException if the facingOrientation is different than
	 *                                  Ant or Post
	 */
	public void setFacingOrientation(Orientation facingOrientation) throws IllegalArgumentException {
		if (facingOrientation != Orientation.ANT && facingOrientation != Orientation.POST && facingOrientation != null)
			throw new IllegalArgumentException("The orientation " + facingOrientation
					+ " is nonsense here, it should be one of " + Orientation.ANT + ", " + Orientation.POST + "!");
		this.facingOrientation = facingOrientation;
	}

	/**
	 * @param lateralisation TRUE for a Right-Left lateralisation and FALSE for a
	 *                       Left-Right lateralisation
	 */
	public void setLateralisation(boolean lateralisation) {
		this.lateralisation = lateralisation;
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
