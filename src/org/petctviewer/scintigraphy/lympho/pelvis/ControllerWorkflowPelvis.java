package org.petctviewer.scintigraphy.lympho.pelvis;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.lympho.ModelLympho;
import org.petctviewer.scintigraphy.lympho.gui.TabPelvis;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.MontageMaker;

public class ControllerWorkflowPelvis extends ControllerWorkflow {

	private List<ImagePlus> captures;

	private final TabResult resultTab;

	public ControllerWorkflowPelvis(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model,
			TabResult resultTab) {
		super(main, vue, model);

		this.resultTab = resultTab;

		this.generateInstructions();
		this.start();
		this.indexRoi = ((ModelLympho)this.getModel()).getNbRoiLympho();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[1];
		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_5, dri_6;
		ScreenShotInstruction dri_capture_1, dri_capture_2;
		this.captures = new ArrayList<>();
		
//		this.position = ((ModelLympho)this.getModel()).getNbRoiLympho();
		
		System.out.println("indexRoi : "+indexRoi);

		
		this.workflows[0] = new Workflow(this, ((ModelLympho) this.getModel()).getImagePelvis());

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_WORKFLOW);
		ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_WORKFLOW);

		dri_1 = new DrawRoiInstruction("Right Pelvis", stateAnt);
		dri_2 = new DrawRoiInstruction("Left Pelvis", stateAnt);
		dri_3 = new DrawRoiInstruction("Background", stateAnt);
		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);
		dri_4 = new DrawRoiInstruction("Right Pelvis", statePost, dri_1);
		dri_5 = new DrawRoiInstruction("Left Pelvis", statePost, dri_2);
		dri_6 = new DrawRoiInstruction("Background", statePost, dri_3);
		dri_capture_2 = new ScreenShotInstruction(captures, this.getVue(), 1);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_capture_1);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(dri_5);
		this.workflows[0].addInstruction(dri_6);
		this.workflows[0].addInstruction(dri_capture_2);

		
		this.workflows[0].addInstruction(new EndInstruction());
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		int firstSlice = 1;
		int secondSlice = 2;
		ImagePlus img = ((ModelLympho)this.getModel()).getImagePelvis().getImagePlus();
		img.setSlice(firstSlice);
		int organ = 0;
		for (int i = 0 ; i < this.model.getRoiManager().getRoisAsArray().length - ((ModelLympho) this.getModel()).getNbRoiLympho(); i++) {

			Roi roi = this.model.getRoiManager().getRoisAsArray()[i + ((ModelLympho) this.getModel()).getNbRoiLympho()];
			System.out.println("\t"+roi+" | "+roi.getName());

			int NBORGAN = 3;
			if (i < NBORGAN) {
				img.setSlice(firstSlice);

			} else if (i < 2 * NBORGAN) {
				img.setSlice(secondSlice);
			}

			img.setRoi(roi);
			((ModelLympho) this.model).calculerCoupsPelvis(organ, img);
			organ++;

		}
		((ModelLympho)this.model).calculateResultsPelvis();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV
				.captureToStack(this.captures.toArray(new ImagePlus[0]));
		ImagePlus montage = this.montage2Images(stackCapture);

		((ModelLympho) this.model).setPelvisMontage(montage);
		((TabPelvis) this.resultTab).setExamDone(true);
		this.resultTab.reloadDisplay();

		this.vue.close();

	}

	/**
	 * Creates an ImagePlus with 2 captures.
	 * 
	 * @param captures ImageStack with 2 captures
	 * @return ImagePlus with the 2 captures on 1 slice
	 */
	private ImagePlus montage2Images(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Resultats Pelvis -" + this.model.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 1, 2, 0.50, 1, 2, 1, 10, false);
		return imp;
	}

	public ModelScin getModel() {
		return this.model;
	}

}
