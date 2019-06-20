package org.petctviewer.scintigraphy.lympho;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.lympho.gui.FenResultatsLympho;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;
import ij.gui.Roi;

public class ControllerWorkflowLympho extends ControllerWorkflow {

	private List<ImagePlus> captures;

	public ControllerWorkflowLympho(FenApplicationWorkflow vue, ModelScin model) {
		super(vue, model);
		
		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		
		this.workflows = new Workflow[this.model.getImageSelection().length];
		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4;
		ScreenShotInstruction dri_capture_1, dri_capture_2;
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
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());
	}

	@Override
	public void end() {
		super.end();
//		this.saveWorkflow();
		// Compute model
		int firstSlice = 1;
		int secondSlice = 2;
		ImagePlus img;
		this.model.getImageSelection()[0].getImagePlus().setSlice(firstSlice);
		this.model.getImageSelection()[1].getImagePlus().setSlice(firstSlice);
		int organ = 0;
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];

			int NBORGAN = 2;
			if (i < NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(firstSlice);

			} else if (i < 2 * NBORGAN) {
				img = this.model.getImageSelection()[0].getImagePlus();
				img.setSlice(secondSlice);
			} else if (i < 3 * NBORGAN) {
				img = this.model.getImageSelection()[1].getImagePlus();
				img.setSlice(firstSlice);
			} else {
				img = this.model.getImageSelection()[1].getImagePlus();
				img.setSlice(secondSlice);

			}

			img.setRoi(r);
			((ModelLympho) this.model).calculerCoups(organ, img);
			organ++;

		}
		this.model.calculateResults();
		FenResults fenResults = new FenResultatsLympho(this, captures.toArray(new ImagePlus[0]));
		fenResults.setVisible(true);
		
		((ModelLympho)this.getModel()).setNbRoiLympho(this.getModel().getRoiManager().getCount());

	}

}
