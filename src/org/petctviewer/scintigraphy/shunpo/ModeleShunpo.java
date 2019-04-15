package org.petctviewer.scintigraphy.shunpo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class ModeleShunpo extends ModeleScin {

	private Map<Integer, Double> coups;
	private int pixrdp;
	private int pixrgp;
	private int pixrda;
	private int pixrga;
	private Map<Integer, Integer> geometricalAverage;
	private String[] retour;

	public static final int LUNG_RIGHT_ANT = 0, LUNG_RIGHT_POST = 1, LUNG_LEFT_ANT = 2, LUNG_LEFT_POST = 3,
			KIDNEY_RIGHT_ANT = 4, KIDNEY_RIGHT_POST = 5, KIDNEY_LEFT_ANT = 6, KIDNEY_LEFT_POST = 7,
			BACKGROUND_NOISE_ANT = 8, BACKGROUND_NOISE_POST = 9, BRAIN_ANT = 10, BRAIN_POST = 11, TOTAL_ORGANS = 12;

	private static final int RESULT_LUNG_RIGHT = 0, RESULT_LUNG_LEFT = 1, RESULT_KIDNEY_RIGHT = 2,
			RESULT_KIDNEY_LEFT = 3, RESULT_BRAIN = 4, RESULT_TOTAL_AVG = 5, RESULT_TOTAL_SHUNT = 6, RESULT_SYSTEMIC = 7,
			RESULT_PULMONARY_SHUNT = 8, TOTAL_RESULT = 9;

	public ModeleShunpo(ImageSelection[] selectedImages) {
		super(selectedImages);
		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();
		this.retour = new String[9];
	}

	protected void calculerCoups(int organ, ImagePlus imp) {
		double counts = Library_Quantif.getCounts(imp);
		this.coups.put(organ, counts);
		System.out.println("Calculations for " + organ + "[" + ModeleShunpo.convertOrgan(organ) + "] -> " + counts);
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

		coups.remove(BACKGROUND_NOISE_ANT);
		coups.remove(BACKGROUND_NOISE_POST);
	}

	private void computeGeometricalAverage() {
		this.moyenneGeo(LUNG_RIGHT_ANT);
		this.moyenneGeo(LUNG_LEFT_ANT);
		this.moyenneGeo(KIDNEY_RIGHT_ANT);
		this.moyenneGeo(KIDNEY_LEFT_ANT);
		this.moyenneGeo(BRAIN_ANT);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(int organ) {
		if (organ % 2 == 0)
			geometricalAverage.put(organ,
					(int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 1)));
		else
			geometricalAverage.put(organ,
					(int) Library_Quantif.moyGeom(this.coups.get(organ - 1), this.coups.get(organ)));
	}

	public static String convertOrgan(int organ) {
		switch (organ) {
		case LUNG_RIGHT_ANT:
		case LUNG_RIGHT_POST:
			return "Right Lung: ";
		case LUNG_LEFT_ANT:
		case LUNG_LEFT_POST:
			return "Left Lung: ";
		case KIDNEY_RIGHT_ANT:
		case KIDNEY_RIGHT_POST:
			return "Right Kidney: ";
		case KIDNEY_LEFT_ANT:
		case KIDNEY_LEFT_POST:
			return "Left Kidney: ";
		case BRAIN_ANT:
		case BRAIN_POST:
			return "Brain: ";
		default:
			return "Unknown Organ (" + organ + "): ";
		}
	}

	public String[] getResult() {
		return this.retour;
	}

	@Override
	public void calculerResultats() {
		this.retour = new String[9];
		coupsReins();
		// Les 5 MGs
		computeGeometricalAverage();
		retour[RESULT_LUNG_RIGHT] = convertOrgan(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_RIGHT_ANT);
		retour[RESULT_LUNG_LEFT] = convertOrgan(LUNG_LEFT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT);
		retour[RESULT_KIDNEY_RIGHT] = convertOrgan(KIDNEY_RIGHT_ANT) + geometricalAverage.get(KIDNEY_RIGHT_ANT);
		retour[RESULT_KIDNEY_LEFT] = convertOrgan(KIDNEY_LEFT_ANT) + geometricalAverage.get(KIDNEY_LEFT_ANT);
		retour[RESULT_BRAIN] = convertOrgan(BRAIN_ANT) + geometricalAverage.get(BRAIN_ANT);
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		// Calculs
		double percPD = (geometricalAverage.get(LUNG_RIGHT_ANT)
				/ (1.0 * geometricalAverage.get(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT))) * 100;
		retour[RESULT_LUNG_RIGHT] += " (" + us.format(percPD) + "%)";
		double percPG = (geometricalAverage.get(LUNG_LEFT_ANT)
				/ (1.0 * geometricalAverage.get(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT))) * 100;
		retour[RESULT_LUNG_LEFT] += " (" + us.format(percPG) + "%)";
		int totmg = geometricalAverage.get(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT);
		retour[RESULT_TOTAL_AVG] = "Total MG : " + totmg;
		int totshunt = geometricalAverage.get(KIDNEY_RIGHT_ANT) + geometricalAverage.get(KIDNEY_LEFT_ANT)
				+ geometricalAverage.get(BRAIN_ANT);
		retour[RESULT_TOTAL_SHUNT] = "Total Shunt : " + totshunt;
		double percSyst = (100.0 * totshunt) / totmg;
		retour[RESULT_SYSTEMIC] = "% Systemic : " + us.format(percSyst) + "%";
		Modele_Shunpo.shunt = ((totshunt * 100.0) / (totmg * 0.38));
		retour[RESULT_PULMONARY_SHUNT] = "Pulmonary Shunt : " + us.format(Modele_Shunpo.shunt) + "% (total blood Flow)";
	}

}
