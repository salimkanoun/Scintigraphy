package org.petctviewer.scintigraphy.gallbladder.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.*;

import org.petctviewer.scintigraphy.gallbladder.resultats.Model_Resultats_Gallbladder;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Model_Gallbladder extends ModelScinDyn {

    //sauvegarde des imp de départ avec tous leur stacks chacun : pour pouvoir faire les calculs de mean dans le temps//trié
    private final ImageSelection[][] sauvegardeImagesSelectDicom;
    // list : liste des examens
	// list->map : map des 4 roi ( entier, premier tier, deuxieme tier et troisieme tier)
	// list->map->list : list des mean(double) pour tous le stack
    private ArrayList<HashMap<String, ArrayList<Double>>> examenMean;
    
    //pour le condensé dynamique
    ArrayList<Object[]> dicomRoi;

    public final Gallbladder gallPlugIn;

    private Model_Resultats_Gallbladder modelResults;

    private ImageSelection impProjeteeAllAcqui;

    public Model_Gallbladder(ImageSelection[][] sauvegardeImagesSelectDicom, String studyName,
    Gallbladder gallPlugIn, ImageSelection impProjeteeAllAcqui) {
        super(sauvegardeImagesSelectDicom[0], studyName, gallPlugIn.getFrameDurations());
        this.sauvegardeImagesSelectDicom = sauvegardeImagesSelectDicom;

        examenMean = new ArrayList<>();
        this.impProjeteeAllAcqui = impProjeteeAllAcqui;
        this.gallPlugIn = gallPlugIn;
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

            //on découpe la roi en 3
            Roi premiereRoi = this.roiManager.getRoi(i);

            Point pointOrigine = premiereRoi.getContainedPoints()[0];
            double largeurRef = premiereRoi.getBounds().getWidth();
            double hauteurRef = premiereRoi.getBounds().getHeight();

            int tierHauteurRoiRef = (int) Math.round(hauteurRef/3);

            Roi unTier = new Roi(pointOrigine.getX(), pointOrigine.getY(),largeurRef, tierHauteurRoiRef);
            Roi deuxTier = new Roi(pointOrigine.getX(), pointOrigine.getY() + tierHauteurRoiRef+1, largeurRef, tierHauteurRoiRef);
            Roi troisTier = new Roi(pointOrigine.getX(), pointOrigine.getY()+tierHauteurRoiRef+tierHauteurRoiRef+2, largeurRef, tierHauteurRoiRef);

            ArrayList<Double> roiEntier = new ArrayList<>();
            ArrayList<Double> unTierList = new ArrayList<>();
            ArrayList<Double> deuxTierList = new ArrayList<>();
            ArrayList<Double> troisTierList = new ArrayList<>();

            if(sauvegardeImagesSelectDicom[1].length == 0){
                //à chaque slice de l'imp
                for(int j = 1; j <= sauvegardeImagesSelectDicom[0][i].getImagePlus().getStackSize(); j++){
                    sauvegardeImagesSelectDicom[0][i].getImagePlus().setSlice(j);

                    sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
                    sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(premiereRoi);
                    roiEntier.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);

                    sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(unTier);
					unTierList.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
					
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(deuxTier);
					deuxTierList.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
					
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(troisTier);
					troisTierList.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
                }
            }

            map4rois.put("entier", roiEntier);
            map4rois.put("unTier", unTierList);
            map4rois.put("deuxTier", deuxTierList);
            map4rois.put("troisTier", troisTierList);

            examenMean.add(map4rois);

        }

        dicomRoi = new ArrayList<>();
        for(int i =0; i < sauvegardeImagesSelectDicom[0].length; i++){
            Object[] content = {sauvegardeImagesSelectDicom[0][i].getImagePlus(), this.roiManager.getRoi(i).getBounds()};
            dicomRoi.add(content);
        }
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
}