package org.petctviewer.scintigraphy.renal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

import ij.ImagePlus;
import ij.Prefs;

public class Modele_Renal extends ModeleScinDyn {

	private HashMap<String, Integer> organArea;
	private HashMap<Comparable, Double> adjustedValues;
	private boolean[] kidneys;
	private double[] patlakPente;
	private boolean lock;
	private ArrayList<String> kidneysLR;

	/**
	 * recupere les valeurs et calcule les resultats de l'examen renal
	 * 
	 * @param frameDuration
	 *            duree de chaque frame en ms
	 */
	public Modele_Renal(int[] frameDuration, boolean[] kidneys, ImagePlus imp) {
		super(frameDuration);
		this.imp = imp;
		this.kidneys = kidneys;
		this.organArea = new HashMap<>();
	}

	public static void graph(XYDataset data) {
		JFreeChart chart = ChartFactory.createXYLineChart("", "x", "y", data);

		JFrame frame = new JFrame();
		frame.add(new ChartPanel(chart));
		frame.pack();
		frame.setVisible(true);
	}

	public void setKidneys(boolean[] kidneys) {
		this.kidneys = kidneys;
	}

	public boolean[] getKidneys() {
		return this.kidneys;
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		if (!this.isLocked()) {
			super.enregistrerMesure(nomRoi, imp);

			// nom de l'organe sans le tag
			String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));
			// si on n'a pas deja enregistre son aire, on l'ajout a la hashmap
			if (this.organArea.get(name) == null) {
				int area = imp.getStatistics().pixelCount;
				this.organArea.put(name, area);
			}
		}

	}

	@Override
	public void calculerResultats() {

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
		List<Double> vascIntegree = Modele_Renal.getIntegral(serieVasc, serieVasc.getMinX(), serieVasc.getMaxX());
		this.getData().put("BPI", vascIntegree); // BPI == Blood Pool Integrated
	}

	private void substractBkg() {
		// ***VALEURS AJUSTEES AVEC LE BRUIT DE FOND POUR CHAQUE REIN***
		for (String lr : kidneysLR) { // pour chaque rein
			List<Double> RGCorrige = new ArrayList<>();
			// on recupere l'aire des rois bruit de fond et rein
			int aireRein = this.organArea.get(lr + ". Kidney");
			int aireBkg = this.organArea.get(lr + ". bkg");
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
		Integer aireBP = this.organArea.get("Blood Pool");

		// pour chaque rein on ajoute la valeur normalisee de la vasculaire
		for (String lr : this.kidneysLR) {
			List<Double> bpNorm = new ArrayList<>();
			Integer aire = this.organArea.get(lr + ". Kidney");
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
				Double output = vascFit.get(i) - corrige.get(i);
				if (output <= 0) {
					output = 0.0;
				}
				/// on ajoute la valeur calculee dans la liste de sortie renale
				sortieInt.add(output);
			}

			/// on ajoute les nouvelles courbes dans les donnees
			this.getData().put("Output K" + lr, sortieInt);
			this.getData().put("Blood pool fitted " + lr, vascFit);
		}
	}

	// recupere les valeurs situees entre startX et endX
	public static XYSeries cropSeries(XYSeries series, Double startX, Double endX) {
		XYSeries cropped = new XYSeries(series.getKey() + " cropped");
		for (int i = 0; i < series.getItemCount(); i++) {
			if (series.getX(i).doubleValue() >= startX && series.getX(i).doubleValue() <= endX) {
				cropped.add(series.getX(i), series.getY(i));
			}
		}
		return cropped;
	}

	// recupere les valeurs situees entre startX et endX
	public static XYDataset cropDataset(XYDataset data, Double startX, Double endX) {
		XYSeriesCollection dataset = new XYSeriesCollection();

		for (int i = 0; i < data.getSeriesCount(); i++) {
			XYSeries series = new XYSeries("" + i);
			for (int j = 0; j < data.getItemCount(0); j++) {
				series.add(data.getX(i, j), data.getY(i, j));
			}
			dataset.addSeries(cropSeries(series, startX, endX));
		}

		return dataset;
	}

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
			seriesVasc.add(x, reg[0] + reg[1] * x + reg[2] * x * x + reg[3] * Math.pow(x, 3));
		}

		XYSeries seriesKid = this.createSerie(kidney, "Kidney");

		// l'intervalle est defini par l'utilisateur
		Double x1 = this.adjustedValues.get("start");
		Double x2 = this.adjustedValues.get("end");
		Double startX = Math.min(x1, x2);
		Double endX = Math.max(x1, x2);

		// on recupere les points compris dans l'intervalle
		XYSeries croppedKidney = Modele_Renal.cropSeries(seriesKid, startX, endX);
		XYSeries croppedVasc = Modele_Renal.cropSeries(seriesVasc, startX, endX);

		// on ajoute les series dans une collection afin d'utiliser le fit de jfreechart
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(croppedVasc);
		dataset.addSeries(croppedKidney);

		double[] courbeVasc = Regression.getOLSRegression(dataset, 0);
		double[] courbeKidney = Regression.getOLSRegression(dataset, 1);

		// calcul du rapport de pente sur l'intervalle defini par l'utilisateur
		Double rapportPente = courbeKidney[1] / courbeVasc[1];

		List<Double> fittedVasc = new ArrayList<>();
		for (Double d : vasc) {
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
	public int getPercentage(int min, XYSeries output, String lr) {
		XYSeries serieBPF = this.getSerie("Blood pool fitted " + lr);
		int perct = (int) (ModeleScinDyn.getY(output, min).doubleValue()
				/ ModeleScinDyn.getY(serieBPF, min).doubleValue() * 100);
		return perct;
	}

	/**
	 * renvoie une serie selon sa cle
	 * 
	 * @param key
	 *            la cle
	 * @return la serie
	 */
	public XYSeries getSerie(String key) {
		List<Double> data = this.getData().get(key);
		if (data == null) {
			throw new IllegalArgumentException("No series with key " + key);
		}
		return this.createSerie(data, key);
	}

	// renvoie l'aire sous la courbe entre les points startX et endX
	private static List<Double> getIntegral(XYSeries series, Double startX, Double endX) {

		List<Double> integrale = new ArrayList<>();

		// on recupere les points de l'intervalle voulu
		XYSeries croppedSeries = Modele_Renal.cropSeries(series, startX, endX);

		// on calcule les aires sous la courbe entre chaque paire de points
		Double airePt1 = croppedSeries.getX(0).doubleValue() * croppedSeries.getY(0).doubleValue() / 2;
		integrale.add(airePt1);
		for (int i = 0; i < croppedSeries.getItemCount() - 1; i++) {
			Double aire = ((croppedSeries.getX(i + 1).doubleValue() - croppedSeries.getX(i).doubleValue())
					* (croppedSeries.getY(i).doubleValue() + croppedSeries.getY(i + 1).doubleValue())) / 2;
			integrale.add(aire);
		}

		// on en deduit l'integrale
		List<Double> integraleSum = new ArrayList<>();
		integraleSum.add(integrale.get(0));
		for (int i = 1; i < integrale.size(); i++) {
			integraleSum.add(integraleSum.get(i - 1) + integrale.get(i));
		}

		return integraleSum;
	}

	@Override
	public String toString() {
		String s = super.toString();

		// points d'interet
		int[] mins = new int[] { 20, 22, 30 };

		// recuperation des series output gauche et droite
		XYSeries lk = this.getSerie("Output KL");
		XYSeries rk = this.getSerie("Output KR");

		// ajout des resultats au csv
		for (int min : mins) {
			s += "\n ROE " + min + "min Left Kidney , " + this.getPercentage(min, lk, "L");
			s += "\n ROE " + min + "min Right Kidney , " + this.getPercentage(min, rk, "R");
		}

		// TODO modifier ça (voir methodes modele renal)
		s += "\n";

		// on ajoute les valeurs du tableau
		for (int i = 1; i < 3; i++) {
			String lr = " Left";
			if (i == 2) {
				lr = " Right";
			}
		}

		return s;

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
			res[1][0] = ModeleScin.round(ModeleScinDyn.getTDemiObs(lk, xMaxG), 1);
			res[0][0] = ModeleScin.round(xMaxG, 2);
		} else {
			res[0][0] = Double.NaN;
			res[1][0] = Double.NaN;
		}

		if (this.kidneys[1]) {
			Double xMaxD = this.adjustedValues.get("tmax R");
			XYSeries rk = this.getSerie("Final KR");
			res[1][1] = ModeleScin.round(ModeleScinDyn.getTDemiObs(rk, xMaxD), 1);
			res[0][1] = ModeleScin.round(xMaxD, 2);
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
	public Double[][] getNoRAPM(Double rg, Double rd) {
		// tableau de retour avec les resultats
		Double[][] res = new Double[2][2];

		Double xLasilixM1 = this.adjustedValues.get("lasilix") - 1;
		
		// si il y a un rein gauche
		if (this.kidneys[0]) {
			Double xMaxL = this.adjustedValues.get("tmax L");
			res[0][0] = ModeleScin.round((100 * rg / ModeleScinDyn.getY(this.getSerie("Final KL"), xMaxL)), 2);
			res[0][1] = ModeleScin.round((100 * rg / ModeleScinDyn.getY(this.getSerie("Final KL"), xLasilixM1)), 2);

		}

		// si il y a un rein droit
		if (this.kidneys[1]) {
			Double xMaxR = this.adjustedValues.get("tmax R");
			res[1][0] = ModeleScin.round((100 * rd / ModeleScinDyn.getY(this.getSerie("Final KR"), xMaxR)), 2);
			res[1][1] = ModeleScin.round((100 * rd / ModeleScinDyn.getY(this.getSerie("Final KR"), xLasilixM1)), 2);
		}

		return res;
	}

	/**
	 * calcule le nora selon le temps d'injection du lasilix
	 * 
	 * @return res[0] : temps, res[1] : rein gauche, res[2] : rein droit
	 */
	public Double[][] getNoRA() {
		Double[][] res = new Double[3][3];

		// adjusted[6] => lasilix
		Double xLasilix = this.adjustedValues.get("lasilix");
		res[0][0] = ModeleScin.round(xLasilix - 1, 1);
		res[0][1] = ModeleScin.round(xLasilix + 2, 1);
		res[0][2] = round(this.getSerie("Blood Pool").getMaxX(), 1);

		for (String lr : this.kidneysLR) {
			XYSeries kidney = this.getSerie("Final K" + lr);
			Double max = kidney.getMaxY();
			
			//change l'index sur lequel ecrire le resultat dans le tableau
			int index = 1;
			if(lr == "R")
				index = 2;

			// calcul nora rein gauche
			for (int i = 0; i < 3; i++) {
				if (this.getAdjustedValues().get("tmax " + lr) < res[0][i]) {
					res[index][i] = ModeleScin.round(getY(kidney, res[0][i]) * 100 / max, 1);
				}
			}
		}

		return res;

	}

	public void setAdjustedValues(HashMap<Comparable, Double> hashMap) {
		this.adjustedValues = hashMap;
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

		// points de la courbe renale ajustee
		XYSeries lk = this.getSerie("Final KL");
		XYSeries rk = this.getSerie("Final KR");

		// bornes de l'intervalle
		Double x1 = this.adjustedValues.get("start");
		Double x2 = this.adjustedValues.get("end");
		Double debut = Math.min(x1, x2);
		Double fin = Math.max(x1, x2);

		List<Double> listRG = Modele_Renal.getIntegral(lk, debut, fin);
		List<Double> listRD = Modele_Renal.getIntegral(rk, debut, fin);
		Double intRG = listRG.get(listRG.size() - 1);
		Double intRD = listRD.get(listRD.size() - 1);

		// Left kidney
		res[0] = ModeleScin.round((intRG / (intRG + intRD)) * 100, 1);

		// Right kidney
		res[1] = ModeleScin.round((intRD / (intRG + intRD)) * 100, 1);

		return res;
	}

	public double[] getPatlakPente() {
		return this.patlakPente;
	}

	public void setPatlakPente(double[] patlakRatio) {
		this.patlakPente = patlakRatio;
	}

	public double getNoRABladder(Double bld) {
		XYSeries bldSeries = this.getSerie("Bladder");
		return 100 * bld / ModeleScinDyn.getY(bldSeries, bldSeries.getMaxX());
	}

	public void lock() {
		this.lock = true;
	}

	public void unlock() {
		this.lock = false;
	}

	public boolean isLocked() {
		return this.lock;
	}

}