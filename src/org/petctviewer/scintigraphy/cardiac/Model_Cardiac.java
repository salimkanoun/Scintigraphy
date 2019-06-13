package org.petctviewer.scintigraphy.cardiac;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;
import ij.gui.Roi;

public class Model_Cardiac extends ModelScin {

	private final HashMap<String, Double[]> data;

	private final HashMap<String, Double[]> dataVisualGradation;

	/** Valeurs mesur�es **/
	// valeurs de la prise late
	private Double fixCoeurL, fixReinGL, fixReinDL, fixVessieL, fixBkgNoiseA, fixBkgNoiseP, heartToContralateral;
	// valeurs des contamination
	private Double sumContE = 0.0, sumContL = 0.0;
	// valeurs totales
	private Double totEarly, totLate;

	private Double hwb, retCardiaque, retCe;

	private Boolean deuxPrises;

	private final Scintigraphy scin;

	// private Controller_Cardiac controler;

	private final HashMap<String, String> resultats;

	private final HashMap<String, String> resultatsVisualGradation;

	private ImageSelection imageVisualGradation;

	private int fullBodyImages;

	private int onlyThoaxImage;

	// Value of Only_Thorax image
	private Double heartThorax, contralateralThorax, heartToContralateralThorax;
	
	private double wholeBodyRetention;
	
	private double heartRetention;
	
	private double heartToWholeBody;

	// private int[] nbConta;

	public Model_Cardiac(Scintigraphy scin, ImageSelection[] selectedImages, String studyName,
			String[] infoOfAllImages) {
		super(selectedImages, studyName);
		this.scin = scin;
		this.resultats = new HashMap<>();
		this.resultatsVisualGradation = new HashMap<>();
		this.data = new HashMap<>();
		this.dataVisualGradation = new HashMap<>();

		// Because info are deleted in the setLut of the FenApplication, probably
		// because of the concatenate
		// (behavior only on this exam), but we don't know why.
		for (int indexImage = 0; indexImage < this.getImageSelection().length; indexImage++)
			this.getImageSelection()[indexImage].getImagePlus().setProperty("Info", infoOfAllImages[indexImage]);
	}

	public void getResults() {
		// controler=(Controller_Cardiac) scin.getFenApplication().getController();
		ControllerWorkflowCardiac controler = (ControllerWorkflowCardiac) scin.getFenApplication().getController();

		// for (int i : controler.getNomRois().keySet()) {
		// this.selectedImages[0].getImagePlus().setSlice(controler.getSliceNumberByRoiIndex(i));
		// this.selectedImages[0].getImagePlus().setRoi((Roi)
		// controler.getRoiManager().getRoi(i).clone());
		//
		// //Array of Double, in 0 raw count, in 1 average count, in 2 number of pixels
		// Double[] counts=new Double[3];
		// counts[0] =Library_Quantif.getCounts(this.selectedImages[0].getImagePlus());
		// counts[1]
		// =Library_Quantif.getAvgCounts(this.selectedImages[0].getImagePlus());
		// counts[2] =(double)
		// Library_Quantif.getPixelNumber(this.selectedImages[0].getImagePlus());
		//
		// this.data.put(controler.getNomRois().get(i), counts);
		//
		// }

		for (Roi roi : this.getRoiManager().getRoisAsArray()) {
			ImagePlus selectedImage = this.selectedImages[controler.getImageNumberByRoiIndex()].getImagePlus();
			selectedImage.setRoi((Roi) roi.clone());

			// Array of Double, in 0 raw count, in 1 average count, in 2 number of pixels
			Double[] counts = new Double[3];
			counts[0] = Library_Quantif.getCounts(selectedImage);
			counts[1] = Library_Quantif.getAvgCounts(selectedImage);
			counts[2] = (double) Library_Quantif.getPixelNumber(selectedImage);

			this.data.put(roi.getName(), counts);
//			System.out.println("this.dataVisualGradation.put(" + roi.getName() + "( " + counts[0] + ", " + counts[1]
//					+ ", " + counts[2] + "))");

		}

	}

