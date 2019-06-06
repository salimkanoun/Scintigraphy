package org.petctviewer.scintigraphy.scin.model;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

/**
 * This class provides simples methods to exchange results with the controller.
 */
public abstract class ModelWorkflow extends ModelScin {

	protected Isotope isotope;

	/**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */
	public ModelWorkflow(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
	}

	/**
	 * This method returns the result asked by the specified request.<br>
	 * The returned result <b>must</b> correspond to the
	 * request. But if the result cannot be provided (for any reason), then the returned value can be null or this
	 * method can throw a runtime exception.
	 *
	 * @param request Request of a result proposed by a model
	 * @return ResultValue containing the result requested
	 */
	public abstract ResultValue getResult(ResultRequest request);

	/**
	 * Gets the isotope selected for this model. If no isotope were selected, then this method will return null.
	 *
	 * @return isotope used in this model
	 */
	public Isotope getIsotope() {
		return this.isotope;
	}

	/**
	 * Sets the isotope used in this study
	 *
	 * @param isotope Isotope to use
	 */
	public void setIsotope(Isotope isotope) {
		this.isotope = isotope;
	}

	/**
	 * Retrieves the image described by the specified image state.<br>
	 * The image can be retrieved from this model or from this image state.
	 *
	 * @param state State describing a data
	 * @return image retrieved from the specified state (null can be returned)
	 * @throws IllegalArgumentException if the ID of the ImageState is different
	 *                                  than {@link ImageState#ID_CUSTOM_IMAGE} or a
	 *                                  positive value
	 */
	protected ImageSelection imageFromState(ImageState state) {
		if (state.getIdImage() == ImageState.ID_CUSTOM_IMAGE) return state.getImage();

		if (state.getIdImage() >= 0) return this.selectedImages[state.getIdImage()];

		throw new IllegalArgumentException("ID " + state.getIdImage() + " is not applicable here");
	}
}
