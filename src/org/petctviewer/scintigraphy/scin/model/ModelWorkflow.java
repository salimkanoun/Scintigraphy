package org.petctviewer.scintigraphy.scin.model;

import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;

/**
 * This class provides simples methods to exchange results with the controller.
 */
public abstract class ModelWorkflow extends ModelScin {

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

}
