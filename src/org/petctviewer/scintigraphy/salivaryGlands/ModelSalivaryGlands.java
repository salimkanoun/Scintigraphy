package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.*;

public class ModelSalivaryGlands extends ModelScinDyn {
    private final Map<String, Roi> organRois;
    private final Map<String, Integer> pixelNumberROIs;
    private final ArrayList<String> glands;
    private final ImageSelection impAnt;
    private final int[] frameDurations;
    private double lemonInjection;
    private Map<String, Map<String, Double>> results;

    /**
     * recupere les valeurs et calcule les resultats de l'examen renal
     *
     * @param frameDuration duree de chaque frame en ms
     */
    public ModelSalivaryGlands(int[] frameDuration, ImageSelection[] selectedImages, String studyName, ImageSelection impAnt) {
        super(selectedImages, studyName, frameDuration);
        this.impAnt = impAnt;
        this.frameDurations = frameDuration;

        this.organRois = new HashMap<>();
        this.pixelNumberROIs = new HashMap<>();

        this.glands = new ArrayList<>();
        this.glands.add("L. Parotid");
        this.glands.add("R. Parotid");
        this.glands.add("L. SubMandib");
        this.glands.add("R. SubMandib");
    }

    public void setLemonInjection(double lemonInjection) {
        this.lemonInjection = lemonInjection;
    }

    public double getLemonInjection() {
        return lemonInjection;
    }

    public Map<String, Map<String, Double>> getResults() {
        return results;
    }

    public ArrayList<String> getGlands() {
        return glands;
    }

    public int[] getFrameDurations() {
        return frameDurations;
    }

    public ImageSelection getImpAnt() {
        return impAnt;
    }

    public void savePixelNumberROIs(ImagePlus imp) {
        for (Roi r : this.getRoiManager().getRoisAsArray()) {
            String roiName = r.getName();
            imp.setRoi(r);
            int pixelNumber = Library_Quantif.getPixelNumber(imp);
            this.pixelNumberROIs.put(roiName, pixelNumber);
        }
    }

    public Map<String, Integer> getPixelNumberROIs() {
        return this.pixelNumberROIs;
    }

    public int getPixelNumberROI(String roiName) {
        return this.pixelNumberROIs.get(roiName);
    }

    /**
     * Getter for the size of the salivary glands in cm
     *
     * @return a map containing size of each glands where key is the name of the salivary gland
     * @see #getGlands
     */
    public Map<String, Double> getSize() {
        String pixelHeightString = DicomTools.getTag(this.getImagePlus(), "0028,0030").trim().split("\\\\")[1];
        double pixelHeight = Double.parseDouble(pixelHeightString);

        Map<String, Double> glandsHeight = new HashMap<>();

        for (String gland : this.organRois.keySet()) {
            //get & convert height to mm
            int height = this.organRois.get(gland).getBounds().height;
            glandsHeight.put(gland, Library_Quantif.round(height * pixelHeight / 10, 2));
        }

        return glandsHeight;
    }

    public void saveOrganRois() {
        for (Roi r : this.getRoiManager().getRoisAsArray())
            this.organRois.put(r.getName(), r);
    }

    public void enregistrerMesure(ImagePlus imp, int slice) {
        if (this.isUnlocked()) {
            imp.setSlice(slice);
            Roi backgroundRoi = this.organRois.get("Background");
            for (Roi r : this.getRoiManager().getRoisAsArray()) {
                String roiName = r.getName();

                this.getData().computeIfAbsent(roiName, k -> new ArrayList<>());
                // on y ajoute le nombre de coups
                double counts = Library_Quantif.getCountCorrectedBackground(imp, r, backgroundRoi);
                double finalCounts = counts / (this.frameDurations[slice - 1] / 1000.0);
                this.getData().get(roiName).add(Math.max(finalCounts, 1));
            }
        }
    }

