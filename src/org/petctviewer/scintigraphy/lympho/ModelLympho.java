package org.petctviewer.scintigraphy.lympho;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.petctviewer.scintigraphy.lympho.gui.TabPelvis;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;

public class ModelLympho extends ModelScin {

	public static final int FOOT_RIGHT_ANT_FIRST = 0, FOOT_LEFT_ANT_FIRST = 1, FOOT_RIGHT_POST_FIRST = 2,
			FOOT_LEFT_POST_FIRST = 3, FOOT_RIGHT_ANT_SECOND = 4, FOOT_LEFT_ANT_SECOND = 5, FOOT_RIGHT_POST_SECOND = 6,
			FOOT_LEFT_POST_SECOND = 7, TOTAL_ORGANS = 8;

	private static final int RESULT_FOOT_RIGHT_FIRST = 0, RESULT_FOOT_LEFT_FIRST = 1, RESULT_FOOT_RIGHT_SECOND = 2,
			RESULT_FOOT_LEFT_SECOND = 3;
	
	public static final int RIGHT_PELVIS_ANT = 0, LEFT_PELVIS_ANT = 1, BACKGROUND_ANT = 2, RIGHT_PELVIS_POST = 3,
			LEFT_PELVIS_POST = 4, BACKGROUND_POST = 5, TOTAL_ORGANS_PELVIS = 6;

	private boolean locked;

	private final Map<Integer, Double> coups;

	private final Map<Integer, Integer> geometricalAverage;

	private String[] retour;

	private TabResult resutlTab;

	private final List<Double> results;

	private int idImagePelvis;
	
	private ImagePlus pelvisMontage;
	
	private String[] retourPelvis;
	
	private final List<Double> resultsPelvis;

	//private final Map<Integer, Double> coupsPelvis;
	
	private final Map<Integer, Integer> geometricalAveragePelvis;
	
	private int nbRoiLympho;
	
