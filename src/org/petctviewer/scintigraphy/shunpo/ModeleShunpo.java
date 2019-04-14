package org.petctviewer.scintigraphy.shunpo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class ModeleShunpo extends ModeleScin {

	private Map<Integer, Double> coups;
	private int pixrdp;
	private int pixrgp;
	private int pixrda;
	private int pixrga;
	private HashMap<Integer, Integer> geometricalAverage;
	private String[] abvsMG = { "PD", "PG", "RD", "RG", "C" };
	private String[] retour;

	public static final int PULMON_RIGHT_ANT = 0, PULMON_RIGHT_POST = 1, PULMON_LEFT_ANT = 2, PULMON_LEFT_POST = 3,
			KIDNEY_RIGHT_ANT = 4, KIDNEY_RIGHT_POST = 5, KIDNEY_LEFT_ANT = 6, KIDNEY_LEFT_POST = 7, BRAIN_ANT = 8,
			BRAIN_POST = 9, BACKGROUND_NOISE_ANT = 10, BACKGROUND_NOISE_POST = 11, TOTAL_ORGANS = 12;

	protected void calculerCoups(int organ, ImagePlus imp) {
		this.coups.put(organ, Library_Quantif.getCounts(imp));
	}

	// Retrait du BDF aux reins
	private void coupsReins() {
		double rdp = coups.get(KIDNEY_RIGHT_POST);
		double rgp = coups.get(KIDNEY_LEFT_POST);
		double bdfp = coups.get(BACKGROUND_NOISE_POST);
		coups.put(KIDNEY_RIGHT_POST, rdp - (bdfp * pixrdp));
		coups.put(KIDNEY_LEFT_POST, rgp - (bdfp * pixrgp));
		double rda = coups.get(KIDNEY_RIGHT_ANT);
		double rga = coups.get(KIDNEY_LEFT_ANT);
		double bdfa = coups.get(BACKGROUND_NOISE_ANT);
		coups.put(KIDNEY_RIGHT_ANT, rda - (bdfa * pixrda));
		coups.put(KIDNEY_LEFT_ANT, rga - (bdfa * pixrga));
	}

	private void computeGeometricalAverage() {
		for (int i = 0; i < TOTAL_ORGANS; i += 2)
			this.moyenneGeo(i);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(int organ) {
		geometricalAverage.put(organ, (int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 1)));
	}

	private String convertOrgan(int organ) {
		switch (organ) {
		case PULMON_RIGHT_ANT:
		case PULMON_RIGHT_POST:
			return "Right Pulmon: ";
		case PULMON_LEFT_ANT:
		case PULMON_LEFT_POST:
			return "Left Pulmon: ";
		case KIDNEY_RIGHT_ANT:
		case KIDNEY_RIGHT_POST:
			return "Right Kidney: ";
		case BRAIN_ANT:
		case BRAIN_POST:
			return "Brain: ";
		default:
			return "Unknown Organ: ";
		}
	}

	public String[] getResult() {
		return this.retour;
	}

	@Override
	public void calculerResultats() {
		this.retour = new String[9];
		coupsReins();
		int index = 0;
		// Les 5 MGs
		computeGeometricalAverage();
		for (Entry<Integer, Integer> entry : geometricalAverage.entrySet()) {
			retour[index] = convertOrgan(entry.getKey()) + entry.getValue();
			index++;
		}
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		// Calculs
		double percPD = (geometricalAverage.get(PULMON_RIGHT_ANT)
				/ (1.0 * geometricalAverage.get(PULMON_RIGHT_ANT) + geometricalAverage.get(PULMON_LEFT_ANT))) * 100;
		retour[3] += " (" + us.format(percPD) + "%)";
		double percPG = (geometricalAverage.get(PULMON_LEFT_ANT)
				/ (1.0 * geometricalAverage.get(PULMON_RIGHT_ANT) + geometricalAverage.get(PULMON_LEFT_ANT))) * 100;
		retour[0] += " (" + us.format(percPG) + "%)";
		int totmg = geometricalAverage.get(PULMON_RIGHT_ANT) + geometricalAverage.get(PULMON_LEFT_ANT);
		retour[index] = "Total MG : " + totmg;
		index++;
		int totshunt = geometricalAverage.get(KIDNEY_RIGHT_ANT) + geometricalAverage.get(KIDNEY_LEFT_ANT)
				+ geometricalAverage.get(BRAIN_ANT);
		retour[index] = "Total Shunt : " + totshunt;
		index++;
		double percSyst = (100.0 * totshunt) / totmg;
		retour[index] = "% Systemic : " + us.format(percSyst) + "%";
		index++;
		Modele_Shunpo.shunt = ((totshunt * 100.0) / (totmg * 0.38));
		retour[index] = "Pulmonary Shunt : " + us.format(Modele_Shunpo.shunt) + "% (total blood Flow)";
	}

}
