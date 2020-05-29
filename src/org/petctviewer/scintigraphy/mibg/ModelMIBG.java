package org.petctviewer.scintigraphy.mibg;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import ij.gui.Roi;

public class ModelMIBG extends ModelWorkflow {

	private static final String[] ORGANS = { "Early Heart", "Early Mediastinum", "Late Heart", "Late Mediastinum" };

	private final HashMap<String, Double> dataImage;
	private Map<Integer, Double> results;

	public ModelMIBG(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.dataImage = new HashMap<>();
		this.results = new HashMap<>();
	}

	@Override
	public void calculateResults() {
		for (int indexImage = 0; indexImage < 2; indexImage++) {
			ImageSelection ims = this.selectedImages[indexImage];
			for (int indexRoi = 0; indexRoi < 2; indexRoi++) {
				Roi roi = this.getRoiManager().getRoi(indexImage * 2 + indexRoi);
				ims.getImagePlus().setRoi((Roi) roi.clone());
				this.dataImage.put(ORGANS[indexImage * 2 + indexRoi], Library_Quantif.getAvgCounts(ims.getImagePlus()));
				System.out.println(
						ORGANS[indexImage * 2 + indexRoi] + " : " + Library_Quantif.getAvgCounts(ims.getImagePlus()));
			}
		}

		this.dataImage.put("Early H/M ratio", (this.dataImage.get(ORGANS[0]) / this.dataImage.get(ORGANS[1])));
		this.dataImage.put("Late H/M ratio", (this.dataImage.get(ORGANS[2]) / this.dataImage.get(ORGANS[3])));
		this.dataImage.put("Washout Rate",
				(this.dataImage.get("Early H/M ratio") - this.dataImage.get("Late H/M ratio"))
						/ this.dataImage.get("Early H/M ratio"));

	}

	public String[] getResults() {
		String[] results = new String[5];

		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);

		results[0] = "H/M ratio : ";
		results[1] = "\tEarly : " + us.format((this.dataImage.get("Early H/M ratio")) * 100) + "%";
		results[2] = "\tLate : " + us.format((this.dataImage.get("Late H/M ratio")) * 100) + "%";
		results[3] = "";
		results[4] = "Washout Rate : " + us.format((this.dataImage.get("Washout Rate") * 100)) + "%";

		return results;
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		Double value = this.results.get(request.getResultOn().hashCode());
        if (value == null) return null;
		// Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
		value = Unit.PERCENTAGE.convertTo(value, conversion);
		return new ResultValue(request, value, conversion);
	}

}
