package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;

public class Model_Gastric extends ModeleScin {

	public Model_Gastric(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
	}

	@Override
	public void calculerResultats() {
	}

}
