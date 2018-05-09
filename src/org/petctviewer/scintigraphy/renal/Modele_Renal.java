package org.petctviewer.scintigraphy.renal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

import ij.ImagePlus;

public class Modele_Renal extends ModeleScinDyn {

	private HashMap<String, Integer> organArea;
	private Double[] adjusted;

	public Modele_Renal(int[] frameDuration) {
		super(frameDuration);
		this.organArea = new HashMap<String, Integer>();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		super.enregistrerMesure(nomRoi, imp);

		// aire de la roi ex pixel
		int area = imp.getStatistics().pixelCount;
		String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));

		// si on n'a pas deja enregistre son aire, on l'ajout a la hashmap
		if (this.organArea.get(name) == null) {
			this.organArea.put(name, area);
		}
	}

	@Override
	public void calculerResultats() {

		// on recupere le tableau avec les noms des organes
		String[] orgs = Controleur_Renal.ORGANES;

		// on ajuste toutes les valeurs pour les mettre en coup / sec
		for (String k : this.getData().keySet()) {
			List<Double> data = this.getData().get(k);
			this.getData().put(k, this.adjustValues(data));
		}

		// on recupere la liste des donnees vasculaires
		List<Double> vasc = this.getData(orgs[4]);

		List<Double> RGCorrige = new ArrayList<Double>();
		List<Double> RDCorrige = new ArrayList<Double>();

		// on recupere l'aire des rois bruit de fond et rein
		int aireRD = this.organArea.get(orgs[0]);
		int aireRG = this.organArea.get(orgs[2]);
		int aireBkgD = this.organArea.get(orgs[1]);
		int aireBkgG = this.organArea.get(orgs[3]);

		// on calcule le coup moyen de la roi, on l'ajuste avec le bdf et on l'applique
		// sur toute la roi pour chaque rein
		// afin d'ajuster la valeur brute
		for (int i = 0; i < this.getData(orgs[0]).size(); i++) {
			Double countRG = this.getData(orgs[2]).get(i);
			Double countBgG = this.getData(orgs[3]).get(i);
			Double adjustedValueG = ((countRG / aireRG) - (countBgG / aireBkgG)) * aireRG;
			RGCorrige.add(adjustedValueG);

			Double countRD = this.getData(orgs[0]).get(i);
			Double countBgD = this.getData(orgs[1]).get(i);
			Double adjustedValueD = ((countRD / aireRD) - (countBgD / aireBkgD)) * aireRD;
			RDCorrige.add(adjustedValueD);
		}

		// on calcule l'aire entre chaque paire de points successif
		List<Double> vascIntegree = new ArrayList<Double>();
		XYSeries serieVasc = this.createSerie(vasc, "");
		vascIntegree = this.getIntegral(serieVasc, serieVasc.getMinX(), serieVasc.getMaxX());

		// on calcule le valeurs de la courbe sortie pour chaque rein
		List<Double> sortieIntRG = new ArrayList<Double>();
		List<Double> sortieIntRD = new ArrayList<Double>();
		for (int i = 0; i < RGCorrige.size(); i++) {
			sortieIntRG.add(vascIntegree.get(i) - RGCorrige.get(i));
			sortieIntRD.add(vascIntegree.get(i) - RDCorrige.get(i));
		}

		// ajout des valeurs dans les donnees
		this.getData().put("Blood pool fitted", vascIntegree);
		this.getData().put("Final KL", RGCorrige);
		this.getData().put("Final KR", RDCorrige);
		this.getData().put("Output KL", sortieIntRG);
		this.getData().put("Output KR", sortieIntRD);
	}

	public int getPercentage(int min, XYSeries output) {
		XYSeries serieBPF = this.getSerie("Blood pool fitted");
		int perct = (int) (ModeleScin.getY(output, min).doubleValue() / ModeleScin.getY(serieBPF, min).doubleValue()
				* 100);
		return perct;
	}

	public XYSeries getSerie(String key) {
		List<Double> data = this.getData().get(key);
		return this.createSerie(data, key);
	}

	private List<Double> getIntegral(XYSeries series, Double startX, Double endX) {
		
		List<Double> integrale = new ArrayList<Double>();

		List<Double> croppedList = new ArrayList<Double>();
		List<Double> seriesList = this.seriesToList(series);
		for (int i = 0; i < seriesList.size(); i++) {
			Double d = seriesList.get(i);
			if (series.getX(i).doubleValue() >= startX && series.getX(i).doubleValue() <= endX) {
				croppedList.add(d);
			}
		}

		XYSeries croppedSeries = this.createSerie(croppedList, "");

		Double airePt1 = croppedSeries.getX(0).doubleValue() * croppedSeries.getY(0).doubleValue() / 2;
		integrale.add(airePt1);
		for (int i = 0; i < croppedSeries.getItemCount() - 1; i++) {
			Double aire = ((croppedSeries.getX(i + 1).doubleValue() - croppedSeries.getX(i).doubleValue())
					* (croppedSeries.getY(i).doubleValue() + croppedSeries.getY(i + 1).doubleValue())) / 2;
			integrale.add(aire);
		}

		// on en deduit l'integrale
		List<Double> integraleSum = new ArrayList<Double>();
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
			s += "\n ROE " + min + "min Left Kidney , " + this.getPercentage(min, lk);
			s += "\n ROE " + min + "min Right Kidney , " + this.getPercentage(min, rk);
		}

		return s;
	}

	public String[][] getTableData() {
		String[][] s = new String[4][3];
		s[0][0] = "Separated function (%)";
		s[1][0] = "Renal retention";
		s[2][0] = "Max duration (min)";
		s[3][0] = "1/2 max duration (min)";

		Double debut = Math.min(this.adjusted[4], this.adjusted[5]);
		Double fin = Math.max(this.adjusted[4], this.adjusted[5]);
		
		XYSeries lk = this.getSerie("Final KL");
		XYSeries rk = this.getSerie("Final KR");
		
		List<Double> listRG = this.getIntegral(lk, debut, fin);
		List<Double> listRD = this.getIntegral(rk, debut, fin);
		
		Double intRG = listRG.get(listRG.size() - 1);
		Double intRD = listRD.get(listRD.size() - 1);

		// Left kidney
		s[0][1] = "" + ModeleScin.round((intRG / (intRG + intRD))*100, 1);
		s[1][1] = "" + ModeleScin.round(ModeleScin.getY(lk, lk.getMaxX()) / ModeleScin.getY(lk, this.adjusted[3]), 3);
		s[2][1] = "" + ModeleScin.round(this.adjusted[1], 2);
		
		Double tdemiL = ModeleScin.round(ModeleScinDyn.getTDemiObs(lk, this.adjusted[1]), 2);
		if(tdemiL != 0) {
			s[3][1] = "" + tdemiL;			
		}else {
			s[3][1] = "n/a";
		}

		// Right kidney
		s[0][2] = "" + ModeleScin.round((intRD / (intRG + intRD))*100, 1);
		s[1][2] = "" + ModeleScin.round(( ModeleScin.getY(rk, rk.getMaxX()) / ModeleScin.getY(rk, this.adjusted[2])), 3);
		s[2][2] = "" + ModeleScin.round(this.adjusted[0], 2);
		
		Double tdemiR = ModeleScin.round(ModeleScinDyn.getTDemiObs(rk, this.adjusted[0]), 2);
		if(tdemiR != 0) {
			s[3][2] = "" + tdemiR;
		}else {
			s[3][2] = "n/a";
		}
		

		return s;
	}

	public void setAdjustedValues(Double[] xValues) {
		this.adjusted = xValues;
	}

	public Double[] getAdjustedValues() {
		return this.adjusted;
	}

}
