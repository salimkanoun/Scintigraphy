package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

public class ModelShunpo_refactored extends ModelWorkflow {

	/**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */
	public ModelShunpo_refactored(ImageSelection[] selectedImages, String studyName) {
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
