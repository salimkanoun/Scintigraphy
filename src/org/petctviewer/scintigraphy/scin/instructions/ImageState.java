package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ImageSelection;
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
	 * Specifies that no id is defined.<br>
	 * The interpretation can vary, please refer to the method or the class that use
	 * this ImageState.
	 */
	public static final int ID_NONE = -1;
	/**
	 * Specifies that the ID used should be the same as the previous image.<br>
	 * If the previous ID was {@link #ID_CUSTOM_IMAGE} then the custom image should
	 * be displayed.
	 */
	public static final int ID_PREVIOUS = -2;
	/**
	 * Specifies that the ID should not be used because an image is specified.
	 */
	public static final int ID_CUSTOM_IMAGE = -3;
	/**
	 * Specifies that the slice should be the same as the previous image.
	 */
	public static final int SLICE_PREVIOUS = 0;
	/**
	 * Specifies a Right-Left lateralisation.
	 */
	public static final boolean LAT_RL = true;
	/**
	 * Specifies a Left-Right lateralisation.
	 */
	public static final boolean LAT_LR = false;

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
	 * Image specified
	 */
	private ImageSelection image;

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

	/**
	 * If the image to be used is not in the model, then this method should return
	 * {@link #ID_CUSTOM_IMAGE} and the {@link #getImage()} method should be used to
	 * know which image to use.
	 * 
	 * @return ID of the image to be used (the image is taken from the model)
	 */
	public int getIdImage() {
		return idImage;
	}

	/**
	 * @return index of the slice to be used
	 */
	public int getSlice() {
		return slice;
	}

	/**
	 * @return facing orientation (Ant or Post)
	 * @see Orientation#getFacingOrientation()
	 */
	public Orientation getFacingOrientation() {
		return facingOrientation;
	}

	/**
	 * @return TRUE if the lateralisation is Right-Left and FALSE if the
	 *         lateralisation is Left-Right
	 */
	public boolean isLateralisationRL() {
		return lateralisation;
	}

	/**
	 * @return TRUE if the lateralisation is Left-Right and FALSE if the
	 *         lateralisation is Right-Left
	 */
	public boolean isLateralisationLR() {
		return !this.lateralisation;
	}
	
	public boolean getLateralisation() {
		return this.lateralisation;
	}

	/**
	 * @return image specified or null if none
	 */
	public ImageSelection getImage() {
		return this.image;
	}

	/**
	 * Specifies an image to be used.
	 * 
	 * @param image Image to use
	 */
	public void specifieImage(ImageSelection image) {
		this.image = image;
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
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + (lateralisation ? 1231 : 1237);
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
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (lateralisation != other.lateralisation)
			return false;
		if (slice != other.slice)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String idImage;
		if (this.idImage == ID_CUSTOM_IMAGE)
			idImage = "CUSTOM_IMAGE";
		else if (this.idImage == ID_NONE)
			idImage = "NONE";
		else if (this.idImage == ID_PREVIOUS)
			idImage = "PREVIOUS";
		else
			idImage = this.idImage + "";

		String slice;
		if (this.slice == SLICE_PREVIOUS)
			slice = "PREVIOUS";
		else
			slice = this.slice + "";

		String lateralisation;
		if (this.lateralisation == LAT_LR)
			lateralisation = "LEFT-RIGHT";
		else
			lateralisation = "RIGHT-LEFT";
		
		String imageName;
		if(this.image != null)
			imageName = (this.image.getImagePlus() != null ? this.image.getImagePlus().getTitle() : "// NO IMAGE PLUS//");
		else
			imageName = "NO-IMAGE";

		return "ImageState [idImage=" + idImage + ",\n\tslice=" + slice + ",\n\tfacingOrientation=" + facingOrientation
				+ ",\n\tlateralisation=" + lateralisation + ",\n\timage=" + imageName + "]";
	}
	
	@Override
	public ImageState clone() {
		ImageState clone = new ImageState(facingOrientation, SLICE_PREVIOUS, lateralisation, idImage);
		clone.specifieImage(image);
		return clone;
	}
}
