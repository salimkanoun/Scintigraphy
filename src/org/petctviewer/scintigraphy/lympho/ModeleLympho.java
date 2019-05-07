package org.petctviewer.scintigraphy.lympho;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.petctviewer.scintigraphy.lympho.gui.TabPost;
import org.petctviewer.scintigraphy.lympho.post.ControllerWorkflowPelvis;
import org.petctviewer.scintigraphy.lympho.post.ModelePost;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.ImagePlus;
import ij.util.DicomTools;

public class ModeleLympho extends ModeleScin {

	private double injectionRatio;

	public static final int FOOT_RIGHT_ANT_FIRST = 0, FOOT_LEFT_ANT_FIRST = 1, FOOT_RIGHT_POST_FIRST = 2,
			FOOT_LEFT_POST_FIRST = 3, FOOT_RIGHT_ANT_SECOND = 4, FOOT_LEFT_ANT_SECOND = 5, FOOT_RIGHT_POST_SECOND = 6,
			FOOT_LEFT_POST_SECOND = 7, TOTAL_ORGANS = 8;

	private static final int RESULT_FOOT_RIGHT_FIRST = 0, RESULT_FOOT_LEFT_FIRST = 1, RESULT_FOOT_RIGHT_SECOND = 2,
			RESULT_FOOT_LEFT_SECOND = 3;

	private boolean locked;

	private Map<Integer, Double> coups;

	private Map<Integer, Integer> geometricalAverage;

	private String[] retour;

	private TabResult resutlTab;

	private List<Double> results;

	public ModeleLympho(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();

		this.retour = new String[9];

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
		// double correctedRadioactiveDecrease =
		// Library_Quantif.calculer_countCorrected(getImagePlus(), imp,
		// Isotope.TECHNICIUM_99);
		double correctedRadioactiveDecrease;
		if (!(imp == getImagePlus())) {
			correctedRadioactiveDecrease = Library_Quantif.calculer_countCorrected(18902000,
					Library_Quantif.getCounts(imp), Isotope.TECHNICIUM_99);
		} else {
			correctedRadioactiveDecrease = Library_Quantif.getCounts(imp);
		}
		System.out.println("\t\tAvant correction : " + Library_Quantif.getCounts(imp));
		System.out.println("\t\tAprès correction : " + correctedRadioactiveDecrease);
		this.coups.put(organ, correctedRadioactiveDecrease);
		System.out.println("Calculations for " + organ + " [" + ModeleLympho.convertOrgan(organ) + "] -> "
				+ correctedRadioactiveDecrease);
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
		System.out.println("nbCounts : " + this.coups.size());
		for (Double count : this.coups.values().toArray(new Double[this.coups.size()])) {
			System.out.println("Counts : " + count);
		}
		this.moyenneGeo(FOOT_RIGHT_ANT_FIRST);
		this.moyenneGeo(FOOT_LEFT_ANT_FIRST);
		this.moyenneGeo(FOOT_RIGHT_ANT_SECOND);
		this.moyenneGeo(FOOT_LEFT_ANT_SECOND);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(int organ) {
		geometricalAverage.put(organ, (int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 2)));
		System.out.println("MG " + organ + " [" + ModeleLympho.convertOrgan(organ) + "/ "
				+ ModeleLympho.convertOrgan(organ + 2) + "] --- [" + this.coups.get(organ) + "/"
				+ this.coups.get(organ + 2) + "] -> " + geometricalAverage.get(organ));

	}

	@Override
	public void calculerResultats() {
		this.retour = new String[9];

		// Les 5 MGs
		computeGeometricalAverage();
		retour[RESULT_FOOT_RIGHT_FIRST] = convertOrgan(FOOT_RIGHT_ANT_FIRST)
				+ geometricalAverage.get(FOOT_RIGHT_ANT_FIRST);
		retour[RESULT_FOOT_LEFT_FIRST] = convertOrgan(FOOT_LEFT_ANT_FIRST)
				+ geometricalAverage.get(FOOT_LEFT_ANT_FIRST);
		retour[RESULT_FOOT_RIGHT_SECOND] = convertOrgan(FOOT_RIGHT_ANT_SECOND)
				+ geometricalAverage.get(FOOT_RIGHT_ANT_SECOND);
		retour[RESULT_FOOT_LEFT_SECOND] = convertOrgan(FOOT_LEFT_ANT_SECOND)
				+ geometricalAverage.get(FOOT_LEFT_ANT_SECOND);
		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);

