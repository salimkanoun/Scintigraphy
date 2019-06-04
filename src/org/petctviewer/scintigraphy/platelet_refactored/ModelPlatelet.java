package org.petctviewer.scintigraphy.platelet_refactored;

import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

public class ModelPlatelet extends ModelWorkflow {

	/**
	 * @param selectedImages Images used for this study (generally those images are used in the workflows)
	 * @param studyName Name of this study (used for display)
	 */
	public ModelPlatelet(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		return null;
	}

	@Override
	public void calculateResults() {

	}
}
