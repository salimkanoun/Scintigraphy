package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.util.DicomTools;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelSalivaryGlands extends ModelScinDyn {

   private final HashMap<String, Roi> organRois;
   private HashMap<Comparable, Double> adjustedValues;
   private ArrayList<String> glands;
   private ImageSelection impAnt;
   private int[] frameDurations;
   private final HashMap<String, Integer> pixelCounts;
    private HashMap<String, Double> uptakeRatio;
    private HashMap<String, Double> excretionFraction;

   /**
    * recupere les valeurs et calcule les resultats de l'examen renal
    * 
    * @param frameDuration
    *            duree de chaque frame en ms
    */
   public ModelSalivaryGlands(int[] frameDuration, ImageSelection[] selectedImages, String studyName) {
       super(selectedImages, studyName, frameDuration);
       this.organRois = new HashMap<>();
       this.impAnt = selectedImages[1];

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
    * calcule le Excr selon le temps d'injection du lasilix
    *
    * @return res[0] : temps, res[1] : rein gauche, res[2] : rein droit
    */
   public Double[][] getExcr() {
       Double[][] res = new Double[3][3];

       // adjusted[6] => lasilix
       Double xLasilix = this.adjustedValues.get("lasilix");
       res[0][0] = Library_Quantif.round(xLasilix - 1, 1);
       res[0][1] = Library_Quantif.round(xLasilix + 2, 1);
       res[0][2] = Library_Quantif.round(this.getSerie("Blood Pool").getMaxX(), 1);

       for (String lr : this.glands) {
           XYSeries kidney = this.getSerie("Final Parotid" + lr);
           double max = kidney.getMaxY();

           // change l'index sur lequel ecrire le resultat dans le tableau
           int index = 1;
           if (lr.equals("R"))
               index = 2;

           // calcul Excr rein gauche
           for (int i = 0; i < 3; i++) {
               if (this.getAdjustedValues().get("tmax " + lr) < res[0][i]) {
                   res[index][i] = Library_Quantif.round(Library_JFreeChart.getY(kidney, res[0][i]) * 100 / max, 1);
               }
           }
       }

       return res;
   }

   /**
    * Renvoie la hauteur des reins en cm, index 0 : rein gauche, 1 : rein droit
    */
   public Map<String,Double> getSize() {
       String pixelHeightString = DicomTools.getTag(this.getImagePlus(), "0028,0030").trim().split("\\\\")[1];
       double pixelHeight = Double.parseDouble(pixelHeightString);

       Map<String, Double> glandsHeight = new HashMap<>();

       for (String gland: this.organRois.keySet()) {
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
           this.getData().get(nomRoi).add(Math.max(Library_Quantif.getCounts(imp),1.0d) );
       }
   }

   @Override
   public void calculateResults() {

       // construction du tableau representant chaque rein
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
       int lemonInjection = (int) Prefs.get("Lemon Injection",
               (this.adjustedValues.containsKey("Lemon Injection")) ? this.adjustedValues.get("Lemon Injection") : 10);

       int lemon = ModelScinDyn.getSliceIndexByTime(lemonInjection * 60 * 1000, this.frameDurations);
       int lemonMinus1 = ModelScinDyn.getSliceIndexByTime((lemonInjection - 1) * 60 * 1000, this.frameDurations);
       int lemonPlus2 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 2) * 60 * 1000, this.frameDurations);
       int lemonPlus4 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 4) * 60 * 1000, this.frameDurations);
       int lemonPlus10 = ModelScinDyn.getSliceIndexByTime((lemonInjection + 10) * 60 * 1000, this.frameDurations);

       this.uptakeRatio = new HashMap<>();
       this.excretionFraction = new HashMap<>();
       this.impAnt.getImagePlus().setRoi(this.organRois.get("Background"));
       //the average counts in the ipsilateral background reference between 10 and 20 min
       ImagePlus bkg10_20 = Library_Dicom.project(this.impAnt, lemon, lemonPlus10, "avg").getImagePlus();
       double avgBck = Library_Quantif.getCounts(bkg10_20) / Library_Quantif.getPixelNumber(bkg10_20);

       for (String s: this.getGlands()) {
           this.impAnt.getImagePlus().setRoi(this.organRois.get(s));

           //the highest count rate in the ninth or 10th minute
           double max = 0, prec = 0;
           ImagePlus impMax = null;
           for (int i = lemonPlus2; i <= lemonPlus4; i++) {
               this.impAnt.getImagePlus().setPosition(i);
               max = Math.max(max, Library_Quantif.getCounts(this.impAnt.getImagePlus()));
               if (prec != max)
                   impMax = this.impAnt.getImagePlus();
           }

           //the lowest count rate 2–4 min after lemon juice stimulation
           double min = Double.MAX_VALUE;
           for (int i = lemonMinus1; i <= lemon; i++) {
               this.impAnt.getImagePlus().setPosition(i);
               min = Math.min(min, Library_Quantif.getCounts(this.impAnt.getImagePlus()));
           }
           double ur = max/(avgBck * Library_Quantif.getPixelNumber(impMax));
           double ef = 1 - min/max;

           this.uptakeRatio.put(s, ur);
           this.excretionFraction.put(s, ef);
       }

       System.out.println(this);

       // ***INTEGRALE DE LA COURBE VASCULAIRE***
       // on recupere la liste des donnees vasculaires
       /*List<Double> vasc = this.getData("L. Parotid");
       XYSeries serieVasc = this.createSerie(vasc, "");
       List<Double> vascIntegree = Library_JFreeChart.getIntegralSummed(serieVasc, serieVasc.getMinX(), serieVasc.getMaxX());
       this.getData().put("BPI", vascIntegree); // BPI == Blood Pool Integrated*/
   }

    /**
     * renvoie la fonction separee
     *
     * @return res[0] : rein gauche, res[1] : rein droit
     */
    public Double[] getSeparatedFunction() {
        Double[] res = new Double[2];

        // points de la courbe renale ajustee
        XYSeries lk = this.getSerie("Final L. Parotid");
        XYSeries rk = this.getSerie("Final R. Parotid");

        // bornes de l'intervalle
        Double x1 = this.adjustedValues.get("start");
        Double x2 = this.adjustedValues.get("end");
        Double debut = Math.min(x1, x2);
        Double fin = Math.max(x1, x2);

        List<Double> listRG = Library_JFreeChart.getIntegralSummed(lk, debut, fin);
        List<Double> listRD = Library_JFreeChart.getIntegralSummed(rk, debut, fin);
        Double intRG = listRG.get(listRG.size() - 1);
        Double intRD = listRD.get(listRD.size() - 1);

        // Left kidney
        res[0] = Library_Quantif.round((intRG / (intRG + intRD)) * 100, 1);

        // Right kidney
        res[1] = Library_Quantif.round((intRD / (intRG + intRD)) * 100, 1);

        return res;
    }

   /** Contenu qui sera present lors de l'exprotation du CSV
    * (non-Javadoc)
    * @see org.petctviewer.scintigraphy.scin.model.ModelScin#toString()
    */
   @Override
   public String toString() {
       StringBuilder s = new StringBuilder(super.toString());
       
       s.append("\n");
       s.append(getDataString("Final L. Parotid", "Corrected Left Parotid"));
       s.append(getDataString("Final R. Parotid", "Corrected Right Parotid"));
       s.append(getDataString("Final L. SubMandib", "Corrected Left Submandible"));
       s.append(getDataString("Final R. SubMandib", "Corrected Right Submandible"));
       s.append("\n");

       StringBuilder name = new StringBuilder();
       StringBuilder ur = new StringBuilder("Uptake ratio");
       StringBuilder ef = new StringBuilder("Excretion fraction");
       for (String gland: this.getGlands()) {
           name.append(",").append(gland);
           ur.append(",").append(this.uptakeRatio.get(gland));
           ef.append(",").append(this.excretionFraction.get(gland));
       }
       s.append(name)
               .append("\n").append(ur)
               .append("\n").append(ef);

       return s.toString();
   }

    @SuppressWarnings("rawtypes")
    private String getDataString(Comparable key, String name) {
        StringBuilder nameBuilder = new StringBuilder(name);

        if(this.getData().containsKey(key)) {
            for(Double d : this.getData().get(key)) {
                nameBuilder.append(",").append(d);
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