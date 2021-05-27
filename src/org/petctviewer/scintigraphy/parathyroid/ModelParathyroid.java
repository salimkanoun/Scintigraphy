package org.petctviewer.scintigraphy.parathyroid;

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
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

public class ModelParathyroid extends ModelWorkflow {

    public static final String REGION_THYRO_PARA = "Thyroid Early", REGION_PARATHYROID = "Thyroid Late";

    public static final Result RES_RATIO_THYRO_PARA = new Result("Thyroid+Para Ratio"), RES_RATIO_PARATHYRO= new Result("Parathyroid ratio"); 

    public static final int IMAGE_THYROIDPARA = 0, IMAGE_PARATHYROID = 1;

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
	 * @param request
	 * @return ResultValue
	 */
	@Override
    public ResultValue getResult(ResultRequest request) {
        Double value = this.results.get(request.getResultOn().hashCode());
        if (value == null) return null;
		// Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
		value = Unit.PERCENTAGE.convertTo(value, conversion);
		return new ResultValue(request, value, conversion);
	}

	
	/** 
	 * @param index
	 * @return Roi
	 */
	public Roi getRoi(int index){
		Roi myRoi = this.roiManager.getRoi(index);
		System.out.println(myRoi);
		return myRoi;
	}
	
	
	/** 
	 * @return ImagePlus
	 */
	private ImagePlus calculateImageRatio(){
		System.out.println("Nbre de coups");
		double r2 = this.datas.get(IMAGE_PARATHYROID).getAntValue(REGION_PARATHYROID, Data.DATA_COUNTS);
		System.out.println("Parathyroide: " + r2 + " ; ");
		
		double r1 = this.datas.get(IMAGE_THYROIDPARA).getAntValue(REGION_THYRO_PARA, Data.DATA_COUNTS);
		System.out.println("Thyroide+Parathyroide: " + r1 +";");
		
		ImageSelection[] selection = this.getImageSelection();
		double result;
		ImagePlus mult;
		ImageProcessor processor;
		if (r1 > r2) {
			result = r1/r2;
			processor = selection[IMAGE_PARATHYROID].getImagePlus().getProcessor();
			processor.multiply(result);

			mult = selection[IMAGE_PARATHYROID].getImagePlus();
		} else {
			result = r2/r1;
			processor = selection[IMAGE_THYROIDPARA].getImagePlus().getProcessor();
			processor.multiply(result);

			mult = selection[IMAGE_THYROIDPARA].getImagePlus();
		}
		mult.setProcessor(processor);
		System.out.println("Ratio = " + result);
		System.out.println("ImageMult = "+ mult);
		return mult;
	}

    
	/** 
	 * @return ImagePlus
	 */
	public ImagePlus calculateResult() {
        //Calculate ratio
		ImagePlus ratio = this.calculateImageRatio();
		double r1 = this.datas.get(IMAGE_THYROIDPARA).getAntValue(REGION_THYRO_PARA, Data.DATA_COUNTS);
		System.out.println("Thyroide+Parathyroide: " + r1 +";");

		double r2 = this.datas.get(IMAGE_PARATHYROID).getAntValue(REGION_PARATHYROID, Data.DATA_COUNTS);
		System.out.println("Parathyroide: " + r2 + " ; ");

		ImageSelection[] selection = this.getImageSelection();
		ImagePlus result = null;
		ImageCalculator ic = new ImageCalculator();

		if (r1 > r2){
			System.out.println("r1 > r2");
			result = ic.run("subtract create stack", ratio, selection[IMAGE_THYROIDPARA].getImagePlus());
		} else {
			result = ic.run("subtract create stack", selection[IMAGE_PARATHYROID].getImagePlus(), ratio);
		}
		result.getProcessor().applyMacro("if(v==0 || v>0)v=1");
		result.getProcessor().applyMacro("if(v<0)v=0");
		result = ic.run("multiply create stack", selection[IMAGE_PARATHYROID].getImagePlus(), result);
		return result;
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