package org.petctviewer.scintigraphy.mibg;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

import ij.ImagePlus;

public class ControllerWorkflowMIBG extends ControllerWorkflow {

	private final List<ImagePlus> captures;

	public ControllerWorkflowMIBG(String studyName, FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(null, vue, new ModelMIBG(selectedImages, studyName));
		// TODO Auto-generated constructor stub

		this.captures = new ArrayList<>();

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		// TODO Auto-generated method stub

		this.workflows = new Workflow[this.model.getImageSelection().length];
		this.workflows[0] = new Workflow(this, this.getModel().getImageSelection()[0]);
		this.workflows[1] = new Workflow(this, this.getModel().getImageSelection()[1]);

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;
		ScreenShotInstruction dri_capture_1 = null, dri_capture_2 = null;
		ImageState stateFirstImage = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
		stateFirstImage.specifieImage(this.getModel().getImageSelection()[0]);
		ImageState stateSecondImage = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
		stateSecondImage.specifieImage(this.getModel().getImageSelection()[1]);

		dri_1 = new DrawRoiInstruction("Heart", stateFirstImage);
		dri_2 = new DrawRoiInstruction("Mediastinum", stateFirstImage);
		dri_3 = new DrawRoiInstruction("Heart", stateSecondImage, dri_1);
		dri_4 = new DrawRoiInstruction("Mediastinum", stateSecondImage, dri_2);

		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);
		dri_capture_2 = new ScreenShotInstruction(captures, this.getVue(), 1);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_capture_1);
		this.workflows[1].addInstruction(dri_3);
		this.workflows[1].addInstruction(dri_4);
		this.workflows[1].addInstruction(dri_capture_2);

		this.workflows[1].addInstruction(new EndInstruction());

		// Update view
		this.getVue().setNbInstructions(this.allInputInstructions().size());

	}

	@Override
	public void end() {
		super.end();

		this.model.calculateResults();

		FenResults fenResults = new FenResultsMIBG(this, this.captures);
		fenResults.setVisible(true);
	}

}
