package org.petctviewer.scintigraphy.colonic;

import java.util.HashMap;

import org.petctviewer.scintigraphy.cardiac.ControllerWorkflowCardiac;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.gui.Roi;

public class ModelColonicTransit extends ModeleScin {
	
	private HashMap<String, Double> data;
	
	private HashMap<String, String> resultats;

	public ModelColonicTransit(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.resultats = new HashMap<>();
		this.data = new HashMap<>();
	}

	
	@Override
	public void calculerResultats() {
		// TODO Auto-generated method stub

	}
	
	
	public void getResults() {

		int index = 1;
		for (Roi roi : this.getRoiManager().getRoisAsArray()) {
			this.selectedImages[0].getImagePlus().setRoi((Roi) roi.clone());

			Double counts = new Double(0.0d);
			counts = Library_Quantif.getCounts(this.selectedImages[0].getImagePlus()) * index;
			
			this.data.put(roi.getName(), counts);
			index++;
		}
		
		double sum = 0;
		for(double values : data.values())
			sum+=values;
		
		this.data.put("Finale", sum);

	}

}
