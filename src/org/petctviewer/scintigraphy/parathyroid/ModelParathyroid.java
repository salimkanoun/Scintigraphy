package org.petctviewer.scintigraphy.parathyroid;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.awt.Rectangle;

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

public class ModelParathyroid extends ModelWorkflow {

    public static final String REGION_THYRO_PARA = "Thyroid and Parathyroid", REGION_THYRO = "Thyroid";

    public static final Result RES_RATIO_THYRO_PARA = new Result("Thyroid+Para Ratio"), RES_RATIO_THYRO= new Result("Thyroid ratio"); 

    public static final int IMAGE_THYROIDPARA = 0, IMAGE_THYROID = 1;

    private List<Data> datas;
    private Map<Integer, Double> results;
    
    /**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */

    public ModelParathyroid(ImageSelection[] selectedImages, String studyName) {
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
	 * Returns the regions for the Thyroid and Thyroid+Para images.
	 *
	 * @return array of regions name of the Thyroid and Thyroid+Para images
	 */
	private String[] regionsThyroParathyro() {
		return new String[]{REGION_THYRO, REGION_THYRO_PARA};
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

	public Roi getRoi(int index){
		Roi premiereRoi = this.roiManager.getRoi(index);
		System.out.println(premiereRoi);
		return premiereRoi;
	}
	
	private double calculateRatio(){
		System.out.println("Sum Liver: MG(Liver_ant ; Liver_post)");
		double liverAnt = this.datas.get(IMAGE_THYROID).getAntValue(REGION_THYRO, Data.DATA_COUNTS);
		System.out.println("Sum Liver: MG(" + liverAnt + " ; ");
		
		double liverPost = this.datas.get(IMAGE_THYROIDPARA).getAntValue(REGION_THYRO_PARA, Data.DATA_COUNTS);
		System.out.println(liverPost + ")");
		System.out.println("Sum Liver: MG(" + liverAnt + " ; " + liverPost + ")");
		
		double result = Library_Quantif.moyGeom(liverAnt, liverPost);
		System.out.println("Sum Liver = " + result);
		return result;
	}

    private void calculateResult() {
        //Calculate sum liver
		double ratio = this.calculateRatio();
		
		//Put the results into the map
		this.results.put(RES_RATIO_THYRO.hashCode(), ratio);
		this.results.put(RES_RATIO_THYRO_PARA.hashCode(), ratio);
    }

    @Override
    public void calculateResults() {
        this.calculateResult();
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
    

}