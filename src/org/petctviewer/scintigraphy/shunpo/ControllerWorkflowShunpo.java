package org.petctviewer.scintigraphy.shunpo;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControllerWorkflowShunpo extends ControllerWorkflow {

	private FenResults fenResults;

	private List<ImagePlus> captures;

	private final boolean FIRST_ORIENTATION_POST;

	private static final int STEP_KIDNEY_LUNG = 0, STEP_BRAIN = 1;

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

	private final int[] NBORGANE = { 5, 1 };

	public ControllerWorkflowShunpo(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);

		this.generateInstructions();
		this.start();

		this.FIRST_ORIENTATION_POST = true;
		this.fenResults = new FenResults(this);
		this.fenResults.setVisible(false);
	}

	// TODO: refactor this method
	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null,
				dri_7 = null, dri_8 = null, dri_9 = null, dri_10 = null, dri_11 = null, dri_12 = null;
		ScreenShotInstruction dri_capture_1 = null, dri_capture_2, dri_capture_3, dri_capture_4;
		this.captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
		ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction("Right lung", statePost);
		dri_2 = new DrawRoiInstruction("Left lung", statePost);
		dri_3 = new DrawRoiInstruction("Right kidney", statePost);
		dri_4 = new DrawRoiInstruction("Left kidney", statePost);
		dri_5 = new DrawRoiInstruction("Background", statePost);

		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);
		dri_6 = new DrawRoiInstruction("Right lung", stateAnt, dri_1);
		dri_7 = new DrawRoiInstruction("Left lung", stateAnt, dri_2);
		dri_8 = new DrawRoiInstruction("Right kidney", stateAnt, dri_3);
		dri_9 = new DrawRoiInstruction("Left kidney", stateAnt, dri_4);
		dri_10 = new DrawRoiInstruction("Background", stateAnt, dri_5);
		dri_capture_2 = new ScreenShotInstruction(captures, this.getVue(), 1);
		dri_capture_3 = new ScreenShotInstruction(captures, this.getVue(), 2);
		dri_capture_4 = new ScreenShotInstruction(captures, this.getVue(), 3);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(dri_5);
		this.workflows[0].addInstruction(dri_capture_1);
		this.workflows[0].addInstruction(dri_6);
		this.workflows[0].addInstruction(dri_7);
		this.workflows[0].addInstruction(dri_8);
		this.workflows[0].addInstruction(dri_9);
		this.workflows[0].addInstruction(dri_10);
		this.workflows[0].addInstruction(dri_capture_2);

		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
		dri_11 = new DrawRoiInstruction("Brain", statePost);
		dri_12 = new DrawRoiInstruction("Brain", stateAnt, dri_11);

		this.workflows[1].addInstruction(dri_11);
		this.workflows[1].addInstruction(dri_capture_3);
		this.workflows[1].addInstruction(dri_12);
		this.workflows[1].addInstruction(dri_capture_4);

		this.workflows[1].addInstruction(new EndInstruction());
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		int firstSlice = (this.FIRST_ORIENTATION_POST ? SLICE_POST : SLICE_ANT);
		int secondSlice = firstSlice % 2 + 1;
		ImagePlus img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
		this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus().setSlice(firstSlice);
		this.model.getImageSelection()[STEP_BRAIN].getImagePlus().setSlice(firstSlice);
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {
			String title_completion = "";
			Roi r = this.model.getRoiManager().getRoisAsArray()[i];
			int organ = 0;

			if (i < this.NBORGANE[STEP_KIDNEY_LUNG]) {
				img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
				img.setSlice(firstSlice);
				title_completion += " {KIDNEY_LUNG}";
				if (this.FIRST_ORIENTATION_POST)
					organ = i * 2 + 1;
				else
					organ = i * 2;
			} else if (i < 2 * this.NBORGANE[STEP_KIDNEY_LUNG]) {
				img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
				img.setSlice(secondSlice);
				title_completion += " {KIDNEY_LUNG}";
				if (this.FIRST_ORIENTATION_POST)
					organ = (i - this.NBORGANE[STEP_KIDNEY_LUNG]) * 2;
				else
					organ = (i - this.NBORGANE[STEP_KIDNEY_LUNG]) * 2 + 1;
			} else if (i - 2 * this.NBORGANE[STEP_KIDNEY_LUNG] < this.NBORGANE[STEP_BRAIN]) {
				img = this.model.getImageSelection()[STEP_BRAIN].getImagePlus();
				img.setSlice(firstSlice);
				title_completion += " {BRAIN}";
				if (this.FIRST_ORIENTATION_POST)
					organ = i + 1;
				else
					organ = i;
			} else {
				img = this.model.getImageSelection()[STEP_BRAIN].getImagePlus();
				img.setSlice(secondSlice);
				title_completion += " {BRAIN}";
				if (this.FIRST_ORIENTATION_POST)
					organ = i - 1;
				else
					organ = i;
			}

			if (img.getCurrentSlice() == SLICE_ANT)
				title_completion += " ANT";
			else
				title_completion += " POST";

			System.out.println("Oppening:: " + r.getName() + title_completion);
			img.setRoi(r);
			((ModeleShunpo) this.model).calculerCoups(organ, img);
		}
		this.model.calculerResultats();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV
				.captureToStack(this.captures.toArray(new ImagePlus[this.captures.size()]));
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		this.fenResults.setMainTab(new MainResult(this.fenResults, montage));
		this.fenResults.pack();
		this.fenResults.setVisible(true);

	}

}
