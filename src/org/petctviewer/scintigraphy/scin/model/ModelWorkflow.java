package org.petctviewer.scintigraphy.scin.model;

import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;

public abstract class ModelWorkflow extends ModeleScin {

	public ModelWorkflow(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
	}
	
	public abstract ResultValue getResult(ResultRequest request);

}
