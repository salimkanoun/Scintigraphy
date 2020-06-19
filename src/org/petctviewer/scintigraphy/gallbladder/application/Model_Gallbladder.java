package org.petctviewer.scintigraphy.gallbladder.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.petctviewer.scintigraphy.gallbladder.resultats.Model_Resultats_Gallbladder;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.Data;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.model.Result;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Model_Gallbladder extends ModelScinDyn {

    public static final String REGION_GALLBLADDER = "Gallbladder", REGION_LIVER = "Liver";

    public static final Result RES_GALLBLADDER = new Result("Taux d'éjection de la vésicule biliaire");

    public static final int IMAGE_GALLBLADER = 0;

    //sauvegarde des imp de départ avec tous leur stacks chacun : pour pouvoir faire les calculs de mean dans le temps//trié
    private final ImageSelection[][] sauvegardeImagesSelectDicom;
    // list : liste des examens
	// list->map->list : list des mean(double) pour tous le stack
    private ArrayList<HashMap<String, ArrayList<Double>>> examenMean;
    
    //pour le condensé dynamique
    ArrayList<Object[]> dicomRoi;

    public final Gallbladder gallPlugIn;

    private Model_Resultats_Gallbladder modelResults;

    private ImageSelection impProjeteeAllAcqui;

    private List<Data> datas;
    private Map<Integer, Double> results;

    public Model_Gallbladder(ImageSelection[][] sauvegardeImagesSelectDicom, String studyName,
    Gallbladder gallPlugIn, ImageSelection impProjeteeAllAcqui) {
        super(sauvegardeImagesSelectDicom[0], studyName, gallPlugIn.getFrameDurations());
        this.sauvegardeImagesSelectDicom = sauvegardeImagesSelectDicom;

        examenMean = new ArrayList<>();
        this.impProjeteeAllAcqui = impProjeteeAllAcqui;
        this.gallPlugIn = gallPlugIn;
        this.datas = new LinkedList<>();
        this.results = new HashMap<>();
    }


    public void setRoiMangager(RoiManager roiManager){
        this.roiManager = roiManager;
    }

    @Override
    public void calculateResults() {
        if(sauvegardeImagesSelectDicom[0].length != this.roiManager.getCount()){
            System.err.println("nombre d'imagePlus différent du nombre de Roi");
        }

        examenMean = new ArrayList<>();
        for(int i = 0; i < sauvegardeImagesSelectDicom[0].length; i++){
            HashMap<String, ArrayList<Double>> map4rois = new HashMap<>();
            //stock acquisition time
            int[] tempsInt = (Library_Dicom.buildFrameDurations(sauvegardeImagesSelectDicom[0][i].getImagePlus()));

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

            if(sauvegardeImagesSelectDicom[1].length == 0){
                //à chaque slice de l'imp
                for(int j = 1; j <= sauvegardeImagesSelectDicom[0][i].getImagePlus().getStackSize(); j++){
                    sauvegardeImagesSelectDicom[0][i].getImagePlus().setSlice(j);

                    sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
                    sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(premiereRoi);
                    roiEntier.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
                }
            }

            map4rois.put("entier", roiEntier);

            examenMean.add(map4rois);

            //Calcul final

            this.datas.get(IMAGE_GALLBLADER).setAntValue(REGION_GALLBLADDER, Data.DATA_COUNTS_CORRECTED,
            this.correctValueWithBkgNoise(REGION_GALLBLADDER, false));

            //double uneRoi = this.datas.get(IMAGE_GALLBLADER).getAntValue(REGION_GALLBLADDER, Data.DATA_COUNTS_CORRECTED);
            
            double maxValue = 0;
            double minValue = 0;
            double result = (maxValue - minValue) / maxValue;

            double finalResult = result * 100;
            this.results.put(RES_GALLBLADDER.hashCode(), finalResult);

        }

        dicomRoi = new ArrayList<>();
        for(int i =0; i < sauvegardeImagesSelectDicom[0].length; i++){
            Object[] content = {sauvegardeImagesSelectDicom[0][i].getImagePlus(), this.roiManager.getRoi(i).getBounds()};
            dicomRoi.add(content);
        }
    }    
            
    /**
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

    public ArrayList<HashMap<String, ArrayList<Double>>> getExamenMean(){
        return this.examenMean;
    }

    public ArrayList<Object[]> getDicomRoi(){
        return this.dicomRoi;
    }

    public void setModelResults(Model_Resultats_Gallbladder model){
        this.modelResults = model;
    }

    public String toString(){
        return this.modelResults.toString();
    }
    
    /**
	 * In order to get the Scinti out of all programms.
	 */
	public void setImpProjeteeAllAcqui(ImageSelection impProjeteeAllAcqui) {
		this.impProjeteeAllAcqui = impProjeteeAllAcqui;
	}

	/**
	 * In order to get the Scinti out of all programms.
	 */
	public ImageSelection getImgPrjtAllAcqui() {
		return this.impProjeteeAllAcqui;
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
}