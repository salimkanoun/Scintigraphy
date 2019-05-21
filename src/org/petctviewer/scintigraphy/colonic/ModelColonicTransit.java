package org.petctviewer.scintigraphy.colonic;

import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.gui.Roi;

public class ModelColonicTransit extends ModeleScin {

	private HashMap<String, Double> dataImage1;

	private HashMap<String, Double> dataImage2;

	private HashMap<String, Double> dataImage3;

	private Isotope isotope;

	public ModelColonicTransit(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		// TODO Auto-generated method stub

		this.dataImage1 = new HashMap<>();
		this.dataImage2 = new HashMap<>();
		this.dataImage3 = new HashMap<>();
	}

	@Override
	public void calculerResultats() {
		// TODO Auto-generated method stub

	}

	public void getResults() {
		// TODO Auto-generated method stub

		ImageSelection imageReference = this.selectedImages[0];

		Map<String, Double>[] datas = new HashMap[] { dataImage1, dataImage2, dataImage3 };

		for (int i = 0; i < this.selectedImages.length; i++) {
			ImageSelection imageForCalculation = this.selectedImages[i];
			this.calculateResults(imageReference, imageForCalculation, (HashMap<String, Double>) datas[i]);
		}

	}

	private void calculateResults(ImageSelection imageReference, ImageSelection imageForCalculation,
			HashMap<String, Double> data) {

		imageForCalculation.getImagePlus().killRoi();

		int index = 1;
		for (Roi roi : this.getRoiManager().getRoisAsArray()) {
			data.put(roi.getName(), (this.getGeomMean(imageForCalculation, roi) * index));
			index++;
		}

		imageReference.getImagePlus().killRoi();
		double decayedFirstImageCount = Library_Quantif.applyDecayFraction(imageForCalculation.getImagePlus(),
				imageReference.getImagePlus(), this.isotope);

		imageForCalculation.getImagePlus().killRoi();

		double excrementGeom = Library_Quantif.moyGeom(Library_Quantif.getCounts(imageForCalculation.getImagePlus()),
				decayedFirstImageCount);

		data.put("Excreted feces", (excrementGeom * index));

		double sum = 0;
		for (double values : data.values())
			sum += values;
		
		data.put("Sum", sum);

		data.put("Geometric Center", (sum / decayedFirstImageCount));
	}

	private double getGeomMean(ImageSelection ims, Roi roi) {

		ims.getImagePlus().setRoi((Roi) roi.clone());

		ims.getImagePlus().setSlice(1);
		double countAnt = Library_Quantif.getCounts(ims.getImagePlus());

		ims.getImagePlus().setSlice(2);
		ims.getImagePlus().setRoi((Roi) roi.clone());
		double countPost = Library_Quantif.getCounts(ims.getImagePlus());

		return Library_Quantif.moyGeom(countAnt, countPost);
	}

	public void setIsotope(Isotope isotope) {
		this.isotope = isotope;
	}

	public String[] getResults(int i) {
		Map<String, Double>[] datas = new HashMap[] { dataImage1, dataImage2, dataImage3 };

		String[] results = new String[this.roiManager.getCount()];

		for (int j = 0; j < this.roiManager.getCount() - 1; j++) {
			results[j] = this.roiManager.getRoi(j).getName() + " : "
					+ (datas[i].get(this.roiManager.getRoi(j).getName()) / datas[i].get("Sum"));
		}
		
		results[this.roiManager.getCount() - 1] = this.roiManager.getRoi(this.roiManager.getCount() - 1).getName() + " : "
				+ datas[i].get(this.roiManager.getRoi(this.roiManager.getCount() - 1).getName());

		return results;
	}
}