		// Calculs

		retour[0] = "Geometric Average Drainage RIGHT : ";
		double drainedPercentageRight = 100 - ((geometricalAverage.get(FOOT_RIGHT_ANT_SECOND)
				/ (1.0 * geometricalAverage.get(FOOT_RIGHT_ANT_FIRST))) * 100);
		retour[0] += " -" + us.format(drainedPercentageRight) + "%";

		retour[1] = "Geometric Average Drainage LEFT : ";
		double drainedPercentageLeft = 100
				- ((geometricalAverage.get(FOOT_LEFT_ANT_SECOND) / (1.0 * geometricalAverage.get(FOOT_LEFT_ANT_FIRST)))
						* 100);
		retour[1] += " -" + us.format(drainedPercentageLeft) + "%";

		retour[2] = "Delta Drainage : ";
		double drainedDelta = (drainedPercentageRight - drainedPercentageLeft);
		retour[2] += " " + us.format(drainedDelta) + "";

		retour[3] = "L < R of : ";
		double relativeDeltaToRight = (drainedDelta / drainedPercentageRight) * 100;
		retour[3] += " " + us.format(relativeDeltaToRight) + "%";
		if (relativeDeltaToRight < 0.25 * ((int) (drainedPercentageRight + drainedPercentageLeft) / 2)) {
			retour[4] = "<html><span style=\"color:green\">Non significant dissymetry because delta &lsaquo; 0.25 MG  ";
			double quartOfMG = 0.25 * ((int) (drainedPercentageRight + drainedPercentageLeft) / 2);
			retour[4] += " (" + us.format(quartOfMG) + " ) </span></html>";
		} else {
			retour[4] = "<html><span style=\"color:red\">Significant dissymetry because delta &lraquo; 0.25 MG  ";
			double quartOfMG = 0.25 * ((int) (drainedPercentageRight + drainedPercentageLeft) / 2);
			retour[4] += " (" + us.format(quartOfMG) + " ) </span></html>";
		}

		retour[5] = "<html><span style=\"color:deeppink\">Injection ratio of MG Left/Right:   ";
		double injectionRatio = this.coups.get(1) / this.coups.get(0);
		retour[5] += " " + us.format(injectionRatio) + "  </span></html>";
		this.injectionRatio = injectionRatio;

		this.results.add(Double.valueOf(us.format(injectionRatio)));
		this.results.add(Double.valueOf(us.format(drainedPercentageRight)));
		this.results.add(Double.valueOf(us.format(drainedPercentageLeft)));
		this.results.add(Double.valueOf(us.format(drainedDelta)));
		this.results.add(Double.valueOf(us.format(relativeDeltaToRight)));

	}

	public double getInjectionRatio() {
		return this.injectionRatio;
	}

	public void setResultTab(TabResult resultTab) {
		this.resutlTab = resultTab;
	}

	@Override
	public String toString() {

		String s = "";

		s += "\n\nInjection Ratio Left/Right," + this.results.get(0) + "\n\n";

		s += ",Right,Left\n";
		s += "Geometric Average," + this.results.get(1) + "," + results.get(2) + "\n\n";
		s += "Delta Drainage," + this.results.get(3) + "\n";
		s += "L < R of :," + this.results.get(4) + "\n\n\n";

		if (((TabPost) this.resutlTab) != null)
			if (((TabPost) this.resutlTab).getVueBasic() != null)
				s += ((ModelePost) ((ControllerWorkflowPelvis) ((TabPost) this.resutlTab).getVueBasic().getFenApplication()
						.getControleur()).getModel()).toString();

		s += super.toString();

		return s;
	}

}
