package org.petctviewer.scintigraphy.thyroid;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;

import java.util.*;

public class ModelThyroid extends ModelWorkflow{
    
    public static final String REGION_LEFT_LOBE = "Left lobe", REGION_RIGHT_LOBE = "Right Lobe",
    REGION_BACKGROUND_LEFT = "BackgroundL", REGION_BACKGROUND_RIGHT = "BackgroundR", REGION_FULL_SYRINGUE = "Full syringue", REGION_EMPTY_SYRINGUE = "Empty syringue";

    public static final Result RES_SYRINGUE_DIFFERENCE = new Result("Syringue difference"),
    RES_THYROID_SHUNT = new Result("Thyroid shunt");

    public static final int IMAGE_FULL_SYRINGUE = 0, IMAGE_EMPTY_SYRINGUE = 1, IMAGE_THYROID = 2;

    private List<Data> datas;
    private Map<Integer,Double> results;

    /**
     * @param selectedImages Images needed for this study(generally those images are used in the workflow)
     * @param studyName Name of the study (used for display)
     */
    public ModelThyroid(ImageSelection[] selectedImages, String studyName){
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
     * Corrects the value of the specified region with the background region. To use this method, the background noise
     * region <b>must be</b> set.
     * @param regionName Name of the region to correct
     * @param post If set to TRUE, the Post orientation of the region will be used. Else, Ant orientation will be used.
     * @return corrected value of the region
     */
    private double correctValueWithBkgNoise(String regionName, boolean post){
        double counts, bkgL, bkgR, meanBkg, pixels;
        if(!post){
            counts = datas.get(IMAGE_THYROID).getAntValue(regionName, Data.DATA_COUNTS);
            bkgL = datas.get(IMAGE_THYROID).getAntValue(REGION_BACKGROUND_LEFT, Data.DATA_MEAN_COUNTS);
            bkgR = datas.get(IMAGE_THYROID).getAntValue(REGION_BACKGROUND_RIGHT, Data.DATA_MEAN_COUNTS);
            meanBkg = bkgL + bkgR;
            pixels = datas.get(IMAGE_THYROID).getAntValue(regionName, Data.DATA_PIXEL_COUNTS);
            return counts - meanBkg * pixels;
        } else {
            return 0;
        }
    }
    /**
     * Calculates the sum of counts of the full syringe in one orientation (Ant).
     * @return fullSyringe Number of counts
     */
    private double calculateSumFullSyringe(){
        double fullSyringe = this.datas.get(IMAGE_FULL_SYRINGUE).getAntValue(REGION_FULL_SYRINGUE, Data.DATA_COUNTS);
        System.out.print("Sum Full Syringe = " + fullSyringe);

        return fullSyringe;
    }

        
    /**
     * Calculates the sum of counts of the empty syringe in one orientation (Ant).
     * @return emptySyringe Number of counts
     */
    private double calculateSumEmptySyringe(){
        double emptySyringe = this.datas.get(IMAGE_EMPTY_SYRINGUE).getAntValue(REGION_EMPTY_SYRINGUE, Data.DATA_COUNTS);
        System.out.print("Sum Full Syringe = " + emptySyringe);

        return emptySyringe;
    }

    /**
     * Calculates the sum of the thyroid by adding the counts of the right lobe and the left lobe
     * only in Ant orientation.
     * @return 
     */

     private double calculateSumThyroid(){

        //Correct lobes with background
        datas.get(IMAGE_THYROID).setAntValue(REGION_RIGHT_LOBE, Data.DATA_COUNTS_CORRECTED,
        correctValueWithBkgNoise(REGION_RIGHT_LOBE, false));
        datas.get(IMAGE_THYROID).setAntValue(REGION_LEFT_LOBE, Data.DATA_COUNTS_CORRECTED,
        correctValueWithBkgNoise(REGION_LEFT_LOBE, false));


         System.out.println("Sum thyroid : (right lobe and + left lobe ant)");
         double lobeRight = datas.get(IMAGE_THYROID).getAntValue(REGION_RIGHT_LOBE, Data.DATA_COUNTS);
         double lobeLeft = datas.get(IMAGE_THYROID).getAntValue(REGION_LEFT_LOBE, Data.DATA_COUNTS);

         double sumLobe = lobeRight + lobeLeft;
         System.out.println("Sum thyroid = " + sumLobe);

         return sumLobe;
     }

	/**
	 * Recover the sum of the thyroid lobes calculated by "calculateSumThyroid"
     * Recover also the sum of the full and the empty syringes and substract them to obtain a fixate ratio
     * of the thyroide compared to the activity injected. It's expressed in %.
	 * The lungs shunt and liver shunt are calculated thanks to the previous variables and are then put into the map "results"
	 */

     private void calculateResult(){ 
        //Calculate sum full syringe
        double sumFullSyringe = this.calculateSumFullSyringe();
        //Calculate sum empty syringe
        double sumEmptySyringe = this.calculateSumEmptySyringe();
        //Calculate sum thyroid
        double sumThyroid = this.calculateSumThyroid();

        //Difference between full and empty syringe
        double difference = sumFullSyringe - sumEmptySyringe;

        //Put the results into the map
        this.results.put(RES_SYRINGUE_DIFFERENCE.hashCode(), difference);
        this.results.put(RES_THYROID_SHUNT.hashCode(), sumThyroid);

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
		return res + "," + this.results.get(res.hashCode()) + "," + this.unitForResult(res) + "\n";
    }
    
    /** 
	 * @return String
	 */
	private String csvResult() {
		return this.studyName + "\n\n" + this.resultToCsvLine(RES_SYRINGUE_DIFFERENCE) + this.resultToCsvLine(RES_THYROID_SHUNT);
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

		Double value = this.results.get(request.getResultOn().hashCode());
		System.out.println("valeur : "+value);

		if(value ==  null) return null;
		//Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
		value = Unit.PERCENTAGE.convertTo(value, conversion);
		return new ResultValue(request, value, conversion);
    }
    
    @Override
    public void calculateResults(){
        this.calculateResult();
    }

    /**
     * @return String
     */
    @Override
    public String toString(){
        return this.csvResult();
    }
}