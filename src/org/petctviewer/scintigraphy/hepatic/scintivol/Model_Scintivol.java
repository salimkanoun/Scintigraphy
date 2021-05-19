package org.petctviewer.scintigraphy.hepatic.scintivol;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Model_Scintivol extends ModelScinDyn {
    private final Map<String, Roi> organRois;
    private Map<String, Map<String, Double>> results;
    private ImageSelection imsRetention;
    private double timeChart;
    private double tracerDelayTime;
    private final Map<String, Integer> pixelCounts;
    private double size;
    private double weight;
    private final ArrayList<String> organes;
    private int[] frameDurations;


    public Model_Scintivol(ImageSelection[] selectedImages, String studyName, int[] frameDuration, ImageSelection imsRetention) {
        super(selectedImages, studyName, frameDuration);
        this.organRois = new HashMap<>();
        this.imsRetention = imsRetention;

        this.pixelCounts = new HashMap<>();
        this.results = new HashMap<>();

        this.organes = new ArrayList<>();
        this.organes.add("Heart");
        this.organes.add("Retention");

        this.organes.add("Liver");
        this.organes.add("FOV");
        this.organes.add("Other");
    }

    public ArrayList<String> getOrganes() {
        return organes;
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


    private double getClairanceFT() {
        double L_t1 = this.results.get("Liver").get("t1");
        double L_t2 = this.results.get("Liver").get("t2");


        double H_t1 = this.results.get("Heart").get("t1");

        double A_t1 = this.results.get("Other").get("BP Activity");

        double AUC_t1_t2 = this.results.get("Heart").get("AUC");


        this.results.get("Other").put("AUC/Cnorm", AUC_t1_t2/H_t1);

        double res = 100 * 6 * (L_t2 - L_t1) / (A_t1 * AUC_t1_t2/H_t1);
        this.results.get("Other").put("Clairance FT",  res);

        return  res;
    }

    private double getNormalizedClairanceFT() {
        double clairanceFT = this.results.get("Other").get("Clairance FT");
        double sc = this.results.get("Other").get("SC");

        double res = clairanceFT / sc;
        this.results.get("Other").put("Norm Clairance FT", res);
        return  res;
    }

    /**
     *
     * @param sliceT1
     * @param sliceT2
     * @return
     */
    private double getAUC(int sliceT1, int sliceT2) {
        ImagePlus imp = this.getImageSelection()[0].getImagePlus();
        imp.setRoi(Library_Roi.getRoiByName(this.getRoiManager(), "Heart"));

        double res = 0;
        for (int slice = sliceT1; slice <= sliceT2; slice++) {
            imp.setSlice(slice);
            res += Library_Quantif.getCounts(imp);
        }

        return res;
    }

    private double getBPActivity() {
        double T_t1 = this.results.get("FOV").get("t1");
        double T_t2 = this.results.get("FOV").get("t2");

        double L_t1 = this.results.get("Liver").get("t1");

        double Cnorm_t2 = this.results.get("Heart").get("t2") / this.results.get("Heart").get("t1");
        this.results.get("Other").put("Cnorm_t2", Cnorm_t2);

        double res = (T_t2 - L_t1 - (T_t1 - L_t1) * Cnorm_t2) / (1 - Cnorm_t2);
        this.results.get("Other").put("BP Activity", res);
        return res;
    }

    private double getSC() {
        double res = Math.sqrt((this.size * this.weight) / 3600);
        this.results.get("Other").put("SC", res);

        return  res;
    }

    /**
     * Futur foie restant
     * @return
     */
    private double getFFR() {
        double ft = this.results.get("Tomo").get("FT");     //Total liver
        double ffr = this.results.get("Tomo").get("FFR");   // Future remaining liver

        double res = ffr/ft;
        this.results.get("Other").put("FFR/FT", res);
        return res;
    }

    private double getClairanceFFR() {
        double clairanceFT = this.results.get("Other").get("Clairance FT");
        double ffr_ft = this.results.get("Other").get("FFR/FT");

        double res = clairanceFT * ffr_ft;
        this.results.get("Other").put("Clairance FFR", res);
        return res;
    }

    private double getNormalizedClairanceFFR() {
        double clairanceFTNorm = this.results.get("Other").get("Norm Clairance FT");
        double ffr_ft = this.results.get("Other").get("FFR/FT");

        double res = clairanceFTNorm * ffr_ft;
        this.results.get("Other").put("Norm Clairance FFR", res);
        return res;
    }

    private double getRetentionRate() {
        double res;
        ImagePlus imp = this.imsRetention.getImagePlus();

        imp.setRoi(Library_Roi.getRoiByName(this.getRoiManager(), "Liver parenchyma"));
        double max = Library_Quantif.getMaxCounts(imp);
        imp.setSlice(imp.getNSlices());
        double end = Library_Quantif.getCounts(imp);
        Map<String, Double> val = new HashMap<>();
        val.put("max", max);
        val.put("end", end);
        this.results.put("Liver Parenchyma", val);

        res = end/max;
        this.results.get("Other").put("Retention rate", res);

        return res;
    }

    public void setCounts(int sliceT1, int sliceT2) {
        ImagePlus imp = this.getImageSelection()[0].getImagePlus();

        for (String roiName: new String[]{"Liver", "Heart", "FOV"}) {
            if (roiName.equals("FOV"))
                imp.deleteRoi();
            else
                imp.setRoi(Library_Roi.getRoiByName(this.getRoiManager(), roiName));

            Map<String, Double> res = new HashMap<>();
            imp.setSlice(sliceT1);
            res.put("t1", Library_Quantif.getCounts(imp));
            imp.setSlice(sliceT2);
            res.put("t2", Library_Quantif.getCounts(imp));
            this.results.put(roiName, res);
        }
        this.results.get("Heart").put("AUC", this.getAUC(sliceT1, sliceT2));

    }

    @Override
    public void calculateResults() {
        int[] frameDurations = Library_Dicom.buildFrameDurations(this.getImagePlus());

        this.setFrameduration(frameDurations);
        double tracerDelayTime = this.getTracerDelayTime();
        int sliceT1 = getSliceIndexByTime((tracerDelayTime + 150) * 1000, frameDurations);
        int sliceT2 = getSliceIndexByTime((tracerDelayTime + 350) * 1000, frameDurations);

        this.setCounts(sliceT1, sliceT2);

        this.results.put("Other", new HashMap<>());
        this.getSC();
        this.getBPActivity();
        this.getClairanceFT();
        this.getNormalizedClairanceFT();
        this.getRetentionRate();
        if (this.results.containsKey("Tomo")) {
            this.getFFR();
            this.getClairanceFFR();
            this.getNormalizedClairanceFFR();
        }

        for (String roi: this.results.keySet()) {
            System.out.println(roi +":");
            for (String t: this.results.get(roi).keySet()) {
                System.out.println("\t"+ t +": "+ this.results.get(roi).get(t));
            }
        }
    }

    public double getTimeChart() {
        return timeChart;
    }

    public ImageSelection getImsRetention(){
        return imsRetention;
    }

    public void setTimeChart(double timeChart){
        this.timeChart = timeChart;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Map<String, Map<String, Double>> getResults() {
        return results;
    }

    public double getTracerDelayTime() {
        return tracerDelayTime;
    }

    public void setTracerDelayTime(double tracerDelayTime) {
        this.tracerDelayTime = tracerDelayTime;
    }

    public void setTomo(Map<String, Double> tomo) {
        this.results.put("Tomo", tomo);
    }
}
