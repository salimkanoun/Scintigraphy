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
    REGION_BACKGROUND_LEFT = "BackgroundL", REGION_BACKGROUND_RIGHT = "BackgroundR", REGION_FULL_SYRINGE = "Full syringe", REGION_EMPTY_SYRINGE = "Empty syringe";

    public static final Result RES_THYROID_SHUNT = new Result("Taux de fixation thyroïdien"), RES_THYROID_SURFACE_LEFT = new Result("Left lobe"), RES_THYROID_SURFACE_RIGHT = new Result("Right lobe");

    public static final int IMAGE_FULL_SYRINGE = 0,  IMAGE_EMPTY_SYRINGE = 1, IMAGE_THYROID = 2;

    private final List<Data> datas;
    private final Map<Integer,Double> results;

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
        double counts, meanBkgL, meanBkgR, pixels;
        if(!post){
            if(regionName.equals(REGION_RIGHT_LOBE)) {
                counts = datas.get(IMAGE_THYROID).getAntValue(regionName, Data.DATA_COUNTS);
                System.out.println("Right counts : " + counts);
                meanBkgR = datas.get(IMAGE_THYROID).getAntValue(REGION_BACKGROUND_RIGHT, Data.DATA_MEAN_COUNTS);
                System.out.println("Right background : " + meanBkgR);
                pixels = datas.get(IMAGE_THYROID).getAntValue(regionName, Data.DATA_PIXEL_COUNTS);
                System.out.println("Right pixels : " + pixels);
                return counts - meanBkgR * pixels;
            }else{
                counts = datas.get(IMAGE_THYROID).getAntValue(regionName, Data.DATA_COUNTS);
                System.out.println("Left counts : " + counts);
                meanBkgL = datas.get(IMAGE_THYROID).getAntValue(REGION_BACKGROUND_LEFT, Data.DATA_MEAN_COUNTS);
                System.out.println("Left background : " + meanBkgL);
                pixels = datas.get(IMAGE_THYROID).getAntValue(regionName, Data.DATA_PIXEL_COUNTS);
                System.out.println("Left pixels : " + pixels);
                return counts - meanBkgL * pixels;
            }
        } else {
            return 0;
        }
    }
    /**
     * Calculates the sum of counts of the full syringe in one orientation (Ant).
     * @return fullSyringe Number of counts
     */
    private double calculateSumFullSyringe(){
        double fullSyringe = this.datas.get(IMAGE_FULL_SYRINGE).getAntValue(REGION_FULL_SYRINGE, Data.DATA_COUNTS);
        System.out.print("\nSum Full Syringe = " + fullSyringe +"\n\n");

        return fullSyringe;
    }

        
    /**
     * Calculates the sum of counts of the empty syringe in one orientation (Ant).
     * @return emptySyringe Number of counts
     */
    private double calculateSumEmptySyringe(){
        double emptySyringe = this.datas.get(IMAGE_EMPTY_SYRINGE).getAntValue(REGION_EMPTY_SYRINGE, Data.DATA_COUNTS);
        System.out.print("Sum Empty Syringe = " + emptySyringe + "\n\n");

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


         double lobeRight = datas.get(IMAGE_THYROID).getAntValue(REGION_RIGHT_LOBE, Data.DATA_COUNTS_CORRECTED);
         double lobeLeft = datas.get(IMAGE_THYROID).getAntValue(REGION_LEFT_LOBE, Data.DATA_COUNTS_CORRECTED);

         double sumLobe = lobeRight + lobeLeft;
         System.out.println("Right lobe corrected = " + lobeRight +"\n");
         System.out.println("Left lobe corrected = " + lobeLeft +"\n");
         System.out.println("Sum thyroid = " + sumLobe +"\n");

         return sumLobe;
     }

	/**
	 * Recover the sum of the thyroid lobes calculated by "calculateSumThyroid"
     * Recover also the sum of the full and the empty syringes and substract them to obtain a fixate ratio
     * of the thyroide compared to the activity injected. It's expressed in %.
	 * The counts are calculated thanks to the previous variables and are then put into the map "results"
	 */

     private void calculateResult(){ 
        //Calculate sum full syringe
        double sumFullSyringe = this.calculateSumFullSyringe();
        //Calculate sum empty syringe
        double sumEmptySyringe = this.calculateSumEmptySyringe();
        //Calculate sum thyroid
        double sumThyroid = this.calculateSumThyroid();

        //Difference between full and empty syringe
        double difference = (sumFullSyringe - sumEmptySyringe);
        System.out.println("Difference = " + difference +"\n");

        
        if (difference > 0){
            //Thyroid fixation ratio compared to the injected activity which is expressed in %
            double finalResult = sumThyroid / difference;
            //Put the results into the map
            this.results.put(RES_THYROID_SHUNT.hashCode(), finalResult*100);

            //Thyroid surface
            //Afficher surface thryoide (surface du pixel * nombre de pixel de chaque ROI)
            double thyroidSurfaceLeftMm, thyroidSurfaceLeftCm, thyroidSurfaceRightMm, thyroidSurfaceRightCm, pixelsLeft, pixelsRight;

            pixelsRight = datas.get(IMAGE_THYROID).getAntValue(REGION_RIGHT_LOBE, Data.DATA_PIXEL_COUNTS);
            pixelsLeft = datas.get(IMAGE_THYROID).getAntValue(REGION_LEFT_LOBE, Data.DATA_PIXEL_COUNTS);

            thyroidSurfaceLeftMm = pixelsLeft * 1.2;
            thyroidSurfaceLeftCm = thyroidSurfaceLeftMm / 100;

            thyroidSurfaceRightMm = pixelsRight * 1.2;
            thyroidSurfaceRightCm = thyroidSurfaceRightMm / 100;
            
            this.results.put(RES_THYROID_SURFACE_RIGHT.hashCode(), thyroidSurfaceRightCm);
            this.results.put(RES_THYROID_SURFACE_LEFT.hashCode(), thyroidSurfaceLeftCm);
        }else{
            this.results.put(RES_THYROID_SHUNT.hashCode(), -1d);
            this.results.put(RES_THYROID_SURFACE_RIGHT.hashCode(), -1d);
            this.results.put(RES_THYROID_SURFACE_LEFT.hashCode(), -1d);
        }
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
		return this.studyName + "\n\n" + this.resultToCsvLine(RES_THYROID_SHUNT) + resultToCsvLine(RES_THYROID_SURFACE_LEFT) + resultToCsvLine(RES_THYROID_SURFACE_RIGHT);
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
        imp.show();
		
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

		Double value = this.results.get(request.getResultOn().hashCode());

		if(value ==  null) return null;
		//Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
        if (request.getUnit() == null){
            value = Unit.PERCENTAGE.convertTo(value, conversion);
        }else{
            value = Unit.SURFACE.convertTo(value, conversion);
        }
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
    public String toString(){
        return this.csvResult();
    }
}