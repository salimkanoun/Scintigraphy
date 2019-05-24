 package org.petctviewer.scintigraphy.renal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.util.DicomTools;

public class Model_Renal extends ModelScinDyn {

	private HashMap<String, Roi> organRois;
	private HashMap<Comparable, Double> adjustedValues;
	private boolean[] kidneys;
	private double[] patlakPente;
	private ArrayList<String> kidneysLR;
	private JValueSetter nephrogramChart;
	private JValueSetter patlakChart;
	private ImageSelection impAnt, impPost/*, impProjetee*/;
	private int[] frameDurations;
	private HashMap<String, Integer> pixelCounts;

	/**
	 * recupere les valeurs et calcule les resultats de l'examen renal
	 * 
	 * @param frameDuration
	 *            duree de chaque frame en ms
	 */
	public Model_Renal(int[] frameDuration, ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName, frameDuration);
		this.organRois = new HashMap<>();
//		this.impProjetee = selectedImages[0];
		this.impPost = selectedImages[1];
		if(selectedImages.length > 2)
			this.impAnt = selectedImages[2];
		
		this.pixelCounts = new HashMap<>();
	}


	/********* Public Setter *********/
	public void setKidneys(boolean[] kidneys) {
		this.kidneys = kidneys;
	}

	public void setAdjustedValues(HashMap<Comparable, Double> hashMap) {
		this.adjustedValues = hashMap;
	}

	public void setPatlakPente(double[] patlakRatio) {
		this.patlakPente = patlakRatio;
	}
	
	
	/******* Public Getter **********/
	public boolean[] getKidneys() {
		return this.kidneys;
	}

	/**
	 * renvoie le roe en pourcent
	 * 
	 * @param min
	 *            minute a laquelle on veut comparer
	 * @param output
	 *            sortie du rein
	 * @param lr
	 *            "L" ou "R"
	 * @return le pourcentage
	 */
	public int getROE(Double min, String lr) {
		int perct =0;
		try {
			XYSeries output = this.getSerie("Output K" + lr);
			XYSeries serieBPF = this.getSerie("Blood pool fitted " + lr);
			 perct = (int) (Library_JFreeChart.getY(output, min).doubleValue()
					/ Library_JFreeChart.getY(serieBPF, min).doubleValue() * 100);
		} catch (IllegalArgumentException e) {
		}
		
		return perct;
	}

	/**
	 * renvoie le temps a tmax et t1/2
	 * 
	 * @return res[0][x] : tMax, res[1][x] : t1/2, res[x][0] : rein gauche,
	 *         res[x][1] : rein droit
	 */
	public Double[][] getTiming() {
		Double[][] res = new Double[2][2];

		if (this.kidneys[0]) {
			Double xMaxG = this.adjustedValues.get("tmax L");
			XYSeries lk = this.getSerie("Final KL");
			res[1][0] = Library_Quantif.round(Library_JFreeChart.getTDemiObs(lk, xMaxG), 1);
			res[0][0] = Library_Quantif.round(xMaxG, 2);
		} else {
			res[0][0] = Double.NaN;
			res[1][0] = Double.NaN;
		}

		if (this.kidneys[1]) {
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
	 * 
	 * @param rg
	 *            coups du rein gauche en coups/sec
	 * @param rd
	 *            coups du rein droit en coups/sec
	 * @return res[0] : rein gauche, res[1] : rein droit, res[x][0] : max, res[x][1]
	 *         : lasilix - 1
	 */
	public Double[][] getExcrPM(Double rg, Double rd) {
		// tableau de retour avec les resultats
		Double[][] res = new Double[2][2];

		Double xLasilixM1 = this.adjustedValues.get("lasilix") - 1;

		// si il y a un rein gauche
		if (this.kidneys[0]) {
			Double xMaxL = this.adjustedValues.get("tmax L");
			res[0][0] = Library_Quantif.round((100 * rg / Library_JFreeChart.getY(this.getSerie("Final KL"), xMaxL)), 2);
			res[0][1] = Library_Quantif.round((100 * rg / Library_JFreeChart.getY(this.getSerie("Final KL"), xLasilixM1)), 2);

		}

		// si il y a un rein droit
		if (this.kidneys[1]) {
			Double xMaxR = this.adjustedValues.get("tmax R");
			res[1][0] = Library_Quantif.round((100 * rd / Library_JFreeChart.getY(this.getSerie("Final KR"), xMaxR)), 2);
			res[1][1] = Library_Quantif.round((100 * rd / Library_JFreeChart.getY(this.getSerie("Final KR"), xLasilixM1)), 2);
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

		for (String lr : this.kidneysLR) {
			XYSeries kidney = this.getSerie("Final K" + lr);
			Double max = kidney.getMaxY();

			// change l'index sur lequel ecrire le resultat dans le tableau
			int index = 1;
			if (lr == "R")
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

	public Double[][] getNora() {
		Double[][] res = new Double[3][3];

		// adjusted[6] => lasilix
		Double xLasilix = this.adjustedValues.get("lasilix");
		res[0][0] = Library_Quantif.round(xLasilix - 1, 1);
		res[0][1] = Library_Quantif.round(xLasilix + 2, 1);
		res[0][2] = Library_Quantif.round(this.getSerie("Blood Pool").getMaxX(), 1);

		for (String lr : this.kidneysLR) {
			XYSeries kidney = this.getSerie("Final K" + lr);

			// change l'index sur lequel ecrire le resultat dans le tableau
			int index = 1;
			if (lr == "R")
				index = 2;

			// calcul nora rein gauche
			for (int i = 0; i < 3; i++) {
				res[index][i] = Library_Quantif.round(Library_JFreeChart.getY(kidney, res[0][i]) * 100 / Library_JFreeChart.getY(kidney, 2.0), 1);
			}
		}

		return res;
	}

	public HashMap<Comparable, Double> getAdjustedValues() {
		return this.adjustedValues;
	}

	/**
	 * renvoie la fonction separee
	 * 
	 * @return res[0] : rein gauche, res[1] : rein droit
	 */
	public Double[] getSeparatedFunction() {
		Double[] res = new Double[2];

		boolean[] kidneys = this.getKidneys();
		if (kidneys[0] && kidneys[1]) {
			
			// points de la courbe renale ajustee
			XYSeries lk = this.getSerie("Final KL");
			XYSeries rk = this.getSerie("Final KR");
	
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
		}else {
			if (kidneys[0]) {res[0] = 100D; res[1] =0D;}
			else if(kidneys[1]){res[0] = 0D; res[1] =100D;}
			
		}
		return res;
	}

	public double[] getPatlakPente() {
		return this.patlakPente;
	}

	public double getExcrBladder(Double bld) {
		XYSeries bldSeries = this.getSerie("Bladder");
		return 100 * bld / Library_JFreeChart.getY(bldSeries, bldSeries.getMaxX());
	}

	/**
	 * Renvoie la hauteur des reins en cm, index 0 : rein gauche, 1 : rein droit
	 * 
	 * @return
	 */
	public Double[] getSize() {
		int heightLK=0;
		if (this.organRois.containsKey("L. Kidney")) {
			 heightLK = this.organRois.get("L. Kidney").getBounds().height;
		}
		int heightRK =0;
		if (this.organRois.containsKey("R. Kidney")) {
			 heightRK = this.organRois.get("R. Kidney").getBounds().height;
		}
		//r�cup�re la hauteur d'un pixel en mm
		//Calibration calibration=this.getImp().getLocalCalibration();
		//calibration.setUnit("mm");
		//Double pixelHeight=calibration.pixelHeight;
		///System.out.println(pixelHeight);
		String pixelHeightString = DicomTools.getTag(this.getImagePlus(), "0028,0030").trim().split("\\\\")[1];
		Double pixelHeight = Double.parseDouble(pixelHeightString);
		Double[] kidneyHeight = new Double[2];

		// convvertion des pixel en mm
		kidneyHeight[0] = Library_Quantif.round(heightLK * pixelHeight / 10, 2);
		kidneyHeight[1] = Library_Quantif.round(heightRK * pixelHeight / 10, 2);

		return kidneyHeight;
	}

	public ArrayList<String> getKidneysLR() {
		return kidneysLR;
	}

	
	/******* Private Getter *********/
	private String getDataString(Comparable key, String name) {
		if(this.getData().containsKey(key)) {
			List<Double> values = this.getData().get(key);
			for(Double d : values) {
				name += "," + d;
			}
		}
		name += "\n";
		return name;
	}

	private String getROEString() {
		String s = "Time ROE";
		Double[] mins = new Double[10];
		for (int i = 0; i < mins.length; i++) {
			mins[i] = Library_Quantif.round((getSerie("Blood Pool").getMaxX() / (mins.length * 1.0)) * i + 1, 1);
			s += ", " + mins[i];
		}
		s += "\n";

		// on recupere les series
		for (String lr : this.kidneysLR) {
			s += lr + ". kidney";
			for (int i = 0; i < mins.length; i++) {
				s += "," + this.getROE(mins[i], lr);
			}
			s += "\n";
		}
		
		return s;
	}
	
	
	/********** Public *********/
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		if (!this.isLocked()) {

			this.organRois.put(nomRoi, imp.getRoi());
			
			if (this.getData().get(nomRoi) == null) 
				this.getData().put(nomRoi, new ArrayList<Double>());
			
			// on y ajoute le nombre de coups
			this.getData().get(nomRoi).add(Math.max(Library_Quantif.getCounts(imp),1.0d) );
		}
	}

	@Override
	public void calculateResults() {

		// construction du tableau representant chaque rein
		this.kidneysLR = new ArrayList<>();
		if (kidneys[0])
			kidneysLR.add("L");
		if (kidneys[1])
			kidneysLR.add("R");

		normalizeBP();

		// on calcule les corticales si elle sont demandees
		if (Prefs.get("renal.pelvis.preferred", true)) {
			this.calculCortical();
		}

		// on ajuste toutes les valeurs pour les mettre en coup / sec
		for (String k : this.getData().keySet()) {
			List<Double> data = this.getData().get(k);
			this.getData().put(k, this.adjustValues(data));
		}

		// on soustrait le bruit de fond
		this.substractBkg();

		// ***INTEGRALE DE LA COURBE VASCULAIRE***
		// on recupere la liste des donnees vasculaires
		List<Double> vasc = this.getData("Blood Pool");
		XYSeries serieVasc = this.createSerie(vasc, "");
		List<Double> vascIntegree = Library_JFreeChart.getIntegralSummed(serieVasc, serieVasc.getMinX(), serieVasc.getMaxX());
		this.getData().put("BPI", vascIntegree); // BPI == Blood Pool Integrated
	}

	/**
	 * Calcule la courbe fitte selon un polynome du 3eme degre pour la courbe de
	 * chaque rein. Ajuste ensuite a l'echelle des valeurs de sortie
	 */
	public void fitVasculaire() {
		// on recupere la liste des donnees vasculaires
		List<Double> bpi = this.getData("BPI");

		for (String lr : this.kidneysLR) {
			// recuperation des donnees des reins
			List<Double> corrige = this.getData().get("Final K" + lr);
			// calcul des courbes fitees
			List<Double> vascFit = this.fitVasc(bpi, corrige);

			// on calcule le valeurs de la courbe sortie
			List<Double> sortieInt = new ArrayList<>();
			for (int i = 0; i < corrige.size(); i++) {
				// on ajoute uniquement si la valeur est positive
				Double output = Math.max(vascFit.get(i) - corrige.get(i), 0);
				/// on ajoute la valeur calculee dans la liste de sortie renale
				sortieInt.add(output);
			}

			/// on ajoute les nouvelles courbes dans les donnees
			this.getData().put("Output K" + lr, sortieInt);
			this.getData().put("Blood pool fitted " + lr, vascFit);
		}
	}

	/* Contenu qui sera present lors de l'exprotation du CSV
	 * (non-Javadoc)
	 * @see org.petctviewer.scintigraphy.scin.ModelScinDyn#toString()
	 */
	@Override
	public String toString() {
		Double[][] nora = this.getNora();
		Double[][] excr = this.getExcr();
		Double[] sep = this.getSeparatedFunction();
		double[] patlak = this.getPatlakPente();
		Double[][] timing = this.getTiming();

		String s = super.toString();
		
		s += "\n";
		
		s += getDataString("Final KL", "Corrected Left Kidney");
		s += getDataString("Final KR", "Corrected Right Kidney");
		s += getDataString("Blood Pool", "Blood Pool");

		s += "\n";
		s+=getROEString();
		s+="\n";
		s += ",time, left kidney, right kidney \n";
		for (int i = 0; i < nora.length; i++) {
			s += "NORA ," + nora[0][i] + "," + nora[1][i] + "," + nora[2][i] + "\n";
		}

		for (int i = 0; i < nora.length; i++) {
			s += "Excretion ratio," + excr[0][i] + "," + excr[1][i] + "," + excr[2][i] + "\n";
		}

		s += "Separated function integral , ," + sep[0] + "," + sep[1] + "\n";
		
		if(patlak != null) {
			s += "Separated function patlak , ," + patlak[0] + "," + patlak[1] + "\n";
		}
		
		s += "Timing tmax , ," + timing[0][0] + "," + timing[0][1] + "\n";
		s += "Timing t1/2 , ," + timing[1][0] + "," + timing[1][1] + "\n";
		
		s += "\n";
		
		
		
		// ROE
		Double xLasilix = this.adjustedValues.get("lasilix");
		Double[] time = {Library_Quantif.round(xLasilix - 1, 1),
				Library_Quantif.round(xLasilix + 2, 1), 
				Library_Quantif.round(this.getSerie("Blood Pool").getMaxX(), 1)};
		s += "Time ROE (min), "+ time[0]+","+this.getROE(time[0], "L")+","+this.getROE(time[0], "R")+"\n"
			+"Time ROE (min), "+ time[1]+","+this.getROE(time[1], "L")+","+this.getROE(time[1], "R")+"\n"
			+"Time ROE (min), "+ time[2]+","+this.getROE(time[2], "L")+","+this.getROE(time[2], "R")+"\n";
		
		s+= s += super.toString();
		return s;

	}
	
	
	/********** Private *********/
	// fit la courbe selon un polynome de degre 3 et la modifie pour qu'elle soit au
	// plus pres de la courbe du rein
	private List<Double> fitVasc(List<Double> vasc, List<Double> kidney) {

		XYSeries bpi = this.createSerie(vasc, "");
		XYSeriesCollection datasetVasc = new XYSeriesCollection();
		datasetVasc.addSeries(bpi);

		// on fait un fit ploynomial de degre 3
		double[] reg = Regression.getPolynomialRegression(datasetVasc, 0, 3);

		// on calcule les points de la courbe fitee
		XYSeries seriesVasc = new XYSeries("Vasc");
		for (int i = 0; i < bpi.getItemCount(); i++) {
			double x = bpi.getX(i).doubleValue();
			seriesVasc.add(x, reg[0] + reg[1] * x + reg[2] * Math.pow(x, 2) + reg[3] * Math.pow(x, 3));
		}

		this.getData().put("Blood pool fitted", Library_JFreeChart.seriesToList(seriesVasc));

		XYSeries seriesKid = this.createSerie(kidney, "Kidney");

		// l'intervalle est defini par l'utilisateur
		Double x1 = this.adjustedValues.get("start");
		Double x2 = this.adjustedValues.get("end");
		Double startX = Math.min(x1, x2);
		Double endX = Math.max(x1, x2);

		// on recupere les points compris dans l'intervalle
		XYSeries croppedKidney = Library_JFreeChart.cropSeries(seriesKid, startX, endX);
		XYSeries croppedVasc = Library_JFreeChart.cropSeries(seriesVasc, startX, endX);

		// on ajoute les series dans une collection afin d'utiliser le fit de jfreechart
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(croppedVasc);
		dataset.addSeries(croppedKidney);

		double[] courbeVasc = Regression.getOLSRegression(dataset, 0);
		double[] courbeKidney = Regression.getOLSRegression(dataset, 1);

		// calcul du rapport de pente sur l'intervalle defini par l'utilisateur
		Double rapportPente = courbeKidney[1] / courbeVasc[1];

		// List<Double> fittedVasc = new ArrayList<>();
		// for (Double d : vasc) {
		// fittedVasc.add(d * rapportPente);
		// }

		List<Double> fittedVasc = new ArrayList<>();
		for (int i = 0; i < seriesVasc.getItemCount(); i++) {
			Double d = seriesVasc.getY(i).doubleValue();
			fittedVasc.add(d * rapportPente);
		}

		// decalage pour que les courbes soient au meme niveau
		Double milieu = (endX + startX) / 2;
		Double offset = this.getY(kidney, milieu) - this.getY(fittedVasc, milieu);

		// on ajoute le decalage sur tous les points
		for (int i = 0; i < fittedVasc.size(); i++) {
			fittedVasc.set(i, fittedVasc.get(i) + offset);
		}

		return fittedVasc;
	}

	private void substractBkg() {
		// ***VALEURS AJUSTEES AVEC LE BRUIT DE FOND POUR CHAQUE REIN***
		for (String lr : kidneysLR) { // pour chaque rein
			List<Double> RGCorrige = new ArrayList<>();
			// on recupere l'aire des rois bruit de fond et rein
			int aireRein = this.organRois.get(lr + ". Kidney").getStatistics().pixelCount;
			int aireBkg = this.organRois.get(lr + ". bkg").getStatistics().pixelCount;
			List<Double> lk = this.getData(lr + ". Kidney");
			List<Double> lbkg = this.getData(lr + ". bkg");

			// on calcule le coup moyen de la roi, on l'ajuste avec le bdf et on l'applique
			// sur toute la roi pour chaque rein afin d'ajuster la valeur brute pour les
			// deux reins
			for (int i = 0; i < this.getFrameduration().length; i++) {
				Double countRein = lk.get(i);
				Double countBkg = lbkg.get(i);

				Double moyRein = countRein / aireRein;
				Double moyBkg = countBkg / aireBkg;
				Double adjustedValueG = (moyRein - moyBkg) * aireRein;
				RGCorrige.add(adjustedValueG);
			}

			// on ajoute les nouveles valeurs aux donnees
			this.getData().put("Final K" + lr, RGCorrige);
		}

	}

	// normalise la vasculaire pour le rein gauche et droit pour le patlak
	private void normalizeBP() {
		List<Double> bp = getData("Blood Pool");
		Integer aireBP = this.organRois.get("Blood Pool").getStatistics().pixelCount;

		// pour chaque rein on ajoute la valeur normalisee de la vasculaire
		for (String lr : this.kidneysLR) {
			List<Double> bpNorm = new ArrayList<>();
			Integer aire = this.organRois.get(lr + ". Kidney").getStatistics().pixelCount;
			for (Double d : bp) {
				bpNorm.add((d / aireBP) * aire);
			}
			this.getData().put("BP norm " + lr, bpNorm);
		}
	}

	/**
	 * calcule et renvoie les valeurs de courbes des bassinets
	 */
	private void calculCortical() {
		for (String lr : this.kidneysLR) { // on calcule la valeur de la corticale pour chaque rein
			List<Double> cortical = new ArrayList<>(); // coups de la corticale
			
			List<Double> rein = this.getData(lr + ". Kidney");
			List<Double> bassinet = this.getData(lr + ". Pelvis");
			for (int i = 0; i < this.getData("Blood Pool").size(); i++) {
				cortical.add(rein.get(i) - bassinet.get(i));
			}
			this.getData().put(lr + ". Cortical", cortical);
		}
	}


	public void setNephrogramChart(JValueSetter nephrogramChart) {
		this.nephrogramChart = nephrogramChart;
	}

	public JValueSetter getNephrogramChart() {
		return nephrogramChart;
	}

	public JValueSetter getPatlakChart() {
		return patlakChart;
	}

	public void setPatlakChart(JValueSetter patlakChart) {
		this.patlakChart = patlakChart;
	}
	
	public int[] getFrameDurations() {
		return frameDurations;
	}
	
	public ImageSelection getImpAnt() {
		return impAnt;
	}

	public ImageSelection getImpPost() {
		return impPost;
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