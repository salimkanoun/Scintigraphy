package org.petctviewer.scintigraphy.lympho.post;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.lympho.gui.TabPost;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.MontageMaker;

public class ControllerWorkflowPelvis extends ControllerWorkflow {

	private final int NBORGAN = 3;

	private List<ImagePlus> captures;

	private TabResult resultTab;

	public ControllerWorkflowPelvis(Scintigraphy main, FenApplication vue, ModeleScin model, TabResult resultTab) {
		super(main, vue, model);

		this.resultTab = resultTab;
	}

	@Override
	protected void generateInstructions() {
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null;
		ScreenShotInstruction dri_capture = null;
		this.captures = new ArrayList<>();

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.model.getImageSelection()[i]);
			
			ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
			ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);
			
			dri_1 = new DrawRoiInstruction("Right Pelvis", stateAnt, null);
			dri_2 = new DrawRoiInstruction("Left Pelvis", stateAnt, null);
			dri_3 = new DrawRoiInstruction("Background", stateAnt, null);
			dri_capture = new ScreenShotInstruction(captures, this.getVue(), i);
			dri_4 = new DrawRoiInstruction("Right Pelvis", statePost, dri_1);
			dri_5 = new DrawRoiInstruction("Left Pelvis", statePost, dri_2);
			dri_6 = new DrawRoiInstruction("Background", statePost, dri_3);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_capture);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(dri_5);
			this.workflows[i].addInstruction(dri_6);
			this.workflows[i].addInstruction(dri_capture);

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

			if (i < NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(firstSlice);

			} else if (i < 2 * NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(secondSlice);
			}

			img.setRoi(r);
			((ModelePost) this.model).calculerCoups(organ, img);
			organ++;

		}
		this.model.calculerResultats();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV
				.captureToStack(this.captures.toArray(new ImagePlus[this.captures.size()]));
		ImagePlus montage = this.montage2Images(stackCapture);

		((ModelePost) this.model).setPelvisMontage(montage);
		((TabPost) this.resultTab).setExamDone(true);
		((TabPost) this.resultTab).reloadDisplay();

		this.vue.close();

	}

	/**
	 * Creates an ImagePlus with 2 captures.
	 * 
	 * @param captures
	 *            ImageStack with 2 captures
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

	public ModeleScin getModel() {
		return this.model;
	}

}
