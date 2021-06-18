package org.petctviewer.scintigraphy.hepatic.scintivol;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Model_Scintivol extends ModelScinDyn {
    private final Map<String, Roi> organRois;
    private final Map<String, Map<String, Double>> results;
    private final ImageSelection imsRetention;
    private double timeChart;
    private double tracerDelayTime;
    private final Map<String, Integer> pixelCounts;
    private double size;
    private double weight;
    private final ArrayList<String> organes;


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
        this.organes.add("Intermediate values");
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


    private void getClairanceFT() {
        double L_t1 = this.results.get("Liver").get("t1");
        double L_t2 = this.results.get("Liver").get("t2");


        double H_t1 = this.results.get("Heart").get("t1");

        double A_t1 = this.results.get("Intermediate values").get("BP Activity");

        double AUC_t1_t2 = this.results.get("Heart").get("AUC");


        this.results.get("Intermediate values").put("AUC/Cnorm", AUC_t1_t2/H_t1);

        double res = 100 * 6 * (L_t2 - L_t1) / (A_t1 * AUC_t1_t2/H_t1);
        this.results.get("Intermediate values").put("Clairance FT",  res);

    }

    private void getNormalizedClairanceFT() {
        double clairanceFT = this.results.get("Intermediate values").get("Clairance FT");
        double sc = this.results.get("Intermediate values").get("SC");

        double res = clairanceFT / sc;
        this.results.get("Intermediate values").put("Norm Clairance FT", res);
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
            res += Library_Quantif.getCounts(imp) * 60;
        }

        return res;
    }

    private void getBPActivity() {
        double T_t1 = this.results.get("FOV").get("t1");
        double T_t2 = this.results.get("FOV").get("t2");

        double L_t1 = this.results.get("Liver").get("t1");

        double Cnorm_t2 = this.results.get("Heart").get("t2") / this.results.get("Heart").get("t1");
        this.results.get("Intermediate values").put("Cnorm_t2", Cnorm_t2);

        double res = (T_t2 - L_t1 - (T_t1 - L_t1) * Cnorm_t2) / (1 - Cnorm_t2);
        this.results.get("Intermediate values").put("BP Activity", res);
    }

    private void getSC() {
        double res = Math.sqrt((this.size * this.weight) / 3600);
        this.results.get("Intermediate values").put("SC", res);

    }

    /**
     * Futur foie restant
     */
    private void getFFR() {
        double ft = this.results.get("Tomo").get("FT");     //Total liver
        double ffr = this.results.get("Tomo").get("FFR");   // Future remaining liver

        double res = ffr/ft;
        this.results.get("Intermediate values").put("FFR/FT", res);
    }

    private void getClairanceFFR() {
        double clairanceFT = this.results.get("Intermediate values").get("Clairance FT");
        double ffr_ft = this.results.get("Intermediate values").get("FFR/FT");

        double res = clairanceFT * ffr_ft;
        this.results.get("Intermediate values").put("Clairance FFR", res);
    }

    private void getNormalizedClairanceFFR() {
        double clairanceFTNorm = this.results.get("Intermediate values").get("Norm Clairance FT");
        double ffr_ft = this.results.get("Intermediate values").get("FFR/FT");

        double res = clairanceFTNorm * ffr_ft;
        this.results.get("Intermediate values").put("Norm Clairance FFR", res);
    }

    private void getRetentionRate() {
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
        this.results.get("Intermediate values").put("Retention rate", res);

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
            res.put("t1", Library_Quantif.getCounts(imp) * 60);
            imp.setSlice(sliceT2);
            res.put("t2", Library_Quantif.getCounts(imp) * 60);
            this.results.put(roiName, res);
        }
        this.results.get("Heart").put("AUC", this.getAUC(sliceT1, sliceT2));

        this.getRetentionRate();

    }

    @Override
    public void calculateResults() {
        double tracerDelayTime = this.getTracerDelayTime();
        int sliceT1 = getSliceIndexByTime((tracerDelayTime + 150) * 1000, this.getFrameDuration());
        int sliceT2 = getSliceIndexByTime((tracerDelayTime + 350) * 1000, this.getFrameDuration());

        this.results.put("Intermediate values", new HashMap<>());
        
        this.setCounts(sliceT1, sliceT2);

        this.getSC();
        this.getBPActivity();
        this.getClairanceFT();
        this.getNormalizedClairanceFT();
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

    @Override
    public String toString() {
        double Ht1 = Library_Quantif.round(this.results.get("Heart").get("t1"),2);
        double Ht2 = Library_Quantif.round(this.results.get("Heart").get("t2"),2);
        double Lv1 = Library_Quantif.round(this.results.get("Liver").get("t1"),2);
        double Lv2 = Library_Quantif.round(this.results.get("Liver").get("t2"),2);
        double Fov1 = Library_Quantif.round(this.results.get("FOV").get("t1"),2);
        double Fov2 = Library_Quantif.round(this.results.get("FOV").get("t2"),2);
        double lvP1 = Library_Quantif.round(this.results.get("Liver Parenchyma").get("max"),2);
        double lvP2 = Library_Quantif.round(this.results.get("Liver Parenchyma").get("end"),2);
        double cnorm = Library_Quantif.round(this.results.get("Intermediate values").get("Cnorm_t2"),2);
        double auc = Library_Quantif.round(this.results.get("Heart").get("AUC"),2);
        double ft = Library_Quantif.round(this.results.get("Intermediate values").get("Clairance FT"),2);
        double ftNorm = Library_Quantif.round(this.results.get("Intermediate values").get("Norm Clairance FT"),2);
        double retention = Library_Quantif.round(this.results.get("Intermediate values").get("Retention rate")*100,2);

        StringBuilder res = new StringBuilder(super.toString());

        res.append("\n\n\n");
        res.append("t1,").append(this.tracerDelayTime + 150).append(",s\n");
        res.append("t2,").append(this.tracerDelayTime + 350).append(",s\n\n");

        res.append("Heart").append("\n");
        res.append("t1,").append(Ht1).append(",counts/min\n");
        res.append("t2,").append(Ht2).append(",counts/min\n\n");

        res.append("Liver").append("\n");
        res.append("t1,").append(Lv1).append(",counts/min\n");
        res.append("t2,").append(Lv2).append(",counts/min\n\n");

        res.append("FOV counts").append("\n");
        res.append("t1,").append(Fov1).append(",counts/min\n");
        res.append("t2,").append(Fov2).append(",counts/min\n\n");

        res.append("Liver Parenchyma counts").append("\n");
        res.append("max,").append(lvP1).append(",counts/min\n");
        res.append("end,").append(lvP2).append(",counts/min\n\n");

        res.append("Intermediate values").append("\n");
        res.append("Cnormt2,").append(cnorm).append("\n");
        res.append("AUC,").append(auc).append(",counts between t1 and t2\n\n");

        res.append("Results").append("\n");
        res.append("FL Clearance,").append(ft).append(",%/min\n");
        res.append("Normalized FL Clearance,").append(ftNorm).append(",%/min.m²\n");
        res.append("Retention rate,").append(retention).append(",%\n");

        if (this.results.containsKey("Tomo")) {
            double fl_frl = Library_Quantif.round(this.results.get("Intermediate values").get("FFR/FT") * 100,2);
            double ffr = Library_Quantif.round(this.results.get("Intermediate values").get("Clairance FFR"),2);
            double ffrNorm = Library_Quantif.round(this.results.get("Intermediate values").get("Norm Clairance FFR"),2);

            res.append("FL/FRL,").append(fl_frl).append(",%\n");
            res.append("Clearance FRL,").append(ffr).append(",%/min\n");
            res.append("Normalized clearance FRL,").append(ffrNorm).append(",%/min.m²\n");
        }

        return res.toString();
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
