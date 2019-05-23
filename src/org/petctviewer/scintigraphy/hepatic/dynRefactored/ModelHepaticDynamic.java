package org.petctviewer.scintigraphy.hepatic.dynRefactored;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ControllerWorkflowHepaticDyn;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabCurves;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.ImagePlus;

public class ModelHepaticDynamic extends ModelScinDyn {

	private TabResult resutlTab;

	private int[] frames;

	private int[] times;

	private ImagePlus[] captures;

	private ImageSelection[] impSecondMethod;

	int indexRoi;

	int nbOrganes;

	boolean examDone;

	private List<Double> results;

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

		int frameDuration[] = this.getFrameduration();

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

		retour[0] = "Delay before : ";
		retour[1] = "\t Hilium (frame n°" + this.frames[0] + ") : " + (this.times[0] / 1000 / 60) + " min "
				+ ((this.times[0] / 1000) - ((this.times[0] / 1000 / 60) * 60)) + " sec";
		retour[2] = "\t Duodenum (frame n°" + this.frames[1] + ") : " + (this.times[1] / 1000 / 60) + " min "
				+ ((this.times[1] / 1000) - ((this.times[1] / 1000 / 60) * 60)) + " sec";
		retour[3] = "\t Intestine (frame n°" + this.frames[2] + ") : " + (this.times[2] / 1000 / 60) + " min "
				+ ((this.times[2] / 1000) - ((this.times[2] / 1000 / 60) * 60)) + " sec ";

		retour[4] = "";

		retour[5] = "Delay difference : ";
		retour[6] = "\t Hilium to Duodenum : " + (this.times[3] / 1000 / 60) + " min & "
				+ ((this.times[3] / 1000) - ((this.times[3] / 1000 / 60) * 60)) + " sec";
		retour[7] = "\t Duodenum to Intestine : " + (this.times[4] / 1000 / 60) + " min & "
				+ ((this.times[4] / 1000) - ((this.times[4] / 1000 / 60) * 60)) + " sec";
		retour[8] = "\t Hilium to Intestine : " + (this.times[5] / 1000 / 60) + " min & "
				+ ((this.times[5] / 1000) - ((this.times[5] / 1000 / 60) * 60)) + " sec";

		this.results.add((double) this.frames[0]);
		this.results.add((double) this.times[0]);
		this.results.add((double) this.frames[1]);
		this.results.add((double) this.times[1]);
		this.results.add((double) this.frames[2]);
		this.results.add((double) this.times[2]);
		this.results.add((double) this.times[3]);
		this.results.add((double) this.times[4]);
		this.results.add((double) this.times[5]);

		return retour;
	}

	public void setResultTab(TabResult resultTab) {
		this.resutlTab = resultTab;
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

		s += "\n\nInjection Ratio Left/Right," + this.results.get(0) + "\n\n";

		s += ",Hilium,Duodenum,Intestine\n";
		s += "Delay before," + this.results.get(1) + "," + results.get(3) + "," + results.get(5) + "\n\n";
		s += "Frame," + this.results.get(0) + "," + results.get(2) + "," + results.get(4) + "\n\n";

		s += ",Hilium to Duodenum,Duodenum to Intestine,Hilium to Intestine\n";
		s += "Delay difference," + this.results.get(6) + "," + results.get(7) + "," + results.get(8) + "\n\n";

		if (examDone)
			s += ((ModelSecondMethodHepaticDynamic) ((ControllerWorkflowHepaticDyn) ((TabCurves) this.resutlTab)
					.getFenApplication().getControleur()).getModel()).toCSV();

		s += super.toString();

		return s;
	}

	@Override
	public void calculateResults() {
		// TODO Auto-generated method stub

	}

}
