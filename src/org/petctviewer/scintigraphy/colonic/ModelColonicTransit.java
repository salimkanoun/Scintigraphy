package org.petctviewer.scintigraphy.colonic;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelColonicTransit extends ModelScin {

	private final HashMap<String, Double> dataImage1;

	private final HashMap<String, Double> dataImage2;

	private final HashMap<String, Double> dataImage3;

	private Isotope isotope;

	public ModelColonicTransit(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.dataImage1 = new HashMap<>();
		this.dataImage2 = new HashMap<>();
		this.dataImage3 = new HashMap<>();
	}

	@Override
	public void calculateResults() {
	}

	public void getResults() {
		ImageSelection imageReference = this.selectedImages[0];

		List<HashMap<String, Double>> datas = new ArrayList<>();
		datas.add(dataImage1);
		datas.add(dataImage2);
		datas.add(dataImage3);

		// TODO: this code is making a lot of assumptions... maybe refactor that
		for (int i = 1; i < this.selectedImages.length; i++) {
			ImageSelection imageForCalculation = this.selectedImages[i];
			this.calculateResults(imageReference, imageForCalculation, datas.get(i-1));
		}

	}

	private void calculateResults(ImageSelection imageReference, ImageSelection imageForCalculation,
			HashMap<String, Double> data) {

		imageForCalculation.getImagePlus().killRoi();

		double sumGeometricCenter = 0;
		double sum = 0;
		for (int index = 0 ; index < 6 ; index++) {
			Roi roi = this.getRoiManager().getRoi(index);
			data.put(roi.getName(), this.getGeomMean(imageForCalculation, roi));
			sumGeometricCenter += this.getGeomMean(imageForCalculation, roi) * (index + 1);
			sum += this.getGeomMean(imageForCalculation, roi);
		}

		imageReference.getImagePlus().killRoi();
		imageForCalculation.getImagePlus().killRoi();
		double decayedFirstImageCount = Library_Quantif.applyDecayFraction(imageForCalculation.getImagePlus(),
				imageReference.getImagePlus(), this.isotope);

		
		double excrementGeom = Library_Quantif.moyGeom(Library_Quantif.getCounts(imageForCalculation.getImagePlus()),
				decayedFirstImageCount);
		data.put("Excreted feces", excrementGeom);
		
		sumGeometricCenter += excrementGeom * 7;
		sum += excrementGeom;
			
		data.put("Sum", sum);

		data.put("Geometric Center", sumGeometricCenter / sum);
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

		String[] results = new String[datas[i].size()];
		
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);

		for (int j = 0; j < 6; j++) {
			results[j] = this.roiManager.getRoi((i*6 + j)).getName() + " : "
					+ us.format((datas[i].get(this.roiManager.getRoi((i*6 + j)).getName()) / datas[i].get("Sum"))*100)+"%";
		}
		
		
		results[6] = "Excreted feces : "
				+ us.format((datas[i].get("Excreted feces") / datas[i].get("Sum"))*100)+"%";
		
		results[7] = "Geometric Center : "
				+ us.format(datas[i].get("Geometric Center"));

		return results;
	}
}
