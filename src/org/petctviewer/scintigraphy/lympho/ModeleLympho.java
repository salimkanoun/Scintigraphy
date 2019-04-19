package org.petctviewer.scintigraphy.lympho;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.shunpo.ModeleShunpo;

import ij.ImagePlus;

public class ModeleLympho extends ModeleScin{
	
	public static final int FOOT_RIGHT_ANT_FIRST = 0, FOOT_LEFT_ANT_FIRST = 1, FOOT_RIGHT_POST_FIRST = 2,  FOOT_LEFT_POST_FIRST = 3,
			FOOT_RIGHT_ANT_SECOND = 4,FOOT_LEFT_ANT_SECOND = 5, FOOT_RIGHT_POST_SECOND = 6,  FOOT_LEFT_POST_SECOND = 7, TOTAL_ORGANS = 8;
	
	private static final int RESULT_FOOT_RIGHT_FIRST = 0, RESULT_FOOT_LEFT_FIRST = 1, RESULT_FOOT_RIGHT_SECOND = 2,
			RESULT_FOOT_LEFT_SECOND = 3, GEOM_AVG = 4, GEOM_AVG_2 = 5;

	private boolean locked;
	
	private Map<Integer, Double> coups;
	
	private Map<Integer, Integer> geometricalAverage;
	
	private String[] retour;

	public ModeleLympho(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		
		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();
		
		this.retour = new String[9];
	}

//	@Override
//	public void calculerResultats() {
//		// TODO Auto-generated method stub
//		
//	}
	
	
	public boolean isLocked() {
		return locked;
	}
	
	/************** Getter *************/

	/************** Setter *************/	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String[] getResult() {
		return this.retour;
	}

	protected void calculerCoups(int organ, ImagePlus imp) {
		double counts = Library_Quantif.getCounts(imp);
		this.coups.put(organ, counts);
		System.out.println("Calculations for " + organ + "[" + ModeleShunpo.convertOrgan(organ) + "] -> " + counts);
	}
	
	public static String convertOrgan(int organ) {
		switch (organ) {
		case FOOT_RIGHT_ANT_FIRST:
		case FOOT_RIGHT_POST_FIRST:
			return "Right Foot First Image: ";
		case FOOT_LEFT_ANT_FIRST:
		case FOOT_LEFT_POST_FIRST:
			return "Left Foot First Image: ";
		case FOOT_RIGHT_ANT_SECOND:
		case FOOT_RIGHT_POST_SECOND:
			return "Right Foot Second Image: ";
		case FOOT_LEFT_ANT_SECOND:
		case FOOT_LEFT_POST_SECOND:
			return "Left Foot Second Image: ";
		default:
			return "Unknown Organ (" + organ + "): ";
		}
	}
	
	
	
	
	private void computeGeometricalAverage() {
		System.out.println("\n\n\n\n\n\n----------------------------\n\n\n\n\n\n");
		System.out.println("nbCounts : "+this.coups.size());
		for(Double count : this.coups.values().toArray(new Double[this.coups.size()])) {
			System.out.println("Counts : "+count);
		}
		this.moyenneGeo(FOOT_RIGHT_ANT_FIRST);
		this.moyenneGeo(FOOT_LEFT_ANT_FIRST);
		this.moyenneGeo(FOOT_RIGHT_ANT_SECOND);
		this.moyenneGeo(FOOT_LEFT_ANT_SECOND);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(int organ) {
		geometricalAverage.put(organ,
				(int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 2)));

	}
	
	
	
	@Override
	public void calculerResultats() {
		this.retour = new String[9];

		// Les 5 MGs
		computeGeometricalAverage();
		retour[RESULT_FOOT_RIGHT_FIRST] = convertOrgan(FOOT_RIGHT_ANT_FIRST) + geometricalAverage.get(FOOT_RIGHT_ANT_FIRST);
		retour[RESULT_FOOT_LEFT_FIRST] = convertOrgan(FOOT_LEFT_ANT_FIRST) + geometricalAverage.get(FOOT_LEFT_ANT_FIRST);
		retour[RESULT_FOOT_RIGHT_SECOND] = convertOrgan(FOOT_RIGHT_ANT_SECOND) + geometricalAverage.get(FOOT_RIGHT_ANT_SECOND);
		retour[RESULT_FOOT_LEFT_SECOND] = convertOrgan(FOOT_LEFT_ANT_SECOND) + geometricalAverage.get(FOOT_LEFT_ANT_SECOND);
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);
		
		// Calculs
		retour[GEOM_AVG] ="Stayed geometric average : ";
		double percPD = (geometricalAverage.get(FOOT_RIGHT_ANT_SECOND)
				/ (1.0 * geometricalAverage.get(FOOT_RIGHT_ANT_FIRST))) * 100;
		retour[GEOM_AVG] += " " + us.format(percPD) + "%";
		
		retour[GEOM_AVG_2] ="Dispatched geometric average : ";
		double percPG = (geometricalAverage.get(FOOT_RIGHT_ANT_FIRST)
				/ (1.0 * geometricalAverage.get(FOOT_RIGHT_ANT_SECOND))) * 100;
		retour[GEOM_AVG_2] += " " + us.format(percPG) + "%";
		// Calculs
//		double percPD = (geometricalAverage.get(LUNG_RIGHT_ANT)
//				/ (1.0 * geometricalAverage.get(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT))) * 100;
//		retour[RESULT_LUNG_RIGHT] += " (" + us.format(percPD) + "%)";
//		double percPG = (geometricalAverage.get(LUNG_LEFT_ANT)
//				/ (1.0 * geometricalAverage.get(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT))) * 100;
//		retour[RESULT_LUNG_LEFT] += " (" + us.format(percPG) + "%)";
//		int totmg = geometricalAverage.get(LUNG_RIGHT_ANT) + geometricalAverage.get(LUNG_LEFT_ANT);
//		retour[RESULT_TOTAL_AVG] = "Total MG : " + totmg;
//		int totshunt = geometricalAverage.get(KIDNEY_RIGHT_ANT) + geometricalAverage.get(KIDNEY_LEFT_ANT)
//				+ geometricalAverage.get(BRAIN_ANT);
//		retour[RESULT_TOTAL_SHUNT] = "Total Shunt : " + totshunt;
//		double percSyst = (100.0 * totshunt) / totmg;
//		retour[RESULT_SYSTEMIC] = "% Systemic : " + us.format(percSyst) + "%";
//		double shunt = ((totshunt * 100.0) / (totmg * 0.38));
//		retour[RESULT_PULMONARY_SHUNT] = "Pulmonary Shunt : " + us.format(shunt) + "% (total blood Flow)";
	}
	
	

}
