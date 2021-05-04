package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.*;

public class ModelSalivaryGlands extends ModelScinDyn {

    private final HashMap<String, Roi> organRois;
    private HashMap<Comparable, Double> adjustedValues;
    private ArrayList<String> glands;
    private JValueSetter citrusChart;

    private ImageSelection impAnt;
    private int[] frameDurations;
    private final HashMap<String, Integer> pixelCounts;
    private HashMap<String, Double> uptakeRatio;
    private HashMap<String, Double> excretionFraction;
    private Map<String, Double> minCountsAfterLemon;
    private Map<String, Double> maxCounts;

    /**
     * recupere les valeurs et calcule les resultats de l'examen renal
     *
     * @param frameDuration duree de chaque frame en ms
     */
    public ModelSalivaryGlands(int[] frameDuration, ImageSelection[] selectedImages, String studyName, ImageSelection impAnt) {
        super(selectedImages, studyName, frameDuration);
        this.organRois = new HashMap<>();
        this.impAnt = impAnt;

        this.frameDurations = frameDuration;

        this.pixelCounts = new HashMap<>();
    }

    @SuppressWarnings("rawtypes")
    public HashMap<Comparable, Double> getAdjustedValues() {
        return this.adjustedValues;
    }

    @SuppressWarnings("rawtypes")
    public void setAdjustedValues(HashMap<Comparable, Double> hashMap) {
        this.adjustedValues = hashMap;
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

    public int getPixelCount(String roiName) {
        return this.pixelCounts.get(roiName);
    }

    public void enregistrerPixelRoi(String roiName, int pixelNumber) {
        this.pixelCounts.put(roiName, pixelNumber);
    }

    public HashMap<String, Integer> getPixelRoi() {
        return this.pixelCounts;
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

    /********** Public *********/
    public void enregistrerMesure(String nomRoi, ImagePlus imp) {
        if (this.isUnlocked()) {
            this.organRois.put(nomRoi, imp.getRoi());

            this.getData().computeIfAbsent(nomRoi, k -> new ArrayList<>());
            // on y ajoute le nombre de coups
            this.getData().get(nomRoi).add(Math.max(Library_Quantif.getCounts(imp), 1));
        }
    }

    public HashMap<String, Double> getUptakeRatio() {
        return uptakeRatio;
    }

    public HashMap<String, Double> getExcretionFraction() {
        return excretionFraction;
    }

    @Override
    public void calculateResults() {

        // construction du tableau representant chaque glande
        this.glands = new ArrayList<>();
        this.glands.add("L. Parotid");
        this.glands.add("R. Parotid");
        this.glands.add("L. SubMandib");
        this.glands.add("R. SubMandib");

        // on ajuste toutes les valeurs pour les mettre en coup / sec
        for (String k : this.getData().keySet()) {
            List<Double> data = this.getData().get(k);
            this.getData().put(k, this.adjustValues(data));
        }

        // on soustrait le bruit de fond
        this.substractBkg();

        //time of lemon juice stimulation in minute
        double lemonInjection = this.adjustedValues.get("lemon");

        int firstMinute = ModelScinDyn.getSliceIndexByTime(60 * 1000, this.frameDurations);
        int lemonMinus5 = ModelScinDyn.getSliceIndexByTime((lemonInjection - 5) * 60 * 1000, this.frameDurations);
        int lemonMinus1 = ModelScinDyn.getSliceIndexByTime((lemonInjection - 1) * 60 * 1000, this.frameDurations);
        int lemon = ModelScinDyn.getSliceIndexByTime(lemonInjection * 60 * 1000, this.frameDurations);
        int lemonPlus2 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 2) * 60 * 1000, this.frameDurations);
        int lemonPlus4 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 4) * 60 * 1000, this.frameDurations);
        int lemonPlus10 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 10) * 60 * 1000, this.frameDurations);

        this.uptakeRatio = new HashMap<>();
        this.excretionFraction = new HashMap<>();
        this.maxCounts = new HashMap<>();
        this.minCountsAfterLemon = new HashMap<>();
        this.impAnt.getImagePlus().setRoi(this.organRois.get("Background"));
        //the average counts in the ipsilateral background reference between 10 and 20 min
        ImagePlus bkg10_20 = Library_Dicom.project(this.impAnt, lemon, lemonPlus10, "avg").getImagePlus();
        double avgBck = Library_Quantif.getCounts(bkg10_20) / Library_Quantif.getPixelNumber(bkg10_20);

        for (String s : this.getGlands()) {
            List<Double> a = this.getData(s);
            this.impAnt.getImagePlus().setRoi(this.organRois.get(s));

            //the highest count rate in the ninth or 10th minute
            double max = 0, prec = 0;
            ImagePlus impMax = null;
            for (int i = lemonPlus2; i <= lemonPlus4; i++) {
                this.impAnt.getImagePlus().setSlice(i);
                max = Math.max(max, Library_Quantif.getCounts(this.impAnt.getImagePlus()));
                if (prec != max) {
                    impMax = this.impAnt.getImagePlus();
                    prec = max;
                }
            }

            //the lowest count rate 2–4 min after lemon juice stimulation
            double min = Double.MAX_VALUE;
            for (int i = lemonMinus1; i <= lemon; i++) {
                this.impAnt.getImagePlus().setSlice(i);
                min = Math.min(min, Library_Quantif.getCounts(this.impAnt.getImagePlus()));
            }
            double ur = Library_Quantif.round(max / (avgBck * Library_Quantif.getPixelNumber(impMax)), 1);

            double ef = Library_Quantif.round((1 - min / max) * 100, 1);


            this.uptakeRatio.put(s, ur);
            this.excretionFraction.put(s, ef);

            this.impAnt.getImagePlus().setSlice(firstMinute);
            double firstMinuteCounts = Library_Quantif.getCounts(this.impAnt.getImagePlus());
            this.impAnt.getImagePlus().setSlice(lemonMinus5);
            double lemonMinus5Counts = Library_Quantif.getCounts(this.impAnt.getImagePlus());
            this.impAnt.getImagePlus().setSlice(lemon);
            double lemonStimuliCounts = Library_Quantif.getCounts(this.impAnt.getImagePlus());

            //search min & max after lemon injection
            double maxAL = 0, minAL = Double.MAX_VALUE;
            for (int i = 0; i < this.impAnt.getImagePlus().getNSlices(); i++) {
                this.impAnt.getImagePlus().setSlice(i);
                maxAL = Math.max(maxAL, Library_Quantif.getCounts(this.impAnt.getImagePlus()));
                if (i >= lemon)
                    minAL = Math.min(minAL, Library_Quantif.getCounts(this.impAnt.getImagePlus()));
            }
            this.maxCounts.put(s, Library_Quantif.round(maxAL, 1));
            this.minCountsAfterLemon.put(s, Library_Quantif.round(minAL, 1));
        }


        //TODO To delete
        System.out.println(this);
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
        s.append(getDataString("Final L. Parotid", "Corrected Left Parotid"));
        s.append(getDataString("Final R. Parotid", "Corrected Right Parotid"));
        s.append(getDataString("Final L. SubMandib", "Corrected Left Submandible"));
        s.append(getDataString("Final R. SubMandib", "Corrected Right Submandible"));
        s.append("\n");

        s.append("Lemon juice stimulation at: ").append(Library_Quantif.round(this.adjustedValues.get("lemon"), 1)).append(" min\n\n");

        StringBuilder name = new StringBuilder();
        StringBuilder ur = new StringBuilder("Uptake ratio");
        StringBuilder ef = new StringBuilder("Excretion fraction");
        StringBuilder max = new StringBuilder("Maximum");
        StringBuilder min = new StringBuilder("Min after lemon stimuli");
        for (String gland : this.getGlands()) {
            name.append(",").append(gland);
            ur.append(",").append(this.uptakeRatio.get(gland));
            ef.append(",").append(this.excretionFraction.get(gland));
            max.append(",").append(this.maxCounts.get(gland));
            min.append(",").append(this.minCountsAfterLemon.get(gland));
        }
        s.append(name)
                .append("\n").append(ur)
                .append("\n").append(ef)
                .append("\n").append(max)
                .append("\n").append(min);

        return s.toString();
    }

    @SuppressWarnings("rawtypes")
    private String getDataString(Comparable key, String name) {
        StringBuilder nameBuilder = new StringBuilder(name);

        if (this.getData().containsKey(key)) {
            for (Double d : this.getData().get(key)) {
                nameBuilder.append(",").append(Library_Quantif.round(d,2));
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

    public JValueSetter getCitrusChart() {
        return citrusChart;
    }

    public void setCitrusChart(JValueSetter citrusChart) {
        this.citrusChart = citrusChart;
    }
}