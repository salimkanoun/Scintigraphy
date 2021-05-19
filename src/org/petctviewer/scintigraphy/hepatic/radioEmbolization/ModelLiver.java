package org.petctviewer.scintigraphy.hepatic.radioEmbolization;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.Data;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;
import org.petctviewer.scintigraphy.scin.model.Result;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import ij.ImagePlus;
import ij.gui.Roi;



public class ModelLiver extends ModelWorkflow{

	public static final String REGION_RIGHT_LUNG = "Right Lung", REGION_LEFT_LUNG = "Left Lung", REGION_LIVER = "Liver";
	
	public static final Result RES_LIVER_SHUNT = new Result(
					"Hepatic fixation (%)"), RES_LUNG_SHUNT = new Result("Pulmonary Fixation (%)");
			
	public static final int IMAGE_LIVER_LUNG = 0;
	
	private List<Data> datas;
	private Map<Integer, Double> results;
	
	
	/**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */
	public ModelLiver(ImageSelection[] selectedImages, String studyName) {
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
		Data data = this.datas.stream().filter(d -> d.getAssociatedImage() == state.getImage()).findFirst().orElse(null);
		if (data == null) {
			Date time0 = (this.datas.size() > 0 ? this.datas.get(0).getAssociatedImage().getDateAcquisition() :
				state.getImage().getDateAcquisition());
			data = new Data(state, Library_Quantif.calculateDeltaTime(time0, state.getImage().getDateAcquisition()));
		}
		return data;
	}

	/**
	 * The sum of the shunts is calculated with the counts of the liver for
	 * each orientation (Ant and Post) and then by computing the geometrical mean on those values.
	 *
	 * @return <code>GeoMean( (Liver)ant ; (Liver)post )</code>
	 */
	
	private double calculateSumLiver() {
		System.out.println("Sum Liver: MG(Liver_ant ; Liver_post)");
		double liverAnt = this.datas.get(IMAGE_LIVER_LUNG).getAntValue(REGION_LIVER, Data.DATA_COUNTS);
		System.out.println("Sum Liver: MG(" + liverAnt + " ; ");
		
		double liverPost = this.datas.get(IMAGE_LIVER_LUNG).getPostValue(REGION_LIVER, Data.DATA_COUNTS);
		System.out.println(liverPost + ")");
		System.out.println("Sum Liver: MG(" + liverAnt + " ; " + liverPost + ")");
		
		double result = Library_Quantif.moyGeom(liverAnt, liverPost);
		System.out.println("Sum Liver = " + result);
		return result;
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
		double lungRight = datas.get(IMAGE_LIVER_LUNG).getAntValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS);
		double lungLeft = datas.get(IMAGE_LIVER_LUNG).getAntValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		double ant = lungRight + lungLeft;
		System.out.print("Sum Lungs: MG(" + lungRight + " + " + lungLeft + " ; ");
		lungRight = datas.get(IMAGE_LIVER_LUNG).getPostValue(REGION_RIGHT_LUNG, Data.DATA_COUNTS);
		lungLeft = datas.get(IMAGE_LIVER_LUNG).getPostValue(REGION_LEFT_LUNG, Data.DATA_COUNTS);
		double post = lungRight + lungLeft;
		System.out.println(lungRight + " + " + lungLeft + ")");
		System.out.println("Sum Lungs: MG(" + ant + " ; " + post + ")");
		
		double result = Library_Quantif.moyGeom(ant,  post);
		System.out.println("Sum Lungs = " + result);
		return result;
	}

	/**
	 * Recover the sum of the lungs and the liver calculated by "calculateSumLiver" and "calculateSumLungs".
	 * The lungs shunt and liver shunt are calculated thanks to the previous variables and are then put into the map "results"
	 */
	private void calculateResult() {
		//Calculate sum liver
		double sumLiver = this.calculateSumLiver();
		//Calculate sum lungs
		double sumLungs = this.calculateSumLungs();
		
		//Lungs shunt
		double shuntLungs = (sumLungs / (sumLungs + sumLiver)) * 100;

		//Liver shunt
		double shuntLiver = (sumLiver / (sumLiver + sumLungs)) * 100;

		//Put the results into the map
		this.results.put(RES_LUNG_SHUNT.hashCode(), shuntLungs);
		this.results.put(RES_LIVER_SHUNT.hashCode(), shuntLiver);
	}

	
	/** 
	 * @param result
	 * @return String
	 */
	private String unitForResult(Result result) {
		return Unit.PERCENTAGE.abbrev();
	}
	
	
	/** 
	 * @param res
	 * @return String
	 */
	private String resultToCsvLine(Result res) {
		return res + "," + Library_Quantif.round(this.results.get(res.hashCode()), 2) + "," + this.unitForResult(res) + "\n";
	}
	
	
	/** 
	 * @return String
	 */
	private String csvResult() {
		return this.studyName + "\n\n" + this.resultToCsvLine(RES_LUNG_SHUNT) + this.resultToCsvLine(RES_LIVER_SHUNT);
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
		//Save the image in the state
		state.specifieImage(this.imageFromState(state));
		state.setIdImage(ImageState.ID_CUSTOM_IMAGE);
		
		ImagePlus imp = state.getImage().getImagePlus();
		
		//Prepare image
		imp.setSlice(state.getSlice());
		imp.setRoi(roi);
		
		//Calculate counts
		double counts = Library_Quantif.getCounts(imp);
		
		Data data = this.createOrRetrieveData(state);
		if(state.getFacingOrientation() == Orientation.ANT) {
			data.setAntValue(regionName, Data.DATA_COUNTS, counts, state, roi);
			data.setAntValue(regionName, Data.DATA_MEAN_COUNTS, Library_Quantif.getAvgCounts(imp));
			data.setAntValue(regionName, Data.DATA_PIXEL_COUNTS, Library_Quantif.getPixelNumber(imp));
		}else{
			data.setPostValue(regionName, Data.DATA_COUNTS, counts, state, roi);
			data.setPostValue(regionName, Data.DATA_MEAN_COUNTS, Library_Quantif.getAvgCounts(imp));
			data.setPostValue(regionName, Data.DATA_PIXEL_COUNTS, Library_Quantif.getPixelNumber(imp));
		}
		this.datas.add(data);
	}


	
	/** 
	 * @param request
	 * @return ResultValue
	 */
	@Override
	public ResultValue getResult(ResultRequest request) {
		System.out.println("requête : " + request);
/*		for (Map.Entry<Integer, Double> e : this.results.entrySet()){
			System.out.println(e.getKey());
			System.out.println(e.getValue());
		}*/
		Double value = this.results.get(request.getResultOn().hashCode());
		System.out.println("valeur : "+value);

		if(value ==  null) return null;
		//Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
		value = Unit.PERCENTAGE.convertTo(value, conversion);
		return new ResultValue(request, value, conversion);
	}


	@Override
	public void calculateResults() {
		this.calculateResult();
	}
	
	
	/** 
	 * @return String
	 */
	@Override
	public String toString() {
		return this.csvResult();
	}	
}
