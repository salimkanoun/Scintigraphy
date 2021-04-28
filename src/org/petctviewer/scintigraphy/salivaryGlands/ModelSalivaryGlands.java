package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.util.DicomTools;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
   public void setAdjustedValues(HashMap<Comparable, Double> hashMap) {
       this.adjustedValues = hashMap;
   }

   /**
    * renvoie le temps a tmax et t1/2
    * 
    * @return res[0][x] : tMax, res[1][x] : t1/2, res[x][0] : rein gauche,
    *         res[x][1] : rein droit
    */
   public Double[][] getTiming() {
       Double[][] res = new Double[2][2];

       if (true) {
           Double xMaxG = this.adjustedValues.get("tmax L");
           XYSeries lk = this.getSerie("Final KL");
           res[1][0] = Library_Quantif.round(Library_JFreeChart.getTDemiObs(lk, xMaxG), 1);
           res[0][0] = Library_Quantif.round(xMaxG, 2);
       } else {
           res[0][0] = Double.NaN;
           res[1][0] = Double.NaN;
       }

       if (true) {
           Double xMaxD = this.adjustedValues.get("tmax R");
           XYSeries rk = this.getSerie("Final KR");
           res[1][1] = Library_Quantif.round(Library_JFreeChart.getTDemiObs(rk, xMaxD), 1);
           res[0][1] = Library_Quantif.round(xMaxD, 2);
       } else {
           res[0][1] = Double.NaN;
           res[1][1] = Double.NaN;
       }

       return res;
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

   @SuppressWarnings("rawtypes")
   public HashMap<Comparable, Double> getAdjustedValues() {
       return this.adjustedValues;
   }

   public double getExcrBladder(Double bld) {
       XYSeries bldSeries = this.getSerie("Bladder");
       return 100 * bld / Library_JFreeChart.getY(bldSeries, bldSeries.getMaxX());
   }

   /**
    * Renvoie la hauteur des reins en cm, index 0 : rein gauche, 1 : rein droit
    */
   public Double[] getSize() {
       int heightLK=0;
       if (this.organRois.containsKey("L. Parotid")) {
            heightLK = this.organRois.get("L. Parotid").getBounds().height;
       }
       int heightRK =0;
       if (this.organRois.containsKey("R. Parotid")) {
            heightRK = this.organRois.get("R. Parotid").getBounds().height;
       }
       //r�cup�re la hauteur d'un pixel en mm
       //Calibration calibration=this.getImp().getLocalCalibration();
       //calibration.setUnit("mm");
       //Double pixelHeight=calibration.pixelHeight;
       ///System.out.println(pixelHeight);
       String pixelHeightString = DicomTools.getTag(this.getImagePlus(), "0028,0030").trim().split("\\\\")[1];
       double pixelHeight = Double.parseDouble(pixelHeightString);
       Double[] kidneyHeight = new Double[2];

       // convvertion des pixel en mm
       kidneyHeight[0] = Library_Quantif.round(heightLK * pixelHeight / 10, 2);
       kidneyHeight[1] = Library_Quantif.round(heightRK * pixelHeight / 10, 2);

       return kidneyHeight;
   }

   public ArrayList<String> getGlands() {
       return glands;
   }

   @SuppressWarnings("rawtypes")
   private String getDataString(Comparable key, String name) {
       if(this.getData().containsKey(key)) {
           StringBuilder nameBuilder = new StringBuilder(name);
           for(Double d : this.getData().get(key)) {
               nameBuilder.append(",").append(d);
           }
           name = nameBuilder.toString();
       }
       name += "\n";
       return name;
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

       this.uptakeRatio = new HashMap<>();
       this.excretionFraction = new HashMap<>();
       this.impAnt.getImagePlus().setRoi(this.organRois.get("Background"));
       //the average counts in the ipsilateral background reference between 10 and 20 min
       double avgBck = Library_Quantif.getAvgCounts(this.impAnt.getImagePlus());

       for (String s: this.getGlands()) {
           double max = 4;  //the highest count rate in the ninth or 10th minute
           double min = 1;  //the lowest count rate 2–4 min after lemon juice stimulation
           double ur = max/avgBck;
           double ef = 1 - min/max;

           this.uptakeRatio.put(s, ur);
           this.excretionFraction.put(s, ef);
       }

       System.out.println(this.uptakeRatio.toString());
       System.out.println(this.excretionFraction.toString());

       // ***INTEGRALE DE LA COURBE VASCULAIRE***
       // on recupere la liste des donnees vasculaires
       /*List<Double> vasc = this.getData("L. Parotid");
       XYSeries serieVasc = this.createSerie(vasc, "");
       List<Double> vascIntegree = Library_JFreeChart.getIntegralSummed(serieVasc, serieVasc.getMinX(), serieVasc.getMaxX());
       this.getData().put("BPI", vascIntegree); // BPI == Blood Pool Integrated*/
   }

   /** Contenu qui sera present lors de l'exprotation du CSV
    * (non-Javadoc)
    * @see org.petctviewer.scintigraphy.scin.model.ModelScin#toString()
    */
   @Override
   public String toString() {
       Double[][] excr = this.getExcr();
       Double[][] timing = this.getTiming();

       StringBuilder s = new StringBuilder(super.toString());
       
       s.append("\n");
       
       s.append(getDataString("Final L. Parotid", "Corrected Left Parotid"));
       s.append(getDataString("Final R. Parotid", "Corrected Right Parotid"));
       s.append(getDataString("Final L. Parotid", "Corrected Left Submandible"));
       s.append(getDataString("Final R. Parotid", "Corrected Right Submandible"));

       s.append("\n");
       s.append(",time, left kidney, right kidney \n");
       
       s.append("Timing tmax , ,").append(timing[0][0]).append(",").append(timing[0][1]).append("\n");
       s.append("Timing t1/2 , ,").append(timing[1][0]).append(",").append(timing[1][1]).append("\n");
       
       s.append("\n");
       
       
       
       // ROE
       Double xLasilix = this.adjustedValues.get("lasilix");
       Double[] time = {Library_Quantif.round(xLasilix - 1, 1),
               Library_Quantif.round(xLasilix + 2, 1), 
               Library_Quantif.round(this.getSerie("Blood Pool").getMaxX(), 1)};
       
       s.append(s.append(super.toString()));
       return s.toString();

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
   
   public int[] getFrameDurations() {
       return frameDurations;
   }
   
   public ImageSelection getImpAnt() {
       return impAnt;
   }

   public void enregistrerPixelRoi(String roiName, int pixelNumber) {
       this.pixelCounts.put(roiName, pixelNumber);
   }
   
   public int getPixelCount(String roiName) {
       return this.pixelCounts.get(roiName);
   }
   
   public HashMap<String, Integer> getPixelRoi() {
       return this.pixelCounts;
   }

   
}