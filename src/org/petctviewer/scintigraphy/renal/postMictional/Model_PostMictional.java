package org.petctviewer.scintigraphy.renal.postMictional;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

public class Model_PostMictional extends ModelScin {

	private HashMap<String, Double> hm;

	public Model_PostMictional(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
	}

	@Override
	public void calculateResults() {
		// pas de calcul
	}

	public HashMap<String, Double> getData() {
		return this.hm;
	}

	public void setData(HashMap<String, Double> hm) {
		this.hm = hm;
	}

}
