package org.petctviewer.scintigraphy.hepatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.ImagePlus;
import ij.gui.Roi;

public class ModelHepaticDynamic extends ModelScinDyn {


	private int[] frames;

	private final int[] times;

	private final ImagePlus[] captures;

	private ImageSelection[] impSecondMethod;

	int indexRoi;

	int nbOrganes;

	boolean examDone;

	private final List<Double> results;
	
	
	
	
	
	private Double tDemiFoieDFit, tDemiFoieGFit, tDemiVascFit, tDemiFoieDObs, tDemiFoieGObs, tDemiVascObs;
	private Double maxFoieD, maxFoieG, finPicD, finPicG, pctVasc;

	
	// This organ names and order shall be the same  that the controller organs
	private final String[] organNames = {"Right Liver", "Left Liver", "Hilium", "CBD", "Duodenom", "Blood pool"};

	int indexRoiSecondExam;

	int nbOrganesSecondExam;

	ImagePlus capture;

	public ModelHepaticDynamic(ImageSelection[] selectedImages, String studyName, int[] frameDuration) {
		super(selectedImages, studyName, frameDuration);
		this.frames = new int[3];
		this.times = new int[6];
		this.captures = new ImagePlus[4];
		this.results = new ArrayList<>();
	}

	public void setTimes(int label1, int label2, int label3) {
		this.frames[0] = label1;
		this.frames[1] = label2;
		this.frames[2] = label3;

		int[] frameDuration = this.getFrameDuration();

		int time1 = 0;
		int time2 = 0;
		int time3 = 0;
		for (int i = 0; i < label1 - 1; i++)
			time1 += frameDuration[i];

		for (int i = 0; i < label2 - 1; i++)
			time2 += frameDuration[i];

		for (int i = 0; i < label3 - 1; i++)
			time3 += frameDuration[i];

		this.times[0] = time1;
		this.times[1] = time2;
		this.times[2] = time3;
		this.times[3] = time2 - time1;
		this.times[4] = time3 - time2;
		this.times[5] = time3 - time1;
	}

	public int[] getFrames() {
		return this.frames;
	}

	public void setCapture(ImagePlus imp, int i) {
		this.captures[i] = Library_Capture_CSV.captureImage(imp, 512, 0);
	}

	public void setCapture(ImagePlus imp, int i, int captureWidht) {
		this.captures[i] = Library_Capture_CSV.captureImage(imp, captureWidht, 0);
	}

	public void setCapture(ImagePlus imp, int i, int captureWidht, int captureHeight) {
		this.captures[i] = Library_Capture_CSV.captureImage(imp, captureWidht, captureHeight);
	}

	public ImagePlus[] getCaptures() {
		return this.captures;
	}

	public String[] getResult() {
		String[] retour = new String[9];

		retour[0] = "Appearance time : ";
		retour[1] = "\t Hilium (frame :" + this.frames[0] + ") : " + Math.round(((double)this.times[0] / 1000.0d / 60.0d)) + " min";
		retour[2] = "\t Duodenum (frame :" + this.frames[1] + ") : " + Math.round(((double)this.times[1] / 1000.0d / 60.0d)) + " min";
		retour[3] = "\t Intestine (frame :" + this.frames[2] + ") : " + Math.round(((double)this.times[2] / 1000.0d / 60.0d)) + " min";

		retour[4] = "";

		retour[5] = "Delay difference : ";
		retour[6] = "\t Hilium to Duodenum : " + Math.round(((double)this.times[3] / 1000.0d / 60.0d)) + " min";
		retour[7] = "\t Duodenum to Intestine : " + Math.round(((double)this.times[4] / 1000.0d / 60.0d)) + " min";
		retour[8] = "\t Hilium to Intestine : " + Math.round(((double)this.times[5] / 1000.0d / 60.0d)) + " min";

		this.results.add((double) this.frames[0]);
		this.results.add((double) Math.round(((double)this.times[0] / 1000.0d / 60.0d)));
		this.results.add((double) this.frames[1]);
		this.results.add((double) Math.round(((double)this.times[1] / 1000.0d / 60.0d)));
		this.results.add((double) this.frames[2]);
		this.results.add((double) Math.round(((double)this.times[2] / 1000.0d / 60.0d)));
		this.results.add((double) Math.round(((double)this.times[3] / 1000.0d / 60.0d)));
		this.results.add((double) Math.round(((double)this.times[4] / 1000.0d / 60.0d)));
		this.results.add((double) Math.round(((double)this.times[5] / 1000.0d / 60.0d)));

		return retour;
	}
	
	public void setImpSecondMethod(ImageSelection[] selectedImages) {
		this.impSecondMethod = selectedImages;
	}

	public ImageSelection[] getImpSecondMethod() {
		return this.impSecondMethod;
	}

	public void setExamDone(boolean boobool) {
		this.examDone = boobool;
	}

