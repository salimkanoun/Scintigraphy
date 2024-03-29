package org.petctviewer.scintigraphy.gallbladder.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.gallbladder.resultats.Model_Resultats_Gallbladder;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;
import org.petctviewer.scintigraphy.scin.model.Data;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.model.Result;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class ModelGallbladder extends ModelScinDyn {

    public static final String REGION_GALLBLADDER = "Gallbladder", REGION_LIVER = "Liver";

    public static final Result RES_GALLBLADDER = new Result("Taux d'éjection de la vésicule biliaire");

    public static final int IMAGE_GALLBLADER = 0;

    // list : liste des examens
	// list->map->list : list des mean(double) pour tous le stack
  //  private ArrayList<HashMap<String, ArrayList<Double>>> examenMean;
    
    //pour le condensé dynamique
    ArrayList<Object[]> dicomRoi;
    private final Map<String, Roi> organRois;
    private final ImageSelection ims;
    private Model_Resultats_Gallbladder modelResults;

    private final List<Data> datas;
    private final Map<Integer, Double> results;

    public ModelGallbladder(ImageSelection[] sauvegardeImagesSelectDicom, String studyName, int[] frameDuration) {
        super(sauvegardeImagesSelectDicom, studyName, frameDuration);
        //sauvegarde des imp de départ avec tous leur stacks chacun : pour pouvoir faire les calculs de mean dans le temps//trié
        this.organRois = new HashMap<>();
        this.ims = sauvegardeImagesSelectDicom[0];
        this.datas = new LinkedList<>();
        this.results = new HashMap<>();
    }


    public void enregistrerMesure(String nomRoi, ImagePlus imp) {
        if (this.isUnlocked()) {

            this.organRois.put(nomRoi, imp.getRoi());

            this.getData().computeIfAbsent(nomRoi, k -> new ArrayList<>());

            // on y ajoute le nombre de coups
            this.getData().get(nomRoi).add(Math.max(Library_Quantif.getCounts(imp),1.0d) );
        }
    }


    public void setRoiMangager(RoiManager roiManager){
        this.roiManager = roiManager;
    }

    private void getEjectionFraction(){
        //correct organs with the background
       //datas.get(IMAGE_GALLBLADER).setAntValue(REGION_GALLBLADDER, Data.DATA_COUNTS_CORRECTED, correctValueWithBkgNoise(REGION_GALLBLADDER,false));
        ImagePlus imp = this.getImageSelection()[0].getImagePlus();
        Roi vesicule = Library_Roi.getRoiByName(this.getRoiManager(),"Gallbladder");
        Roi liverBckg = Library_Roi.getRoiByName(this.getRoiManager(),"Liver");

        this.organRois.put("Gallbladder", vesicule);
        this.organRois.put("Liver", liverBckg);


        double maxValue = Library_Quantif.getMaxCountsCorrectedBackground(imp,vesicule,liverBckg);
        double minValue = Library_Quantif.getMinCountsCorrectedBackground(imp,vesicule,liverBckg, 120,  imp.getNSlices());
        double result = (maxValue - minValue) / maxValue;

        double finalResult = result * 100;
        this.results.put(RES_GALLBLADDER.hashCode(), finalResult);

    }

    @Override
    public void calculateResults() {

    /*
        examenMean = new ArrayList<>();
        for(int i = 0; i < sauvegardeImagesSelectDicom.length; i++){
            HashMap<String, ArrayList<Double>> map4rois = new HashMap<>();
            //stock acquisition time
            int[] tempsInt = (Library_Dicom.buildFrameDurations(this.getImagePlus()));

            double[] tempsSeconde = new double[tempsInt.length];
            for(int j = 0; j < tempsInt.length; j++){
                tempsSeconde[j] = tempsInt[j] / 1000.0D;
            }

            ArrayList<Double> temps = new ArrayList<>();
            double memtemps = 0.0;
            for(int j = 0 ; j< tempsSeconde.length; j++){
                memtemps += tempsSeconde[i];
                temps.add(memtemps);
            }
            map4rois.put("temps", temps);

            Roi premiereRoi = this.roiManager.getRoi(i);


            ArrayList<Double> roiEntier = new ArrayList<>();

            if(sauvegardeImagesSelectDicom.length == 0){
                //à chaque slice de l'imp
                for(int j = 1; j <= sauvegardeImagesSelectDicom[0].getImagePlus().getStackSize(); j++){
                    sauvegardeImagesSelectDicom[i].getImagePlus().setSlice(j);

                    sauvegardeImagesSelectDicom[i].getImagePlus().deleteRoi();
                    sauvegardeImagesSelectDicom[i].getImagePlus().setRoi(premiereRoi);
                    roiEntier.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[i].getImagePlus())/tempsSeconde[j-1]);
                }
            }

            map4rois.put("entier", roiEntier);

            examenMean.add(map4rois); */

            //Calcul final

          //  this.datas.get(IMAGE_GALLBLADER).setAntValue(REGION_GALLBLADDER, Data.DATA_COUNTS_CORRECTED,
            //this.correctValueWithBkgNoise(REGION_GALLBLADDER, false));

            //double uneRoi = this.datas.get(IMAGE_GALLBLADER).getAntValue(REGION_GALLBLADDER, Data.DATA_COUNTS_CORRECTED);
        this.getEjectionFraction();
        System.out.println(this.results.get(RES_GALLBLADDER.hashCode()));

        ImagePlus imp = this.ims.getImagePlus();
        this.getData().computeIfAbsent("Gallbladder", k -> new ArrayList<>());
        List<Double> gb = this.getData().get("Gallbladder");
        imp.setRoi(this.organRois.get("Gallbladder"));
        for (int i = 1; i <= imp.getNSlices(); i++) {
            imp.setSlice(i);
            gb.add(Math.max(Library_Quantif.getCounts(imp), 1.0d));
        }

        /*

        dicomRoi = new ArrayList<>();
        for(int i =0; i < sauvegardeImagesSelectDicom.length; i++){
            Object[] content = {sauvegardeImagesSelectDicom[i].getImagePlus(), this.roiManager.getRoi(i).getBounds()};
            dicomRoi.add(content);
        }*/
    }    
            
    /**
     *
     * Corrects the value of the specified region with the background region. To use this method, the background noise
     * region <b>must be</b> set.
     * @param regionName Name of the region to correct
     * @param post If set to TRUE, the Post orientation of the region will be used. Else, Ant orientation will be used.
     * @return corrected value of the region
     */
    private double correctValueWithBkgNoise(String regionName, boolean post){
        double counts,meanBkg, pixels;
                counts = datas.get(0).getAntValue(regionName, Data.DATA_COUNTS);
                System.out.println("Right counts : " + counts);
                meanBkg = datas.get(IMAGE_GALLBLADER).getAntValue(REGION_LIVER, Data.DATA_MEAN_COUNTS);
                System.out.println("Right background : " + meanBkg);
                pixels = datas.get(IMAGE_GALLBLADER).getAntValue(regionName, Data.DATA_PIXEL_COUNTS);
                System.out.println("Right pixels : " + pixels);
                return counts - meanBkg * pixels;
    }

    /**public ArrayList<HashMap<String, ArrayList<Double>>> getExamenMean(){
        return this.examenMean;
    }**/

    public ArrayList<Object[]> getDicomRoi(){
        return this.dicomRoi;
    }

    public Model_Resultats_Gallbladder getModelResults() {
        return modelResults;
    }

    public void setModelResults(Model_Resultats_Gallbladder model){
        this.modelResults = model;
    }


    public Map<Integer, Double> getResults() {
        return results;
    }
    
    /** 
	 * @param request
	 * @return ResultValue
	 */
	public ResultValue getResult(ResultRequest request) {

		Double value = this.results.get(request.getResultOn().hashCode());

		if(value ==  null) return null;
		//Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
        if (request.getUnit() == null){
            value = Unit.PERCENTAGE.convertTo(value, conversion);
        }
		return new ResultValue(request, value, conversion);
    }

    @Override
    public String toString(){

	    double result = Library_Quantif.round(this.results.get(RES_GALLBLADDER.hashCode()),2);
        return super.toString() + "\nEjection Fraction: "+ result + ",%\n";


    }
}