    @Override
    public void calculateResults() {
        // on soustrait le bruit de fond
        this.substractBkg();

        //time of lemon juice stimulation in minute
        double lemonInjection = this.getLemonInjection();

        int firstMinute = ModelScinDyn.getSliceIndexByTime(60 * 1000, this.frameDurations);
        int lemon15 = ModelScinDyn.getSliceIndexByTime(15 * 60 * 1000, this.frameDurations);

        int lemonMinus1 = ModelScinDyn.getSliceIndexByTime((lemonInjection - 1) * 60 * 1000, this.frameDurations);
        int lemon = ModelScinDyn.getSliceIndexByTime(lemonInjection * 60 * 1000, this.frameDurations);
        int lemonPlus2 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 2) * 60 * 1000, this.frameDurations);
        int lemonPlus4 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 4) * 60 * 1000, this.frameDurations);

        this.results = new HashMap<>();

        ImagePlus imp = this.impAnt.getImagePlus();
        Roi backgroundRoi = this.organRois.get("Background");
        imp.setRoi(backgroundRoi);
        //the average counts in the ipsilateral background reference between 10 and 20 min
        double avgBck = Library_Quantif.getAvgCounts(imp) / Library_Quantif.getPixelNumber(imp);

        for (String s : this.getGlands()) {
            List<Double> a = this.getData(s);
            Roi roi = this.organRois.get(s);
            imp.setRoi(roi);
            Map<String, Double> glandResults = new HashMap<>();
            this.results.put(s, glandResults);

            //the highest count rate in the ninth or 10th minute (when lemon stimuli is at 10min)
            double max = 0;
            //the lowest count rate 2–4 min after lemon juice stimulation
            double min = Double.MAX_VALUE;
            //maximum counts/sec during the whole activity
            double maxGlobal = 0;
            //minimum counts/sec after lemon injection
            double minAfterLemon = Double.MAX_VALUE;
            for (int i = 1; i <= imp.getNSlices(); i++) {
                imp.setSlice(i);

                maxGlobal = Math.max(maxGlobal, Library_Quantif.getCounts(imp) / (this.frameDurations[i - 1] / 1000.0));

                if (i >= lemonMinus1 && i <= lemon)
                    max = Math.max(max, Library_Quantif.getCounts(imp) / (this.frameDurations[i - 1] / 1000.0));

                if (i >= lemonPlus2 && i <= lemonPlus4)
                    min = Math.min(min, Library_Quantif.getCounts(imp) / (this.frameDurations[i - 1] / 1000.0));

                if (i >= lemon)
                    minAfterLemon = Math.min(minAfterLemon, Library_Quantif.getCounts(imp) / (this.frameDurations[i - 1] / 1000.0));
            }

            double ur = Library_Quantif.round(max / (avgBck * Library_Quantif.getPixelNumber(imp)), 1);
            double ef = Library_Quantif.round((1 - min / max) * 100, 1);

            double fm = Library_Quantif.getAvgCounts(imp, 1, firstMinute) / (this.frameDurations[0] / 1000.0);
            double lm = Library_Quantif.getAvgCounts(imp,imp.getNSlices() - 60000 / this.frameDurations[0], imp.getNSlices()) / (this.frameDurations[0] / 1000.0);

            imp.setSlice(lemon15);
            double m15 = Library_Quantif.getCounts(imp) / (this.frameDurations[0] / 1000.0);
            imp.setSlice(lemon);
            double lemonCounts = Library_Quantif.getCounts(imp) / (this.frameDurations[0] / 1000.0);

            glandResults.put("Uptake Ratio", ur);
            glandResults.put("Excretion Fraction", ef);
            glandResults.put("First Minute", fm);
            glandResults.put("Last Minute", lm);
            glandResults.put("15 Minutes", m15);
            glandResults.put("Lemon", lemonCounts);
            glandResults.put("Maximum", Library_Quantif.round(maxGlobal, 1));
            glandResults.put("Minimum", Library_Quantif.round(minAfterLemon, 1));
        }
    }

    /**
     * Contenu qui sera present lors de l'exprotation du CSV
     * (non-Javadoc)
     *
     * @see org.petctviewer.scintigraphy.scin.model.ModelScin#toString()
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());

        s.append("\n\n");
        s.append(getDataString("L. Parotid", "Left Parotid"));
        s.append(getDataString("R. Parotid", "Right Parotid"));
        s.append(getDataString("L. SubMandib", "Left Submandible"));
        s.append(getDataString("R. SubMandib", "Right Submandible"));
        s.append("\n");

        s.append("Lemon juice stimulation at: ").append(Library_Quantif.round(this.getLemonInjection(), 1)).append(" min\n\n");

        StringBuilder name = new StringBuilder();
        StringBuilder ur = new StringBuilder("Uptake ratio");
        StringBuilder ef = new StringBuilder("Excretion fraction");
        StringBuilder fm_max = new StringBuilder("FM/Max");
        StringBuilder max_min = new StringBuilder("Max/Min");
        StringBuilder max_lemon = new StringBuilder("Max/Lemon");
        StringBuilder m15_lemon = new StringBuilder("15min/Lemon");

        for (String gland : this.getGlands()) {
            Map<String, Double> res = this.results.get(gland);
            name.append(",").append(gland);
            ur.append(",").append(res.get("Uptake Ratio"));
            ef.append(",").append(res.get("Excretion Fraction")).append("%");
            fm_max.append(",").append(Library_Quantif.round(res.get("First Minute") / res.get("Maximum"), 2)).append("%");
            max_min.append(",").append(Library_Quantif.round(res.get("Maximum") / res.get("Minimum"), 2)).append("%");
            max_lemon.append(",").append(Library_Quantif.round(res.get("Maximum") / res.get("Lemon"), 2)).append("%");
            m15_lemon.append(",").append(Library_Quantif.round(res.get("15 Minutes") / res.get("Lemon"), 2)).append("%");
        }

        s.append(name)
                .append("\n").append(ur)
                .append("\n").append(ef)
                .append("\n").append(fm_max)
                .append("\n").append(max_min)
                .append("\n").append(max_lemon)
                .append("\n").append(m15_lemon);

        return s.toString();
    }

    private String getDataString(String key, String name) {
        StringBuilder nameBuilder = new StringBuilder(name);

        if (this.getData().containsKey(key)) {
            for (Double d : this.getData().get(key)) {
                nameBuilder.append(",").append(Library_Quantif.round(d, 2));
            }
            nameBuilder.append("\n");
        }
        return nameBuilder.toString();
    }

    private void substractBkg() {
        int aireBkg = this.organRois.get("Background").getStatistics().pixelCount;
        List<Double> lbkg = this.getData("Background");

        // ***VALEURS AJUSTEES AVEC LE BRUIT DE FOND POUR CHAQUE GLANDE***
        for (String r : glands) { // pour chaque glande
            List<Double> glandeCorrigee = new ArrayList<>();
            // on recupere l'aire des rois bruit de fond et rein
            int aireGlande = this.organRois.get(r).getStatistics().pixelCount;
            List<Double> lg = this.getData(r);

            // on calcule le coup moyen de la roi, on l'ajuste avec le bdf et on l'applique
            // sur toute la roi pour chaque glande afin d'ajuster leur valeur brut
            for (int i = 0; i < this.getFrameDuration().length; i++) {
                Double countGlande = lg.get(i);
                Double countBkg = lbkg.get(i);

                double moyGlande = countGlande / aireGlande;
                double moyBkg = countBkg / aireBkg;
                Double adjustedValueG = (moyGlande - moyBkg) * aireGlande;
                glandeCorrigee.add(adjustedValueG);
            }

            // on ajoute les nouvelles valeurs aux donnees
            this.getData().put("Final " + r, glandeCorrigee);
        }

    }
}