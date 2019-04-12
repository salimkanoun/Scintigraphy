package org.petctviewer.scintigraphy.shunpo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageStatistics;

public class ModeleShunpo extends ModeleScin {
	
	private Map<String, Double> coups;
	private int pixrdp;
	private int pixrgp;
	private int pixrda;
	private int pixrga;
	private HashMap<String, Integer> mgs;
	private String[] abvsMG = { "PD", "PG", "RD", "RG", "C" };
	private String[] retour;
	
	protected void calculerCoups(String roi, ImagePlus imp) {
		ImageStatistics is = imp.getStatistics();

		if (roi.contains("BDF"))
			coups.put(roi, is.mean);
		else {
			if (roi.contains("R")) {
				if (roi.equals("RDP"))
					pixrdp = is.pixelCount;
				if (roi.equals("RGP"))
					pixrgp = is.pixelCount;
				if (roi.equals("RDA"))
					pixrda = is.pixelCount;
				if (roi.equals("RGA"))
					pixrga = is.pixelCount;
			}
			coups.put(roi, is.pixelCount * is.mean);
		}
		if (Controleur_Shunpo.showLog) {
			IJ.log(roi + "coups= " + String.valueOf(is.pixelCount * is.mean));
		}
	}

	// Retrait du BDF aux reins
	private void coupsReins() {
		double rdp = coups.get("RDP");
		double rgp = coups.get("RGP");
		double bdfp = coups.get("BDFP");
		coups.put("RDP", rdp - (bdfp * pixrdp));
		coups.put("RGP", rgp - (bdfp * pixrgp));
		double rda = coups.get("RDA");
		double rga = coups.get("RGA");
		double bdfa = coups.get("BDFA");
		coups.put("RDA", rda - (bdfa * pixrda));
		coups.put("RGA", rga - (bdfa * pixrga));
	}

	private void mgs() {
		for (String abv : abvsMG)
			moyenneGeo(abv);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(String abv) {
		double[] coupsa = new double[2];
		String[] asuppr = new String[2];
		int index = 0;
		for (Entry<String, Double> entry : coups.entrySet()) {
			if (entry.getKey().contains(abv)) {
				coupsa[index] = entry.getValue();
				asuppr[index] = entry.getKey();
				index++;
			}
		}
		for (String so : asuppr)
			coups.remove(so);
		mgs.put("MG" + abv, moyenneGeometrique(coupsa));
	}

	// Calcule la moyenne g茅om茅trique des nombres en param猫tre
	private int moyenneGeometrique(double[] vals) {
		double result = 1.0;
		for (int i = 0; i < vals.length; i++) {
			result *= vals[i];
		}
		result = Math.sqrt(result);
		return (int) result;
	}

	private String convertAbrev(String abv) {
		char[] decomp = abv.toCharArray();
		String result = "";
		for (int i = 0; i < decomp.length; i++) {
			switch (decomp[i]) {
			case 'P':
				result += "Poumon ";
				break;
			case 'R':
				result += "Rein ";
				break;
			case 'C':
				result += "Cerveau ";
				break;
			case 'M':
				result += "MG ";
				i++;
				break;
			case 'D':
				result += "Droite ";
				break;
			case 'G':
				result += "Gauche ";
				break;
			}
			if (i == decomp.length - 1)
				result += ": ";
		}
		return result;
	}

	@Override
	public void calculerResultats() {
		this.retour = new String[9];
		coupsReins();
		int index = 0;
		// Les 5 MGs
		mgs();
		for (Entry<String, Integer> entry : mgs.entrySet()) {
			retour[index] = convertAbrev(entry.getKey()) + entry.getValue();
			index++;
		}
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		// Calculs
		double percPD = (mgs.get("MGPD") / (1.0 * mgs.get("MGPD") + mgs.get("MGPG"))) * 100;
		retour[3] += " (" + us.format(percPD) + "%)";
		double percPG = (mgs.get("MGPG") / (1.0 * mgs.get("MGPD") + mgs.get("MGPG"))) * 100;
		retour[0] += " (" + us.format(percPG) + "%)";
		int totmg = mgs.get("MGPD") + mgs.get("MGPG");
		retour[index] = "Total MG : " + totmg;
		index++;
		int totshunt = mgs.get("MGRD") + mgs.get("MGRG") + mgs.get("MGC");
		retour[index] = "Total Shunt : " + totshunt;
		index++;
		double percSyst = (100.0 * totshunt) / totmg;
		retour[index] = "% Systemic : " + us.format(percSyst) + "%";
		index++;
		Modele_Shunpo.shunt = ((totshunt * 100.0) / (totmg * 0.38));
		retour[index] = "Pulmonary Shunt : " + us.format(Modele_Shunpo.shunt) + "% (total blood Flow)";
	}

}