	public ModelLympho(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.coups = new HashMap<>();
		this.geometricalAverage = new HashMap<>();

		this.retour = new String[9];

		this.results = new ArrayList<>();
		
		this.resultsPelvis = new ArrayList<>();
	//	this.coupsPelvis = new HashMap<>();
		this.geometricalAveragePelvis = new HashMap<>();
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String[] getResult() {
		return this.retour;
	}

	protected void calculerCoups(int organ, ImagePlus imp) {
		
		double correctedRadioactiveDecrease;
		if (!(imp == getImagePlus())) {
//			correctedRadioactiveDecrease = Library_Quantif.calculer_countCorrected(18902000,
//					Library_Quantif.getCounts(imp), Isotope.TECHNETIUM_99);
			correctedRadioactiveDecrease = Library_Quantif.calculer_countCorrected(getImagePlus(), imp,Isotope.TECHNETIUM_99);
		} else {
			correctedRadioactiveDecrease = Library_Quantif.getCounts(imp);
		}
//		System.out.println("\t\tAvant correction : " + Library_Quantif.getCounts(imp));
//		System.out.println("\t\tAprès correction : " + correctedRadioactiveDecrease);
		this.coups.put(organ, correctedRadioactiveDecrease);
		// System.out.println("Calculations for " + organ + " [" +
		// ModelLympho.convertOrgan(organ) + "] -> "
		// + correctedRadioactiveDecrease);
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
//		System.out.println("\n\n\n\n\n\n----------------------------\n\n\n\n\n\n");
//		System.out.println("nbCounts : " + this.coups.size());
//		for (Double count : this.coups.values().toArray(new Double[0])) {
//			System.out.println("Counts : " + count);
//		}
		this.moyenneGeo(FOOT_RIGHT_ANT_FIRST);
		this.moyenneGeo(FOOT_LEFT_ANT_FIRST);
		this.moyenneGeo(FOOT_RIGHT_ANT_SECOND);
		this.moyenneGeo(FOOT_LEFT_ANT_SECOND);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeo(int organ) {
		geometricalAverage.put(organ, (int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 2)));
//		System.out.println("MG " + organ + " [" + ModelLympho.convertOrgan(organ) + "/ "
//				+ ModelLympho.convertOrgan(organ + 2) + "] --- [" + this.coups.get(organ) + "/"
//				+ this.coups.get(organ + 2) + "] -> " + geometricalAverage.get(organ));

	}

	@Override
	public void calculateResults() {
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
		retour[0] = "Geometric Average : ";
		retour[1] = "<html>Drainage RIGHT : ";
		double drainedPercentageRight = 100 - ((geometricalAverage.get(FOOT_RIGHT_ANT_SECOND)
				/ (1.0 * geometricalAverage.get(FOOT_RIGHT_ANT_FIRST))) * 100);
		if (drainedPercentageRight < 30) 
			retour[1] += "<span style=\"color:red\"> " + us.format(drainedPercentageRight) + "%</span></html>";
		else
			retour[1] += " " + us.format(drainedPercentageRight) + "%";

		retour[2] = "<html> Drainage LEFT : ";
		double drainedPercentageLeft = 100
				- ((geometricalAverage.get(FOOT_LEFT_ANT_SECOND) / (1.0 * geometricalAverage.get(FOOT_LEFT_ANT_FIRST)))
						* 100);
		if (drainedPercentageLeft < 30)
			retour[2] += "<span style=\"color:red\"> " + us.format(drainedPercentageLeft) + "%</span></html>";
		else
			retour[2] += " " + us.format(drainedPercentageLeft) + "%";
		
		
		double drainedDelta = Math.abs(((Math.abs(drainedPercentageRight) - Math.abs(drainedPercentageLeft))));
		if(drainedPercentageRight < 0 || drainedPercentageLeft < 0) 
			retour[3] = "";
		else
			retour[3] = "Delta Drainage : "+" " + us.format(drainedDelta) + "";
		
		retour[4] = "";

//		retour[3] = "L < R of : ";
//		double relativeDeltaToRight = (drainedDelta / drainedPercentageRight) * 100;
//		retour[3] += " " + us.format(relativeDeltaToRight) + "%";
		if(drainedPercentageRight < 0 || drainedPercentageLeft < 0) {
			retour[5] = "<html><span style=\"color:red\">NEGATIVE DRAINAGE, ANY RESULT WOULD BE ABSURD. </span></html>";
		}
		else if (drainedDelta < 0.25 * ((int) (drainedPercentageRight + drainedPercentageLeft) / 2)) {
			retour[5] = "<html><span style=\"color:green\">Non significant dissymetry because delta &lsaquo; 0.25 MG  ";
			double quartOfMG = 0.25 * ((int) (drainedPercentageRight + drainedPercentageLeft) / 2);
			retour[5] += " (" + us.format(quartOfMG) + " ) </span></html>";
		} else {
			retour[5] = "<html><span style=\"color:red\">Significant dissymetry because delta &raquo; 0.25 MG  ";
			double quartOfMG = 0.25 * ((int) (drainedPercentageRight + drainedPercentageLeft) / 2);
			retour[5] += " (" + us.format(quartOfMG) + " ) </span></html>";
		}

		retour[6] = "<html><span style=\"color:deeppink\">Injection ratio of MG Left/Right:   ";
		double injectionRatio = this.coups.get(1) / this.coups.get(0);
		retour[6] += " " + us.format(injectionRatio) + "  </span></html>";

		this.results.add(Double.valueOf(us.format(injectionRatio)));
		this.results.add(Double.valueOf(us.format(drainedPercentageRight)));
		this.results.add(Double.valueOf(us.format(drainedPercentageLeft)));
		this.results.add(Double.valueOf(us.format(drainedDelta)));
//		this.results.add(Double.valueOf(us.format(relativeDeltaToRight)));

	}

	public void getInjectionRatio() {
	}

	public void setResultTab(TabResult resultTab) {
		this.resutlTab = resultTab;
	}

	@Override
	public String toString() {

		String s = "";

		s += "\n\nInjection Ratio Left/Right," + Library_Quantif.round(this.results.get(0), 2) + "\n\n";

		s += ",Right,Left\n";
		s += "Geometric Average," + Library_Quantif.round(this.results.get(1), 2)
				+ "," + Library_Quantif.round(results.get(2), 2) + "\n\n";
		s += "Delta Drainage," + Library_Quantif.round(this.results.get(3), 2) + "\n";
//		s += "L < R of :," + this.results.get(4) + "\n\n\n";

		if (this.resutlTab != null)
			if (((TabPelvis) this.resutlTab).getVueBasic() != null)
				s += this.toCSVPelvis();

		s += super.toString();

		return s;
	}
	
	
	/*
	 * 
	 * 
	 *  ------------------------
	 * |                         |
	 * |      *************      |
	 * |      *Pelvis Part*      |
	 * |      *************      |
	 * |                         |
	 *  ------------------------
	 * 
	 */
	
	public void setImagePelvis(ImageSelection imagePelvis) {
		ImageSelection[] newBoundsOfImages = new ImageSelection[this.getImageSelection().length + 1];
		for(int i = 0 ; i < this.getImageSelection().length ; i++)
			newBoundsOfImages[i] = this.getImageSelection()[i];
		
		newBoundsOfImages[newBoundsOfImages.length - 1] = imagePelvis;
		
		this.setImages(newBoundsOfImages);
		this.idImagePelvis = newBoundsOfImages.length - 1;
		System.out.println("this.getImageSelection().length : "+this.getImageSelection().length);
		System.out.println("newBoundsOfImages.length : "+newBoundsOfImages.length);
	}

	public ImageSelection getImagePelvis() {
		System.out.println("this.idImagePelvis : "+this.idImagePelvis);
		return this.getImageSelection()[this.idImagePelvis];
	}


	public String[] getResultPelvis() {
		return this.retourPelvis;
	}

	public void calculerCoupsPelvis(int organ, ImagePlus imp) {
		double correctedRadioactiveDecrease;
		if (organ % 3 == 2) {
			correctedRadioactiveDecrease = Library_Quantif.getCounts(imp);
		} else {
//			System.out.println("\n\n\tAvant correction : " + Library_Quantif.getCounts(imp));
			correctedRadioactiveDecrease = Library_Quantif.getCountCorrectedBackground(imp,
					this.roiManager.getRoi(organ + this.nbRoiLympho), this.roiManager.getRoi(((organ / 3) * 3) + 2 + this.nbRoiLympho));
		}
	//	this.coupsPelvis.put(organ, correctedRadioactiveDecrease);
		System.out.println("Calculations for " + organ + " [" + this.convertOrganPelvis(organ) + "] -> "
				+ correctedRadioactiveDecrease + "\n\n");
	}

	public String convertOrganPelvis(int organ) {
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

	private void computeGeometricalAveragePelvis() {
//		System.out.println("\n\n\n\n\n\n----------------------------\n\n\n\n\n\n");
//		System.out.println("nbCounts : " + this.coups.size());
//		for (Double count : this.coups.values().toArray(new Double[0])) {
//			System.out.println("Counts : " + count);
//		}
		this.moyenneGeoPelvis(RIGHT_PELVIS_ANT);
		this.moyenneGeoPelvis(LEFT_PELVIS_ANT);
	}

	// Calcule la moyenne g茅om茅trique pour un organe sp茅cifique
	// Si abv = PD alors on calculera la MG pour le poumon droit
	private void moyenneGeoPelvis(int organ) {
		Library_Quantif.getCountCorrectedBackground(this.selectedImages[organ / 3].getImagePlus(),
				this.roiManager.getRoi(organ + this.nbRoiLympho), this.roiManager.getRoi((organ / 3) + 3 + this.nbRoiLympho));
		geometricalAveragePelvis.put(organ, (int) Library_Quantif.moyGeom(this.coups.get(organ), this.coups.get(organ + 3)));
//		System.out.println("MG " + organ + " [" + ModelLympho.convertOrgan(organ) + "/ "
//				+ ModelLympho.convertOrgan(organ + 3) + "] --- [" + this.coups.get(organ) + "/"
//				+ this.coups.get(organ + 3) + "] -> " + geometricalAverage.get(organ));

	}

	public void calculateResultsPelvis() {
		this.retourPelvis = new String[9];

		// Permet de definir le nombre de chiffre apr猫s la virgule et mettre la
		// virgue en system US avec un .
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.##");
		us.setDecimalFormatSymbols(sym);

		// Les 5 MGs
		computeGeometricalAveragePelvis();
		retourPelvis[0] = "Geometric Average Drainage RIGHT : ";
		double GeometricAverageRight = geometricalAveragePelvis.get(RIGHT_PELVIS_ANT);
		retourPelvis[0] += " " + us.format(GeometricAverageRight) + "";

		retourPelvis[1] = "Geometric Average Drainage LEFT : ";
		double GeometricAverageLeft = geometricalAveragePelvis.get(LEFT_PELVIS_ANT);
		retourPelvis[1] += " " + us.format(GeometricAverageLeft) + "";

		retourPelvis[2] = "Gradient Right/Left : ";
		double LeftRightGradient = ((GeometricAverageRight - GeometricAverageLeft) / GeometricAverageLeft) * 100;
		retourPelvis[2] += " " + us.format(LeftRightGradient) + "%";

		retourPelvis[3] = "Gradient Left/Right : ";
		double RightLeftGradient = ((GeometricAverageLeft - GeometricAverageRight) / GeometricAverageRight) * 100;
		retourPelvis[3] += " " + us.format(RightLeftGradient) + "%";

		this.getInjectionRatio();

		this.resultsPelvis.add(Double.valueOf(us.format(GeometricAverageRight)));
		this.resultsPelvis.add(Double.valueOf(us.format(GeometricAverageLeft)));
		this.resultsPelvis.add(Double.valueOf(us.format(LeftRightGradient)));
		this.resultsPelvis.add(Double.valueOf(us.format(RightLeftGradient)));

	}

	public ImagePlus getPelvisMontage() {
		return this.pelvisMontage;
	}

	public void setPelvisMontage(ImagePlus pelvisMontage) {
		this.pelvisMontage = pelvisMontage;
	}
	
	public String toCSVPelvis() {
		String s = "";

		s += ",Right,Left\n";
		s += "Geometric Average Drainage," + Library_Quantif.round(this.resultsPelvis.get(0), 2)
				+ "," + Library_Quantif.round(resultsPelvis.get(1), 2) + "\n\n";
		s += "Gradient Right/Left," + Library_Quantif.round(this.resultsPelvis.get(2), 2) + "\n";
		s += "Gradient Left/Right," + Library_Quantif.round(this.resultsPelvis.get(3), 2) + "\n\n";

		return s;
	}
	
	
	
	public void setNbRoiLympho (int nbRoiLympho){
		this.nbRoiLympho = nbRoiLympho;
	}
	
	public int getNbRoiLympho() {
		return this.nbRoiLympho;
	}
	
	
	
	
	
	
}
