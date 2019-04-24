package org.petctviewer.scintigraphy.lympho;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;
import org.petctviewer.scintigraphy.shunpo.ModeleShunpo;

import ij.ImagePlus;

public class ModeleLympho extends ModeleScin{
	
	public static final int FOOT_RIGHT_ANT_FIRST = 0, FOOT_LEFT_ANT_FIRST = 1, FOOT_RIGHT_POST_FIRST = 2,  FOOT_LEFT_POST_FIRST = 3,
			FOOT_RIGHT_ANT_SECOND = 4,FOOT_LEFT_ANT_SECOND = 5, FOOT_RIGHT_POST_SECOND = 6,  FOOT_LEFT_POST_SECOND = 7, TOTAL_ORGANS = 8;
	
	private static final int RESULT_FOOT_RIGHT_FIRST = 0, RESULT_FOOT_LEFT_FIRST = 1, RESULT_FOOT_RIGHT_SECOND = 2,
			RESULT_FOOT_LEFT_SECOND = 3, GEOM_AVG = 4, GEOM_AVG_2 = 5, GEOM_AVG_3 = 6, GEOM_AVG_4 = 7;

	private boolean locked;
	
	private Map<Integer, Double> coups;
	
	private Map<Integer, Integer> geometricalAverage;
	
	private String[] retour;

	private double ratio;

	public ModeleLympho(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		
		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();
		
		this.retour = new String[9];
		
		this.calculerRatio();
	}	
	
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
//		double correctedRadioactiveDecrease = Library_Quantif.calculer_countCorrected(getImagePlus(), imp, Isotope.TECHNICIUM_99);
		double correctedRadioactiveDecrease;
		if(!(imp == getImagePlus())) {
			correctedRadioactiveDecrease = Library_Quantif.calculer_countCorrected(18902000,Library_Quantif.getCounts(imp),Isotope.TECHNICIUM_99);
		}else {
			correctedRadioactiveDecrease = Library_Quantif.getCounts(imp);
		}
		System.out.println("\t\tAvant correction : "+Library_Quantif.getCounts(imp));
		System.out.println("\t\tAprès correction : "+correctedRadioactiveDecrease);
		this.coups.put(organ, correctedRadioactiveDecrease);
		System.out.println("Calculations for " + organ + " [" + ModeleLympho.convertOrgan(organ) + "] -> " + correctedRadioactiveDecrease);
	}
	
	public static String convertOrgan(int organ) {
		switch (organ) {
		case FOOT_RIGHT_ANT_FIRST:
			return "Right Foot First Image ANT: ";
		case FOOT_RIGHT_POST_FIRST:
			return "Right Foot First Image POST: ";
		case FOOT_LEFT_ANT_FIRST:
			return "Left Foot First Image ANT: ";
		case FOOT_LEFT_POST_FIRST:
			return "Left Foot First Image POST: ";
		case FOOT_RIGHT_ANT_SECOND:
			return "Right Foot Second Image ANT: ";
		case FOOT_RIGHT_POST_SECOND:
			return "Right Foot Second Image POST: ";
		case FOOT_LEFT_ANT_SECOND:
			return "Left Foot Second Image ANT: ";
		case FOOT_LEFT_POST_SECOND:
			return "Left Foot Second Image POST: ";
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
		System.out.println("MG " + organ + " [" + ModeleLympho.convertOrgan(organ) + "/ "+ModeleLympho.convertOrgan(organ+2)+"] --- ["+this.coups.get(organ) +"/"+this.coups.get(organ + 2)+"] -> " + geometricalAverage.get(organ));

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
		retour[GEOM_AVG_3] ="MGDrainage Droit : ";
		double percPD = (geometricalAverage.get(FOOT_RIGHT_ANT_SECOND)
				/ (1.0 * geometricalAverage.get(FOOT_RIGHT_ANT_FIRST))) * 100;
		retour[GEOM_AVG_3] += " " + us.format(percPD) + "%";
		
		
		retour[GEOM_AVG_4] ="MGDrainage Gauche : ";
		double percPDP = (geometricalAverage.get(FOOT_LEFT_ANT_SECOND)
				/ (1.0 * geometricalAverage.get(FOOT_LEFT_ANT_FIRST))) * 100;
		retour[GEOM_AVG_4] += " " + us.format(percPDP) + "%";
		
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
	
	

	
	public void calculerRatio() {
		int timeStatic = Library_Dicom.getFrameDuration(getImagesPlus()[1]);
		int[] timesDynamic = Library_Dicom.buildFrameDurations(getImagePlus());
		int acquisitionTimeDynamic = 0;
		for (int times : timesDynamic) {
			acquisitionTimeDynamic += times;
		}
		this.ratio =  (timeStatic*1.0D / acquisitionTimeDynamic*1.0D);

	}
}