	@Override
	public void calculateResults() {
		if (this.fullBodyImages != 0) {

			// Avg background value of ant and post images for Heart
			Double meanBdfAntHeart = this.data.get("Bkg noise A")[1];
			Double meanBdfPostHeart = this.data.get("Bkg noise P")[1];

			// Avg background value of ant and post images for Bladder
			Double meanBdfAntBladder = this.data.get("Bladder Background A")[1];
			Double meanBdfPostBladder = this.data.get("Bladder Background P")[1];

			// Avg background value of ant and post images for Kidney R
			Double meanBdfAntKidneyR = this.data.get("Kidney R Background A")[1];
			Double meanBdfPostKidneyR = this.data.get("Kidney R Background P")[1];

			// Avg background value of ant and post images for Kidney L
			Double meanBdfAntKidneyL = this.data.get("Kidney L Background A")[1];
			Double meanBdfPostKidneyL = this.data.get("Kidney L Background P")[1];

			// calculation of corrected heart uptake
			Double correctedHeartAnt = this.data.get("Heart A")[0] - (meanBdfAntHeart * this.data.get("Heart A")[2]);
			Double correctedHeartPost = this.data.get("Heart P")[0] - (meanBdfPostHeart * this.data.get("Heart P")[2]);

			// Calculation of corrected Left Renal uptake
			Double correctedKLAnt = this.data.get("Kidney L A")[0]
					- (meanBdfAntKidneyL * this.data.get("Kidney L A")[2]);
			Double correctedKLPost = this.data.get("Kidney L P")[0]
					- (meanBdfPostKidneyL * this.data.get("Kidney L P")[2]);
			System.out.println("correctedKLAnt => "+correctedKLAnt+" = "+this.data.get("Kidney L A")[0]+" - ("+meanBdfAntKidneyL+"*" +this.data.get("Kidney L A")[2]+")");

			System.out.println("correctedKLAnt => "+correctedKLAnt+" = "+this.data.get("Kidney L A")[0]+" - "+(meanBdfAntKidneyL*this.data.get("Kidney L A")[2]));
			
			Double test = (this.data.get("Kidney L A")[1] - meanBdfAntKidneyL) * this.data.get("Kidney L A")[2];
			System.out.println("test => "+test+" = ("+this.data.get("Kidney L A")[1]+" - "+(meanBdfAntKidneyL+" )"+" * "+this.data.get("Kidney L A")[2]));

			// Calculation of corrected Right Renal uptake
			Double correctedKRAnt = this.data.get("Kidney R A")[0]
					- (meanBdfAntKidneyR * this.data.get("Kidney R A")[2]);
			Double correctedKRPost = this.data.get("Kidney R P")[0]
					- (meanBdfPostKidneyR * this.data.get("Kidney R P")[2]);

			// Calculation of corrected Blader uptake
			Double correctedBladAnt = this.data.get("Bladder A")[0]
					- (meanBdfAntBladder * this.data.get("Bladder A")[2]);
			Double correctedBladPost = this.data.get("Bladder P")[0]
					- (meanBdfPostBladder * this.data.get("Bladder P")[2]);

			this.fixBkgNoiseA = meanBdfAntHeart;
			this.fixBkgNoiseP = meanBdfPostHeart;

			// on fait les moyennes geometriques de chaque ROI Late
			this.fixCoeurL = Library_Quantif.moyGeom(correctedHeartAnt, correctedHeartPost);
			this.fixReinGL = Library_Quantif.moyGeom(correctedKLAnt, correctedKLPost);
			this.fixReinDL = Library_Quantif.moyGeom(correctedKRAnt, correctedKRPost);
			this.fixVessieL = Library_Quantif.moyGeom(correctedBladAnt, correctedBladPost);

			// on somme les moyennes geometriques des contaminations

			HashMap<Integer, Double[]> contE = new HashMap<>();
			HashMap<Integer, Double[]> contL = new HashMap<>();
//			System.out.println("this.data.keySet() contient : ");
			for (String s : this.data.keySet()) {
//				System.out.println("\t" + s);
				if (s.startsWith("Cont")) {
					String label = s.substring(s.indexOf(" ") + 2);
//					System.out.println("Label : ");
					int number = Integer.parseInt(label);

					if (s.startsWith("ContE")) {
						if (!contE.containsKey(number))
							contE.put(number, new Double[2]);

						if (s.contains("A"))
							contE.get(number)[0] = this.data.get(s)[0];
						else if (s.contains("P"))
							contE.get(number)[1] = this.data.get(s)[0];

					} else if (s.startsWith("ContL")) {
						if (!contL.containsKey(number))
							contL.put(number, new Double[2]);

						if (s.contains("A"))
							contL.get(number)[0] = this.data.get(s)[0];
						else if (s.contains("P"))
							contL.get(number)[1] = this.data.get(s)[0];

					}
				}
			}

			Double[] nonGeomSumCountE = new Double[] {0.0d, 0.0d};
			for (Integer i : contE.keySet()) {
				nonGeomSumCountE[0] += contE.get(i)[0];
				nonGeomSumCountE[1] += contE.get(i)[1];
//				this.sumContE += Library_Quantif.moyGeom(contE.get(i)[0], contE.get(i)[1]);
			}
			this.sumContE = Library_Quantif.moyGeom(nonGeomSumCountE[0], nonGeomSumCountE[1]);

			Double[] nonGeomSumCountL = new Double[] {0.0d, 0.0d};
			for (Integer i : contL.keySet()) {
				nonGeomSumCountL[0] += contE.get(i)[0];
				nonGeomSumCountL[1] += contE.get(i)[1];
//				this.sumContL += Library_Quantif.moyGeom(contL.get(i)[0], contL.get(i)[1]);
			}
			this.sumContL = Library_Quantif.moyGeom(nonGeomSumCountL[0], nonGeomSumCountL[1]);
			
			

			// calcul heart/whole body
			this.hwb = (this.fixCoeurL)
					/ (this.totLate - (this.fixReinDL + this.fixReinGL + this.fixVessieL + this.sumContL));

			// calcul des retentions
			if (this.deuxPrises) {

				/* Valeurs calcul�es */
				// valeurs finales
				long timeEarly = Library_Dicom.getDateAcquisition(this.selectedImages[0].getImagePlus()).getTime();
				long timeLate = Library_Dicom.getDateAcquisition(this.selectedImages[1].getImagePlus()).getTime();
				
				int delaySeconds = (int) (timeEarly - timeLate);
				
				
				double wholeBodyCountEarly = this.totEarly - this.sumContE;
				
				double wholeBodyCountLate = this.totLate - sumContL;
				
				double summ = (wholeBodyCountLate - this.fixReinDL - this.fixVessieL);
				
				double earlyDecayed = Library_Quantif.applyDecayFraction(delaySeconds, wholeBodyCountEarly,
						Library_Quantif.Isotope.TECHNETIUM_99);
				
				this.wholeBodyRetention = summ / earlyDecayed;
				
				this.heartRetention = fixCoeurL / earlyDecayed;
				
				this.heartToWholeBody = fixCoeurL/wholeBodyCountLate;

				

				

				this.retCardiaque = Library_Quantif.applyDecayFraction(delaySeconds, this.fixCoeurL,
						Library_Quantif.Isotope.TECHNETIUM_99) / wholeBodyCountEarly;

				double sum = this.fixReinDL + this.fixVessieL + this.sumContL;
				this.retCe = (this.totLate - Library_Quantif.applyDecayFraction(delaySeconds, sum,
						Library_Quantif.Isotope.TECHNETIUM_99)) / wholeBodyCountEarly;
			}

			this.heartToContralateral = this.data.get("Heart A")[1] / this.data.get("Bkg noise A")[1];

		}
		if (this.onlyThoaxImage != 0) {
			// calculation of corrected heart uptake
			Double heartThoraxAnt = this.data.get("Heart Thorax A")[1];

			Double contralateralThoraxAnt = this.data.get("CL Thorax A")[1];

			// Calcul heart/ bg heart
			this.heartThorax = heartThoraxAnt;
			this.contralateralThorax = contralateralThoraxAnt;
			this.heartToContralateralThorax = this.heartThorax / this.contralateralThorax;
		}

	}

