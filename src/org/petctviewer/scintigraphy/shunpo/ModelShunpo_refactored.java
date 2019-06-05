package org.petctviewer.scintigraphy.shunpo;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.gastric.Result;
import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.gastric.Unit;
import org.petctviewer.scintigraphy.platelet_refactored.Data;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

import java.util.*;

public class ModelShunpo_refactored extends ModelWorkflow {

	public static final String REGION_RIGHT_LUNG = "Right Lung", REGION_LEFT_LUNG = "Left Lung", REGION_BACKGROUND =
			"Background", REGION_RIGHT_KIDNEY = "Right Kidney", REGION_LEFT_KIDNEY = "Left Kidney", REGION_BRAIN =
			"Brain";

	public static final Result RES_RATIO_RIGHT_LUNG = new Result("Right Lung Ratio"), RES_RATIO_LEFT_LUNG = new Result(
			"Left Lung Ratio"), RES_SHUNT_SYST = new Result("Shunt Systemic"), RES_PULMONARY_SHUNT = new Result(
			"Pulmonary Shunt");

	private static final int IMAGE_KIDNEY_LUNG = 0, IMAGE_BRAIN = 1;

	private List<Data> datas;
	private Map<Integer, Double> results;

	/**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */
	public ModelShunpo_refactored(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		this.datas = new LinkedList<>();
		this.results = new HashMap<>();
	}

	private Data createOrRetrieveData(ImageState state) {
		Data data = this.datas.stream().filter(d -> d.getAssociatedImage() == state.getImage()).findFirst().orElse(
				null);
		if (data == null) {
			Date time0 = (this.datas.size() > 0 ? this.datas.get(0).getAssociatedImage().getDateAcquisition() :
					state.getImage().getDateAcquisition());
			data = new Data(state, Library_Quantif.calculateDeltaTime(time0, state.getImage().getDateAcquisition()));
		}
		return data;
	}

	private String[] allRegions() {
		return new String[]{REGION_RIGHT_LUNG, REGION_LEFT_LUNG, REGION_RIGHT_KIDNEY, REGION_LEFT_KIDNEY,
				REGION_BACKGROUND, REGION_BRAIN};
	}

	private String[] regionsKidneyLung() {
		return new String[]{REGION_RIGHT_LUNG, REGION_LEFT_LUNG, REGION_RIGHT_KIDNEY, REGION_LEFT_KIDNEY,
				REGION_BACKGROUND};
	}

	/**
	 * This method takes care of all necessary operations to do on the ImagePlus or the RoiManager. This requires the
	 * state to contain all of the required data.
	 *
	 * @param regionName Region to calculate
	 * @param state      State the image must be to do the calculations
	 * @param roi        Region where the calculates must be made
	 */
	public void addData(String regionName, ImageState state, Roi roi) {
		// Save the image in the state
		state.specifieImage(this.imageFromState(state));
		state.setIdImage(ImageState.ID_CUSTOM_IMAGE);

		ImagePlus imp = state.getImage().getImagePlus();

		// Prepare image
		imp.setSlice(state.getSlice());
		imp.setRoi(roi);

		// Calculate counts
		double counts = Library_Quantif.getCounts(imp);

		Data data = this.createOrRetrieveData(state);
		if (state.getFacingOrientation() == Orientation.ANT) {
			data.setAntValue(regionName, Data.DATA_ANT_COUNTS, counts, state, roi);
		} else {
			data.setPostValue(regionName, Data.DATA_POST_COUNTS, counts, state, roi);
		}
		this.datas.add(data);
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		Double value = this.results.get(request.getResultOn().hashCode());
		if (value == null) return null;
		// Convert result to requested unit
		value = Unit.PERCENTAGE.convertTo(value, request.getUnit());
		return new ResultValue(request, value, request.getUnit());
	}

	@Override
	public void calculateResults() {
		// Compute geometrical averages
		// == KIDNEY-LUNG ==
		Data data = datas.get(IMAGE_KIDNEY_LUNG);
		for (String regionName : this.regionsKidneyLung()) {
			double geoAvg = Library_Quantif.moyGeom(data.getAntValue(regionName, Data.DATA_ANT_COUNTS),
													data.getPostValue(regionName, Data.DATA_POST_COUNTS));
			data.setAntValue(regionName, Data.DATA_GEO_AVG, geoAvg);
		}
		// Percentage
		double sumAvg = data.getAntValue(REGION_RIGHT_LUNG, Data.DATA_GEO_AVG) + data.getAntValue(REGION_LEFT_LUNG,
																								  Data.DATA_GEO_AVG);
		this.results.put(RES_RATIO_RIGHT_LUNG.hashCode(),
						 data.getAntValue(REGION_RIGHT_LUNG, Data.DATA_GEO_AVG) / sumAvg * 100.);
		this.results.put(RES_RATIO_LEFT_LUNG.hashCode(),
						 data.getAntValue(REGION_LEFT_LUNG, Data.DATA_GEO_AVG) / sumAvg * 100.);

		// == BRAIN ==
		data = datas.get(IMAGE_BRAIN);
		data.setAntValue(REGION_BRAIN, Data.DATA_GEO_AVG,
						 Library_Quantif.moyGeom(data.getAntValue(REGION_BRAIN, Data.DATA_ANT_COUNTS),
												 data.getPostValue(REGION_BRAIN, Data.DATA_POST_COUNTS)));

		// Percentage shunt systemic
		double sumShunt = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_KIDNEY, Data.DATA_GEO_AVG) + datas.get(
				IMAGE_KIDNEY_LUNG).getAntValue(REGION_LEFT_KIDNEY, Data.DATA_GEO_AVG) + datas.get(
				IMAGE_BRAIN).getAntValue(REGION_BRAIN, Data.DATA_GEO_AVG);
		this.results.put(RES_SHUNT_SYST.hashCode(), 100. * sumShunt / sumAvg);

		// Pulmonary shunt
		double pulmonaryShunt = (sumShunt * 100.) / (sumAvg * .38);
		this.results.put(RES_PULMONARY_SHUNT.hashCode(), pulmonaryShunt);
	}
}
