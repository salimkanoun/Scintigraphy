package org.petctviewer.scintigraphy.lympho.post;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.petctviewer.scintigraphy.lympho.ModeleLympho;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class ModelePost extends ModeleScin {

	public static final int RIGHT_PELVIS_ANT = 0, LEFT_PELVIS_ANT = 1, BACKGROUND_ANT = 2, RIGHT_PELVIS_POST = 3,
			LEFT_PELVIS_POST = 4, BACKGROUND_POST = 5, TOTAL_ORGANS = 6;

	private boolean locked;

	private Map<Integer, Double> coups;

	private Map<Integer, Integer> geometricalAverage;

	private String[] retour;

	TabResult resutlTab;

	private ImagePlus pelvisMontage;

	private List<Double> results;

	public ModelePost(ImageSelection[] selectedImages, String studyName, TabResult resultTab) {
		super(selectedImages, studyName);

		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();

		this.retour = new String[9];

		this.resutlTab = resultTab;

		this.results = new ArrayList<>();
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
		double correctedRadioactiveDecrease;
		if (organ % 3 == 2) {
			correctedRadioactiveDecrease = Library_Quantif.getCounts(imp);
		} else {
			System.out.println("\n\n\tAvant correction : " + Library_Quantif.getCounts(imp));
			correctedRadioactiveDecrease = Library_Quantif.getCountCorrectedBackground(imp,
					this.roiManager.getRoi(organ), this.roiManager.getRoi(((organ / 3) * 3) + 2));
		}
		this.coups.put(organ, correctedRadioactiveDecrease);
		System.out.println("Calculations for " + organ + " [" + ModelePost.convertOrgan(organ) + "] -> "
				+ correctedRadioactiveDecrease + "\n\n");
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
		Library_Quantif.getCountCorrectedBackground(this.selectedImages[organ / 3].getImagePlus(),
				this.roiManager.getRoi(organ), this.roiManager.getRoi((organ / 3) + 3));
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
		double GeometricAverageRight = geometricalAverage.get(RIGHT_PELVIS_ANT);
		retour[0] += " " + us.format(GeometricAverageRight) + "";

		retour[1] = "Geometric Average Drainage LEFT : ";
		double GeometricAverageLeft = geometricalAverage.get(LEFT_PELVIS_ANT);
		retour[1] += " " + us.format(GeometricAverageLeft) + "";

		retour[2] = "Gradient Right/Left : ";
		double LeftRightGradient = ((GeometricAverageRight - GeometricAverageLeft) / GeometricAverageLeft) * 100;
		retour[2] += " " + us.format(LeftRightGradient) + "%";

		retour[3] = "Gradient Left/Right : ";
		double RightLeftGradient = ((GeometricAverageLeft - GeometricAverageRight) / GeometricAverageRight) * 100;
		retour[3] += " " + us.format(RightLeftGradient) + "%";

		((ModeleLympho) resutlTab.getParent().getModel()).getInjectionRatio();

		this.results.add(Double.valueOf(us.format(GeometricAverageRight)));
		this.results.add(Double.valueOf(us.format(GeometricAverageLeft)));
		this.results.add(Double.valueOf(us.format(LeftRightGradient)));
		this.results.add(Double.valueOf(us.format(RightLeftGradient)));

	}

	public ImagePlus getPelvisMontage() {
		return this.pelvisMontage;
	}

	public void setPelvisMontage(ImagePlus pelvisMontage) {
		this.pelvisMontage = pelvisMontage;
	}

	@Override
	public String toString() {

		String s = "";

		s += ",Right,Left\n";
		s += "Geometric Average Drainage," + this.results.get(0) + "," + results.get(1) + "\n\n";
		s += "Gradient Right/Left," + this.results.get(2) + "\n";
		s += "Gradient Left/Right," + this.results.get(3) + "\n\n";

		return s;
	}

}