	// renvoie la moyenne geometrique de la vue ant et post de la slice courante
	private Double getGlobalCountAvg() {
		this.selectedImages[0].getImagePlus().setRoi(0, 0, this.selectedImages[0].getImagePlus().getWidth() / 2,
				this.selectedImages[0].getImagePlus().getHeight());
		Double countAnt = Library_Quantif.getCounts(this.selectedImages[0].getImagePlus());

		this.selectedImages[0].getImagePlus().setRoi(this.selectedImages[0].getImagePlus().getWidth() / 2, 0,
				this.selectedImages[0].getImagePlus().getWidth() / 2,
				this.selectedImages[0].getImagePlus().getHeight());
		Double countPost = Library_Quantif.getCounts(this.selectedImages[0].getImagePlus());

		return Library_Quantif.moyGeom(countAnt, countPost);
	}

	public void calculerMoyGeomTotale() {
		this.selectedImages[0].getImagePlus();
		if (this.deuxPrises) {
			this.totEarly = getGlobalCountAvg();
			this.selectedImages[1].getImagePlus();
			this.totLate = getGlobalCountAvg();
		} else {
			this.totLate = getGlobalCountAvg();
		}

		this.selectedImages[0].getImagePlus().killRoi();
		this.selectedImages[0].getImagePlus().setSlice(1);
	}

	public void setDeuxPrise(Boolean b) {
		this.deuxPrises = b;
	}

