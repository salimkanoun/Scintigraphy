package org.petctviewer.scintigraphy.lympho.post;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.lympho.ModeleLympho;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.ImagePlus;

public class ModelePost extends ModeleScin {

	public static final int RIGHT_PELVIS_ANT = 0, LEFT_PELVIS_ANT = 1,BACKGROUND_ANT = 2, RIGHT_PELVIS_POST = 3, LEFT_PELVIS_POST = 4, BACKGROUND_POST = 5,
			TOTAL_ORGANS = 6;

	private static final int RESULT_PELVIS_RIGHT = 0, RESULT_PELVIS_LEFT = 1;

	private boolean locked;

	private Map<Integer, Double> coups;

	private Map<Integer, Integer> geometricalAverage;

	private String[] retour;

	TabResult resutlTab;
	
	private ImagePlus pelvisMontage;

	public ModelePost(ImageSelection[] selectedImages, String studyName, TabResult resultTab) {
		super(selectedImages, studyName);

		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();

		this.retour = new String[9];

		this.resutlTab = resultTab;
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
		// double correctedRadioactiveDecrease =
		// Library_Quantif.calculer_countCorrected(getImagePlus(), imp,
		// Isotope.TECHNICIUM_99);
		double correctedRadioactiveDecrease;
		if(organ%3==2) {
			correctedRadioactiveDecrease = Library_Quantif.getCounts(imp);
		}else {
			correctedRadioactiveDecrease = Library_Quantif.getCountCorrectedBackground(imp, this.roiManager.getRoi(organ), this.roiManager.getRoi((organ/3)+2));
			System.out.println("\t\tAvant correction : " + Library_Quantif.getCounts(imp));
			System.out.println("\t\tAprès correction : " + correctedRadioactiveDecrease);
		}
		this.coups.put(organ, correctedRadioactiveDecrease);
		System.out.println("Calculations for " + organ + " [" + ModeleLympho.convertOrgan(organ) + "] -> "
				+ correctedRadioactiveDecrease);
	}

	public static String convertOrgan(int organ) {
		switch (organ) {
		case RIGHT_PELVIS_ANT:
			return "Right Pelvis First Image ANT: ";
		case RIGHT_PELVIS_POST:
			return "Right Pelvis First Image POST: ";
		case LEFT_PELVIS_ANT:
			return "Left Pelvis First Image ANT: ";
		case LEFT_PELVIS_POST:
			return "Left Pelvis First Image POST: ";
		case BACKGROUND_ANT:
			return "Background ANT: ";
		case BACKGROUND_POST:
			return "Background POST: ";
		default:
			return "Unknown Organ (" + organ + "): ";
		}
	}

	private void computeGeometricalAverage() {
		System.out.println("\n\n\n\n\n\n----------------------------\n\n\n\n\n\n");
		System.out.println("nbCounts : " + this.coups.size());
		for (Double count : this.coups.values().toArray(new Double[this.coups.size()])) {
			System.out.println("Counts : " + count);
		}
		this.moyenneGeo(RIGHT_PELVIS_ANT);
		this.moyenneGeo(LEFT_PELVIS_ANT);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(int organ) {
		Library_Quantif.getCountCorrectedBackground(this.selectedImages[organ/3].getImagePlus(), this.roiManager.getRoi(organ), this.roiManager.getRoi((organ/3)+3));
		geometricalAverage.put(organ, (int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 3)));
		System.out.println("MG " + organ + " [" + ModeleLympho.convertOrgan(organ) + "/ "
				+ ModeleLympho.convertOrgan(organ + 3) + "] --- [" + this.coups.get(organ) + "/"
				+ this.coups.get(organ + 3) + "] -> " + geometricalAverage.get(organ));

	}

	@Override
	public void calculerResultats() {
		this.retour = new String[9];
		
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);

		// Les 5 MGs
		computeGeometricalAverage();
		retour[0] = "Geometric Average Drainage RIGHT : ";
		double GeometricAverageRight = geometricalAverage.get(RIGHT_PELVIS_ANT) ;
		retour[0] += " " + us.format(GeometricAverageRight) + "%";
		
		retour[1] = "Geometric Average Drainage RIGHT : ";
		double GeometricAverageLeft = geometricalAverage.get(LEFT_PELVIS_ANT) ;
		retour[1] += " " + us.format(GeometricAverageLeft) + "%";
		
		retour[2] = "Gradient Right/Left : ";
		double LeftRightGradient = (GeometricAverageRight/GeometricAverageLeft) - 100;
		retour[2] += " " + us.format(LeftRightGradient) + "%";
		
		retour[3] = "Gradient Left/Right : ";
		double RightLeftGradient = (GeometricAverageLeft/GeometricAverageRight) - 100;
		retour[3] += " " + us.format(RightLeftGradient) + "%";



		
		((ModeleLympho)resutlTab.getParent().getModel()).getInjectionRatio();

		this.resutlTab.updateResultFrame((ModelePost) this);

	}

	public ImagePlus getPelvisMontage() {
		return this.pelvisMontage;
	}
	
	public void setPelvisMontage(ImagePlus pelvisMontage) {
		this.pelvisMontage = pelvisMontage;
	}

}