	@Override
	public String toString() {
		String s = "";


		s += "Hilium,Duodenum,Intestine\n";
		s += "Appearance time," + this.results.get(1) + "," + results.get(3) + "," + results.get(5) + "\n\n";
		s += "Frame," + this.results.get(0) + "," + results.get(2) + "," + results.get(4) + "\n\n";

		s += "Hilium to Duodenum,Duodenum to Intestine,Hilium to Intestine\n";
		s += "Delay difference," + this.results.get(6) + "," + results.get(7) + "," + results.get(8) + "\n\n";

		if (examDone)
			s += this.toCSV();

		s += super.toString();

		return s;
	}

	@Override
	public void calculateResults() {
		
		for (String k : this.getData().keySet()) {
			List<Double> data = this.getData().get(k);
			this.getData().put(k, this.adjustValues(data));
		}
		

		XYSeries liverL = this.getSerie("Left Liver");
		XYSeries liverR = this.getSerie("Right Liver");
		XYSeries bloodPool = this.getSerie("Blood pool");

		this.maxFoieD = Library_JFreeChart.getAbsMaxY(liverR);
		// this.tDemiFoieDFit = Modele_HepaticDyn.getTDemiFit(liverR, (this.maxFoieD +
		// 2)*1.0);
		this.tDemiFoieDObs = Library_JFreeChart.getTDemiObs(liverR, this.maxFoieD + 2);
		this.finPicD = liverR.getY(liverR.getItemCount() - 1).doubleValue() / liverR.getMaxY();

		this.maxFoieG = Library_JFreeChart.getAbsMaxY(liverL);
		// this.tDemiFoieGFit = Modele_HepaticDyn.getTDemiFit(liverL, this.maxFoieG +
		// 2);
		this.tDemiFoieGObs = Library_JFreeChart.getTDemiObs(liverL, this.maxFoieG + 2);
		this.finPicG = liverL.getY(liverL.getItemCount() - 1).doubleValue() / liverL.getMaxY();

		this.pctVasc = Library_JFreeChart.getY(bloodPool, 20.0) / Library_JFreeChart.getY(bloodPool, 5.0);
		// this.tDemiVascFit = Modele_HepaticDyn.getTDemiFit(bloodPool, 20.0);
		this.tDemiVascObs = Library_JFreeChart.getTDemiObs(bloodPool, 20.0);

	}

	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> hm = new HashMap<>();

		// foie droit
		hm.put("T1/2 Righ Liver", Library_Quantif.round(this.tDemiFoieDObs, 1) + "mn");
		hm.put("T1/2 Righ Liver *", Library_Quantif.round(this.tDemiFoieDFit, 1) + "mn");
		hm.put("Maximum Right Liver", Library_Quantif.round(this.maxFoieD, 1) + "mn");
		hm.put("end/max Ratio Right", (int) (this.finPicD * 100) + "%");

		// foie gauche
		hm.put("T1/2 Left Liver", Library_Quantif.round(this.tDemiFoieGObs, 1) + "mn");
		hm.put("T1/2 Left Liver *", Library_Quantif.round(this.tDemiFoieGFit, 1) + "mn");
		hm.put("Maximum Left Liver", Library_Quantif.round(this.maxFoieG, 1) + "mn");
		hm.put("end/max Ratio Left", (int) (this.finPicG * 100) + "%");

		// vasculaire
		hm.put("Blood pool ratio 20mn/5mn", (int) (this.pctVasc * 100) + "%");
		hm.put("T1/2 Blood pool", Library_Quantif.round(this.tDemiVascObs, 1) + "mn");
		hm.put("T1/2 Blood pool *", Library_Quantif.round(this.tDemiVascFit, 1) + "mn");