	@Override
	public String toString() {
		String s = "";

		if (this.fullBodyImages != 0) {
			s += "Heart," + Library_Quantif.round(this.fixCoeurL, 2) + "\n";
			s += "Left Kidney," + Library_Quantif.round(this.fixReinGL, 2) + "\n";
			s += "Right Kidney," + Library_Quantif.round(this.fixReinDL, 2) + "\n";
			if (this.deuxPrises)
				s += "WB early (5mn)," + Library_Quantif.round(this.totEarly, 2) + "\n";
			s += "WB late (3h)," + Library_Quantif.round(this.totLate, 2) + "\n";
			s += "Bladder," + Library_Quantif.round(this.fixVessieL, 2) + "\n";
			s += "Bkg Ant (mean)," + Library_Quantif.round(this.fixBkgNoiseA, 2) + "\n";
			s += "Bkg Post (mean)," + Library_Quantif.round(this.fixBkgNoiseP, 2) + "\n";
			s += "Ratio H/WB %," + Library_Quantif.round(this.hwb, 2) + "\n";
			if (this.deuxPrises) {
				s += "Cardiac retention %," + Library_Quantif.round(this.retCardiaque * 100, 2) + "\n";
				s += "WB retention %," + Library_Quantif.round(this.retCe * 100, 2) + "\n";

			}
		}

		return s;
	}

	public HashMap<String, String> getResultsHashMap() {
		if (this.deuxPrises) {
			this.resultats.put("WB early (5mn)", "" + Library_Quantif.round(this.totEarly, 2));
			this.resultats.put("Cardiac retention %", "" + Library_Quantif.round(this.retCardiaque * 100, 2));
			this.resultats.put("WB retention %", "" + Library_Quantif.round(this.retCe * 100, 2));
		}

		this.resultats.put("WB late (3h)", "" + Library_Quantif.round(this.totLate, 2));
		this.resultats.put("Bladder", "" + Library_Quantif.round(this.fixVessieL, 2));
		this.resultats.put("Heart", "" + Library_Quantif.round(this.fixCoeurL, 2));
		this.resultats.put("Bkg noise A", "" + Library_Quantif.round(this.fixBkgNoiseA, 2));
		this.resultats.put("Bkg noise P", "" + Library_Quantif.round(this.fixBkgNoiseP, 2));
		this.resultats.put("Right Kidney", "" + Library_Quantif.round(this.fixReinDL, 2));
		this.resultats.put("Left Kidney", "" + Library_Quantif.round(this.fixReinGL, 2));

		this.resultats.put("Ratio H/WB %", "" + Library_Quantif.round(this.hwb * 100, 2));

		this.resultats.put("Heart to contralateral", "" + Library_Quantif.round(this.heartToContralateral, 2));
		
		this.resultats.put("wholeBodyRetention", "" + Library_Quantif.round(this.wholeBodyRetention * 100, 2));
		this.resultats.put("heartRetention", "" + Library_Quantif.round(this.heartToContralateral * 100, 2));
		this.resultats.put("heartToWholeBody", "" + Library_Quantif.round(this.heartToWholeBody * 100, 2));

		return this.resultats;
	}

	public ImageSelection getImageVisualGradation() {
		// TODO Auto-generated method stub
		return this.imageVisualGradation;
	}

	public void setImageVisualGradation(ImageSelection imageVisualGradation) {
		// TODO Auto-generated method stub
		this.imageVisualGradation = imageVisualGradation;
	}

	public void getResultsVisualGradation() {
		// TODO Auto-generated method stub

		ImagePlus selectedImage = this.getImageVisualGradation().getImagePlus();

		for (Roi roi : this.getRoiManager().getRoisAsArray()) {

			selectedImage.setRoi((Roi) roi.clone());

			// Array of Double, in 0 raw count, in 1 average count, in 2 number of pixels
			Double[] counts = new Double[3];
			counts[0] = Library_Quantif.getCounts(selectedImage);
			counts[1] = Library_Quantif.getAvgCounts(selectedImage);
			counts[2] = (double) Library_Quantif.getPixelNumber(selectedImage);

			this.dataVisualGradation.put(roi.getName(), counts);
//			System.out.println("this.dataVisualGradation.put(" + roi.getName() + "[ " + counts[0] + ", " + counts[1]
//					+ ", " + counts[2] + "])");

		}

	}

	public void calculateResultsVisualGradation() {
		// TODO Auto-generated method stub

	}

	public HashMap<String, String> getResultsVisualGradationHashMap() {

		this.resultatsVisualGradation.put("Heart", "" + Library_Quantif.round(this.heartThorax, 2));

		this.resultatsVisualGradation.put("Heart to contralateral",
				"" + Library_Quantif.round(this.heartToContralateralThorax, 2));

		return this.resultatsVisualGradation;
	}

	public void setFullBodyImages(int fullBodyImages) {
		// TODO Auto-generated method stub
		this.fullBodyImages = fullBodyImages;
	}

	public void setOnlyThoraxImage(int onlyThoraxImage) {
		// TODO Auto-generated method stub
		this.onlyThoaxImage = onlyThoraxImage;
	}

	// public void setNbConta(int[] is) {
	// this.nbConta = is;
	// }

}
