package org.petctviewer.scintigraphy.hepatic.scintivol;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.ArrayList;
import java.util.HashMap;



public class Model_Scintivol extends ModelScinDyn {



    private final HashMap<String, Roi> organRois;
    private HashMap<Comparable, Double> adjustedValues;
    private ArrayList<String> glands;

    private ImageSelection impAnt;
    private int[] frameDurations;
    private final HashMap<String, Integer> pixelCounts;
    private HashMap<String, Double> uptakeRatio;
    private HashMap<String, Double> excretionFraction;

    public Model_Scintivol(int[] frameDuration, ImageSelection[] selectedImages, String studyName, ImageSelection impAnt) {

        super(selectedImages, studyName, frameDuration);
        this.organRois = new HashMap<>();
        this.impAnt = impAnt;

        this.frameDurations = frameDuration;

        this.pixelCounts = new HashMap<>();
    }

    public ImageSelection getImpAnt() {
        return impAnt;
    }
    public void enregistrerMesure(String nomRoi, ImagePlus imp) {
        if (this.isUnlocked()) {
            this.organRois.put(nomRoi, imp.getRoi());

            this.getData().computeIfAbsent(nomRoi, k -> new ArrayList<>());
            // on y ajoute le nombre de coups
            this.getData().get(nomRoi).add(Math.max(Library_Quantif.getCounts(imp),1.0d) );
        }
    }

    public void enregistrerPixelRoi(String roiName, int pixelNumber) {
        this.pixelCounts.put(roiName, pixelNumber);
    }


    public double getClairanceFT(double a, double b){
        //a = diff hépatique
        //b = getA() * getCnorm
        return a/b;
    }
    public double getCnorm(double c1, double c2){
        return c1/c2;
    }

    public double getA(){
        return 0;
    }

    @Override
    public void calculateResults() {

    }
}