		return hm;
	}

	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		// si le modele n'est pas bloque
		if (this.isUnlocked()) {
			// recupere la phrase sans le dernier mot (tag)

			String name = nomRoi.substring(nomRoi.lastIndexOf("_"), nomRoi.length() - 1);
			// on cree la liste si elle n'existe pas
			this.getData().computeIfAbsent(name, k -> new ArrayList<>());
			// on y ajoute le nombre de coups
			this.getData().get(name).add(Library_Quantif.getCounts(imp) != 0.0d ? Library_Quantif.getCounts(imp) : 1);
		}
	}

	public void saveValues() {
		ImagePlus imp = this.selectedImages[2].getImagePlus();
		this.indexRoiSecondExam = 0;
		this.nbOrganesSecondExam = this.getRoiManager().getCount();
		HashMap<String, List<Double>> mapData = new HashMap<>();
		// For every slice, we calculate the number of count
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (this.indexRoiSecondExam = 0; this.indexRoiSecondExam < this.nbOrganesSecondExam; this.indexRoiSecondExam++) {
				imp.setRoi(getOrganRoi(this.indexRoiSecondExam));
				// On récupère seulement le studyName de l'organe (usually 0_organNameA => 0 for the number of image, A for Ant or Post)
				String name = this.organNames[this.indexRoiSecondExam];
				// on cree la liste si elle n'existe pas
				mapData.computeIfAbsent(name, k -> new ArrayList<>());
				// on y ajoute le nombre de coups
				// For Hillium, we correct by Right Liver as Background
				if (name.equals("Hilium")) {
					// Apply the roi of the Right Liver, and save the count
					imp.setRoi(getOrganRoi(0));
					double averageCountLiverRight = Library_Quantif.getAvgCounts(imp);
					// Apply the roi of the Left Liver
					imp.setRoi(getOrganRoi(1));
					double averageCountLiverLeft = Library_Quantif.getAvgCounts(imp);
					double averageCountLiver = (averageCountLiverRight + averageCountLiverLeft) / 2;

					imp.setRoi(getOrganRoi(this.indexRoiSecondExam));
					double count = Library_Quantif.getCountCorrectedBackground(imp, getOrganRoi(this.indexRoiSecondExam),
							averageCountLiver);
					mapData.get(name).add(count != 0.0d ? count : 1);

				} else
					mapData.get(name).add(Library_Quantif.getCounts(imp) != 0.0d ? Library_Quantif.getCounts(imp) : 1);
			}
		}
		
		// Putting in data the count/pixel of Right Liver and Blood Pool, used in the deconvolution.
		String[] organAVGCount = { "Right Liver AVG", "Blood Pool AVG" };
		// Roi number of the Right Liver (0), and the Blood Pool (5), in the Roi Manager.
		int[] organRoiNumber = { 0, 5 };
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int orgAVG = 0; orgAVG < organAVGCount.length; orgAVG++) {
				imp.setRoi(getOrganRoi(organRoiNumber[orgAVG]));
				// String name = this.getNomOrgane(this.indexRoi);
				String name = organAVGCount[orgAVG];
				// on cree la liste si elle n'existe pas
				mapData.computeIfAbsent(name, k -> new ArrayList<>());
				mapData.get(name)
						.add(Library_Quantif.getAvgCounts(imp) != 0.0d ? Library_Quantif.getAvgCounts(imp) : 1);
			}
		}

		// set data to the model
		this.setData(mapData);
		this.calculateResults();

	}

	public Roi getOrganRoi(int lastRoi) {
		return this.getRoiManager().getRoi(lastRoi % this.nbOrganesSecondExam);
	}

	public String getNomOrgane(int index) {
		return this.getRoiManager().getRoi(index % this.nbOrganesSecondExam).getName();
	}

	public void setCapture(ImagePlus capture) {
		this.capture = capture;
	}

	public ImagePlus getCapture() {
		return this.capture;
	}

	public String toCSV() {
		XYSeries liverL = this.getSerie("Left Liver");
		XYSeries liverR = this.getSerie("Right Liver");
		XYSeries bloodPool = this.getSerie("Blood pool");

		StringBuilder s = new StringBuilder("\n\n");
		s.append("Time (mn),");
		for (int i = 0; i < bloodPool.getItemCount(); i++) {
			s.append(Library_Quantif.round(bloodPool.getX(i).doubleValue(), 2)).append(",");
		}
		s.append("\n");

		s.append("Blood Pool (counts/sec),");
		for (int i = 0; i < bloodPool.getItemCount(); i++) {
			s.append(Library_Quantif.round(bloodPool.getY(i).doubleValue(), 2)).append(",");
		}
		s.append("\n");

		s.append("Right Liver (counts/sec),");
		for (int i = 0; i < liverR.getItemCount(); i++) {
			s.append(Library_Quantif.round(liverR.getY(i).doubleValue(), 2)).append(",");
		}
		s.append("\n");

		s.append("Left Liver (counts/sec),");
		for (int i = 0; i < liverL.getItemCount(); i++) {
			s.append(Library_Quantif.round(liverL.getY(i).doubleValue(), 2)).append(",");
		}
		s.append("\n");

		s.append("T1/2 Right Liver Obs,").append(Library_Quantif.round(this.tDemiFoieDObs, 2)).append("mn").append("\n");
		s.append("T1/2 Right Liver Fit,").append(Library_Quantif.round(this.tDemiFoieDFit, 2)).append("mn").append("\n");
		s.append("Maximum Right Liver,").append(Library_Quantif.round(this.maxFoieD, 2)).append("mn").append("\n");
		s.append("END/MAX Ratio Right,").append((int) (this.finPicD * 100)).append("%").append("\n");

		s.append("T1/2 Left Liver Obs,").append(Library_Quantif.round(this.tDemiFoieGObs, 2)).append("mn").append("\n");
		s.append("T1/2 Left Liver Fit,").append(Library_Quantif.round(this.tDemiFoieGFit, 2)).append("mn").append("\n");
		s.append("Maximum Left Liver,").append(Library_Quantif.round(this.maxFoieG, 2)).append("mn").append("\n");
		s.append("END/MAX Ratio Left,").append((int) (this.finPicG * 100)).append("%").append("\n");

		s.append("Blood pool ratio 20mn/5mn,").append((int) (this.pctVasc * 100)).append("%").append("\n");
		s.append("T1/2 Blood pool Obs,").append(Library_Quantif.round(this.tDemiVascObs, 2)).append("mn").append("\n");
		s.append("T1/2 Blood pool Fit,").append(Library_Quantif.round(this.tDemiVascFit, 2)).append("mn").append("\n");

		return s.toString();
	}
	
	
	public void setFramesDuration(int[] framesDuration) {
		this.frames = framesDuration;
	}
	
	

}