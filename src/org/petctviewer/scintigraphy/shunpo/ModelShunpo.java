package org.petctviewer.scintigraphy.shunpo;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;

import java.util.*;

public class ModelShunpo extends ModelWorkflow {

	public static final String REGION_RIGHT_LUNG = "Right Lung", REGION_LEFT_LUNG = "Left Lung", REGION_BACKGROUND =
			"Background", REGION_RIGHT_KIDNEY = "Right Kidney", REGION_LEFT_KIDNEY = "Left Kidney", REGION_BRAIN =
			"Brain";

	public static final Result RES_RATIO_RIGHT_LUNG = new Result("Right Lung Ratio"), RES_RATIO_LEFT_LUNG = new Result(
			"Left Lung Ratio"), RES_SHUNT_SYST = new Result("Shunt Systemic"), RES_PULMONARY_SHUNT = new Result(
			"Pulmonary Shunt"), RES_PULMONARY_SHUNT_2 = new Result("Pulmonary Shunt", 2);

	public static final int IMAGE_KIDNEY_LUNG = 0, IMAGE_BRAIN = 1;

	private List<Data> datas;
	private Map<Integer, Double> results;

	/**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */
	public ModelShunpo(ImageSelection[] selectedImages, String studyName) {
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

	@SuppressWarnings("unused")
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
			data.setAntValue(regionName, Data.DATA_COUNTS, counts, state, roi);
			data.setAntValue(regionName, Data.DATA_MEAN_COUNTS, Library_Quantif.getAvgCounts(imp));
			data.setAntValue(regionName, Data.DATA_PIXEL_COUNTS, Library_Quantif.getPixelNumber(imp));
		} else {
			data.setPostValue(regionName, Data.DATA_COUNTS, counts, state, roi);
			data.setPostValue(regionName, Data.DATA_MEAN_COUNTS, Library_Quantif.getAvgCounts(imp));
			data.setPostValue(regionName, Data.DATA_PIXEL_COUNTS, Library_Quantif.getPixelNumber(imp));
		}
		this.datas.add(data);
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

	@Override
	public void calculateResults() {
		// Correct kidneys with background
		// -- Ant
		// --- Right
		double counts = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS);
		double meanBkg = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_BACKGROUND, Data.DATA_MEAN_COUNTS);
		double pixels = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_KIDNEY, Data.DATA_PIXEL_COUNTS);
		double value = counts - meanBkg * pixels;
		datas.get(IMAGE_KIDNEY_LUNG).setAntValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED, value);
		// --- Left
		counts = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS);
		meanBkg = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_BACKGROUND, Data.DATA_MEAN_COUNTS);
		pixels = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_KIDNEY, Data.DATA_PIXEL_COUNTS);
		value = counts - meanBkg * pixels;
		datas.get(IMAGE_KIDNEY_LUNG).setAntValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED, value);
		// -- Post
		// --- Right
		counts = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS);
		meanBkg = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_BACKGROUND, Data.DATA_MEAN_COUNTS);
		pixels = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_KIDNEY, Data.DATA_PIXEL_COUNTS);
		value = counts - meanBkg * pixels;
		datas.get(IMAGE_KIDNEY_LUNG).setPostValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED, value);
		// --- Left
		counts = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS);
		meanBkg = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_BACKGROUND, Data.DATA_MEAN_COUNTS);
		pixels = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_KIDNEY, Data.DATA_PIXEL_COUNTS);
		value = counts - meanBkg * pixels;
		datas.get(IMAGE_KIDNEY_LUNG).setPostValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED, value);

		// Compute geometrical averages
		// == KIDNEY-LUNG ==
		Data data = datas.get(IMAGE_KIDNEY_LUNG);
		for (String regionName : this.regionsKidneyLung()) {
			double geoAvg = Library_Quantif.moyGeom(data.getAntValue(regionName, Data.DATA_COUNTS),
													data.getPostValue(regionName, Data.DATA_COUNTS));
			data.setAntValue(regionName, Data.DATA_GEO_AVG, geoAvg);
		}
		// Percentage
		double totalLung = data.getAntValue(REGION_RIGHT_LUNG, Data.DATA_GEO_AVG) + data.getAntValue(REGION_LEFT_LUNG,
																									 Data.DATA_GEO_AVG);
		this.results.put(RES_RATIO_RIGHT_LUNG.hashCode(),
						 data.getAntValue(REGION_RIGHT_LUNG, Data.DATA_GEO_AVG) / totalLung * 100.);
		this.results.put(RES_RATIO_LEFT_LUNG.hashCode(),
						 data.getAntValue(REGION_LEFT_LUNG, Data.DATA_GEO_AVG) / totalLung * 100.);

		// Calculate sum shunt
		double kidneyRight = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		double kidneyLeft = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		double brain = datas.get(IMAGE_BRAIN).getAntValue(REGION_BRAIN, Data.DATA_COUNTS);
		double ant = kidneyRight + kidneyLeft + brain;
		kidneyRight = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		kidneyLeft = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		brain = datas.get(IMAGE_BRAIN).getPostValue(REGION_BRAIN, Data.DATA_COUNTS);
		double post = kidneyRight + kidneyLeft + brain;
		double sumShunt = Library_Quantif.moyGeom(ant, post);

		// Calculate sum average
		double lungRight = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS);
		double lungLeft = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		ant = lungRight + lungLeft;
		lungRight = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS);
		lungLeft = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		post = lungRight + lungLeft;
		double sumAvg = ant + post;

		// Percentage shunt systemic
		this.results.put(RES_SHUNT_SYST.hashCode(), 100. * sumShunt / sumAvg);

		// Pulmonary shunt
		double pulmonaryShunt = (sumShunt * 100.) / (sumAvg * .38);
		System.out.println("SumShunt=" + sumShunt + ";SumAvg=" + sumAvg + ";Result=" + pulmonaryShunt);
		this.results.put(RES_PULMONARY_SHUNT.hashCode(), pulmonaryShunt);
		System.out.println("Put(" + RES_PULMONARY_SHUNT.hashCode() + "," + pulmonaryShunt + ")");

		// Pulmonary shunt - method 2
		double lungAnt = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS) + datas.get(
				IMAGE_KIDNEY_LUNG).getAntValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		double lungPost = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS) + datas.get(
				IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		double lungGeo = Library_Quantif.moyGeom(lungAnt, lungPost);

		double brainAnt = datas.get(IMAGE_BRAIN).getAntValue(REGION_BRAIN, Data.DATA_COUNTS);
		double brainPost = datas.get(IMAGE_BRAIN).getPostValue(REGION_BRAIN, Data.DATA_COUNTS);
		double brainGeo = Library_Quantif.moyGeom(brainAnt, brainPost);

		double shunt = (brainGeo / .13) / ((brainGeo / .13) + lungGeo) * 100.;
		System.out.println("BrainGeo=" + brainGeo + ";LungGeo=" + lungGeo + ";Result=" + shunt);
		this.results.put(RES_PULMONARY_SHUNT_2.hashCode(), shunt);
		System.out.println("Put(" + RES_PULMONARY_SHUNT_2.hashCode() + "," + shunt + ")");
	}
}
