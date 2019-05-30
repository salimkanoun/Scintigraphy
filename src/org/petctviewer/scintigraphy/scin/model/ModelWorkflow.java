package org.petctviewer.scintigraphy.scin.model;

import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

/**
 * This class provides simples methods to exchange results with the controller.
 */
public abstract class ModelWorkflow extends ModelScin {

    protected Isotope isotope;

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
     * Sets the isotope used in this study
     *
     * @param isotope Isotope to use
     */
    public void setIsotope(Isotope isotope) {
        this.isotope = isotope;
    }

}
