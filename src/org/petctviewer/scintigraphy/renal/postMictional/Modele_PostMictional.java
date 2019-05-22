package org.petctviewer.scintigraphy.renal.postMictional;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.model.ModeleScin;

public class Modele_PostMictional extends ModeleScin {

	private HashMap<String, Double> hm;

	public Modele_PostMictional(ImageSelection[] selectedImages,String studyName) {
		super(selectedImages, studyName);
	}

	@Override
	public void calculerResultats() {
		// pas de calcul
	}

	public HashMap<String, Double> getData() {
		return this.hm;
	}

	public void setData(HashMap<String, Double> hm) {
		this.hm = hm;
	}

}
