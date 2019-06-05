package org.petctviewer.scintigraphy.lympho.pelvis;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.MontageMaker;
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

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflowPelvis extends ControllerWorkflow {

	private List<ImagePlus> captures;

	private final TabResult resultTab;

	public ControllerWorkflowPelvis(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model,
			TabResult resultTab) {
		super(main, vue, model);

		this.resultTab = resultTab;

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];
		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_5, dri_6;
		ScreenShotInstruction dri_capture_1, dri_capture_2;
		this.captures = new ArrayList<>();

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.model.getImageSelection()[i]);

			ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
			ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);

			dri_1 = new DrawRoiInstruction("Right Pelvis", stateAnt);
			dri_2 = new DrawRoiInstruction("Left Pelvis", stateAnt);
			dri_3 = new DrawRoiInstruction("Background", stateAnt);
			dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), i);
			dri_4 = new DrawRoiInstruction("Right Pelvis", statePost, dri_1);
			dri_5 = new DrawRoiInstruction("Left Pelvis", statePost, dri_2);
			dri_6 = new DrawRoiInstruction("Background", statePost, dri_3);
			dri_capture_2 = new ScreenShotInstruction(captures, this.getVue(), (i * 2) + 1);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_capture_1);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(dri_5);
			this.workflows[i].addInstruction(dri_6);
			this.workflows[i].addInstruction(dri_capture_2);

		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		int firstSlice = 1;
		int secondSlice = 2;
		ImagePlus img = this.model.getImageSelection()[0].getImagePlus();
		this.model.getImageSelection()[0].getImagePlus().setSlice(firstSlice);
		int organ = 0;
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];

			int NBORGAN = 3;
			if (i < NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(firstSlice);

			} else if (i < 2 * NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(secondSlice);
			}

			img.setRoi(r);
			((ModelPelvis) this.model).calculerCoups(organ, img);
			organ++;

		}
		this.model.calculateResults();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV
				.captureToStack(this.captures.toArray(new ImagePlus[0]));
		ImagePlus montage = this.montage2Images(stackCapture);

		((ModelPelvis) this.model).setPelvisMontage(montage);
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
