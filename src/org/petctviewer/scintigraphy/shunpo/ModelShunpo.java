package org.petctviewer.scintigraphy.shunpo;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabShunpo;

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

	/**
	 * Retrieves the data associated with the specified state of image. If no data exists, then it will be created.
	 *
	 * @param state State of the image associated with the data (not null)
	 * @return data previously saved or new data
	 */
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

	/**
	 * Returns the regions for the kidney-lung image.
	 *
	 * @return array of regions name of the kidney-lung image
	 */
	private String[] regionsKidneyLung() {
		return new String[]{REGION_RIGHT_LUNG, REGION_LEFT_LUNG, REGION_RIGHT_KIDNEY, REGION_LEFT_KIDNEY,
							REGION_BACKGROUND};
	}

	/**
	 * Corrects the value of the specified region with the background region. To use this method, the background noise
	 * region <b>must</b> be set.
	 *
	 * @param regionName Name of the region to correct
	 * @param post       If set to TRUE, the Post orientation of the region will be used. If set to FALSE, the Ant
	 *                   orientation of the region will be used.
	 * @return corrected value of the region
	 */
	private double correctValueWithBkgNoise(String regionName, boolean post) {
		double counts, meanBkg, pixels;
		if (post) {
			counts = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(regionName, Data.DATA_COUNTS);
			meanBkg = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_BACKGROUND, Data.DATA_MEAN_COUNTS);
			pixels = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(regionName, Data.DATA_PIXEL_COUNTS);
		} else {
			counts = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(regionName, Data.DATA_COUNTS);
			meanBkg = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_BACKGROUND, Data.DATA_MEAN_COUNTS);
			pixels = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(regionName, Data.DATA_PIXEL_COUNTS);
		}
		return counts - meanBkg * pixels;
	}

	/**
	 * The sum of the shunts is calculated by adding the counts of the right kidney, the left kidney and the brain for
	 * each orientation (Ant and Post) and then by computing the geometrical mean on those values.
	 *
	 * @return <code>GeoMean( (R-KIDNEY + L-KIDNEY + BRAIN)ant ; (R-KIDNEY + L-KIDNEY + BRAIN)post )</code>
	 */
	private double calculateSumShunts() {
		System.out.println("Sum Shunts: MG(right_kidney_ant + left_kidney_ant + brain_ant ; right_kidney_post + " +
								   "left_kidney_post + brain_post)");
		double kidneyRight = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		double kidneyLeft = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		double brain = datas.get(IMAGE_BRAIN).getAntValue(REGION_BRAIN, Data.DATA_COUNTS);
		double ant = kidneyRight + kidneyLeft + brain;
		System.out.print("Sum Shunts: MG(" + kidneyRight + " + " + kidneyLeft + " + " + brain + " ; ");
		kidneyRight = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		kidneyLeft = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED);
		brain = datas.get(IMAGE_BRAIN).getPostValue(REGION_BRAIN, Data.DATA_COUNTS);
		double post = kidneyRight + kidneyLeft + brain;
		System.out.println(kidneyRight + " + " + kidneyLeft + " + " + brain + ")");
		System.out.println("Sum Shunts: MG(" + ant + " ; " + post + ")");
		System.out.println("Sum Shunts = " + Library_Quantif.moyGeom(ant, post));
		return Library_Quantif.moyGeom(ant, post);
	}

	/**
	 * The sum of the lungs is calculated by adding the counts of the right lung with the left lung for each
	 * orientation
	 * (Ant and Post) and then by computing the geometrical mean on those values.
	 *
	 * @return <code>GeoMean( (R-LUNG + L-LUNG)ant ; (R-LUNG + L-LUNG)post )</code>
	 */
	private double calculateSumLungs() {
		System.out.println("Sum Lungs: MG(right_lung_ant + left_lung_ant ; right_lung_post + left_lung_post)");
		double lungRight = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS);
		double lungLeft = datas.get(IMAGE_KIDNEY_LUNG).getAntValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		double ant = lungRight + lungLeft;
		System.out.print("Sum Lungs: MG(" + lungRight + " + " + lungLeft + " ; ");
		lungRight = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS);
		lungLeft = datas.get(IMAGE_KIDNEY_LUNG).getPostValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		double post = lungRight + lungLeft;
		System.out.println(lungRight + " + " + lungLeft + ")");
		System.out.println("Sum Lungs: MG(" + ant + " ; " + post + ")");
		System.out.println("Sum Lungs = " + Library_Quantif.moyGeom(ant, post));
		return Library_Quantif.moyGeom(ant, post);
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

	private void calculateResultsWithKidneys() {
		// Correct kidneys with background
		datas.get(IMAGE_KIDNEY_LUNG).setAntValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED,
												 correctValueWithBkgNoise(REGION_RIGHT_KIDNEY, false));
		datas.get(IMAGE_KIDNEY_LUNG).setAntValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED,
												 correctValueWithBkgNoise(REGION_LEFT_KIDNEY, false));
		datas.get(IMAGE_KIDNEY_LUNG).setPostValue(REGION_RIGHT_KIDNEY, Data.DATA_COUNTS_CORRECTED,
												  correctValueWithBkgNoise(REGION_RIGHT_KIDNEY, true));
		datas.get(IMAGE_KIDNEY_LUNG).setPostValue(REGION_LEFT_KIDNEY, Data.DATA_COUNTS_CORRECTED,
												  correctValueWithBkgNoise(REGION_RIGHT_KIDNEY, true));

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

		// Calculate sum shunts
		double sumShunts = this.calculateSumShunts();

		// Calculate sum lungs
		double sumLungs = this.calculateSumLungs();

		// Percentage shunt systemic
		this.results.put(RES_SHUNT_SYST.hashCode(), 100. * sumShunts / sumLungs);

		// Pulmonary shunt
		double pulmonaryShunt = (sumShunts * 100.) / (sumLungs * .38);
		this.results.put(RES_PULMONARY_SHUNT.hashCode(), pulmonaryShunt);
	}

	private void calculateResultsWithoutKidneys() {
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
		this.results.put(RES_PULMONARY_SHUNT_2.hashCode(), shunt);
	}

	@Override
	public void calculateResults() {
		if (Prefs.get(PrefTabShunpo.PREF_WITH_KIDNEYS, true)) {
			this.calculateResultsWithKidneys();
		} else {
			this.calculateResultsWithoutKidneys();
		}
	}
}
