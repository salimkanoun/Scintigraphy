package org.petctviewer.scintigraphy.lympho;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.lympho.gui.FenResultatsLympho;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

import ij.ImagePlus;
import ij.gui.Roi;

public class ControllerWorkflowLympho extends ControllerWorkflow {

	private final int NBORGAN = 2;

	private List<ImagePlus> captures;

	public ControllerWorkflowLympho(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
		
		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		
		this.workflows = new Workflow[this.model.getImageSelection().length];
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;
		ScreenShotInstruction dri_capture_1 = null, dri_capture_2 = null;
		this.captures = new ArrayList<>();

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.model.getImageSelection()[i]);
			
			ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
			ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);
			
			dri_1 = new DrawRoiInstruction("Right Foot", stateAnt);
			dri_2 = new DrawRoiInstruction("Left Foot", stateAnt);
			dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), i*2);
			dri_3 = new DrawRoiInstruction("Right Foot", statePost, dri_1);
			dri_4 = new DrawRoiInstruction("Left Foot", statePost, dri_2);
			dri_capture_2 = new ScreenShotInstruction(captures, this.getVue(), (i*2)+1);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(dri_capture_1);
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_4);
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
		this.model.getImageSelection()[1].getImagePlus().setSlice(firstSlice);
		int organ = 0;
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];

			if (i < this.NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(firstSlice);

			} else if (i < 2 * this.NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(secondSlice);
			} else if (i < 3 * this.NBORGAN) {
				img = this.model.getImageSelection()[1].getImagePlus();
				img.setSlice(firstSlice);
			} else {
				img = this.model.getImageSelection()[1].getImagePlus();
				img.setSlice(secondSlice);

			}

			img.setRoi(r);
			((ModeleLympho) this.model).calculerCoups(organ, img);
			organ++;

		}
		this.model.calculerResultats();
		System.out.println(captures != null);
		new FenResultatsLympho(this, captures.toArray(new ImagePlus[captures.size()]));

	}

}
