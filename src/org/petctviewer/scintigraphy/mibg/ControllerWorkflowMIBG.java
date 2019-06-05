package org.petctviewer.scintigraphy.mibg;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.mibg.tabResults.TabContrastMIBG;
import org.petctviewer.scintigraphy.mibg.tabResults.TabMainMIBG;
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

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflowMIBG extends ControllerWorkflow {

	private final List<ImagePlus> captures;

	public ControllerWorkflowMIBG(String studyName, FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(null, vue, new ModelMIBG(selectedImages, studyName));

		this.captures = new ArrayList<>();

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_heart = null, dri_mediastinum = null;

		for(int i = 0; i<2; i++) {
			this.workflows[i] = new Workflow(this, this.getModel().getImageSelection()[i]);


			ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state.specifieImage(this.getModel().getImageSelection()[i]);

			dri_heart = new DrawRoiInstruction("Heart", state, dri_heart);
			dri_mediastinum = new DrawRoiInstruction("Mediastinum", state, dri_mediastinum);

			this.workflows[i].addInstruction(dri_heart);
			this.workflows[i].addInstruction(dri_mediastinum);
			this.workflows[i].addInstruction(new ScreenShotInstruction(captures, this.getVue(), i));
		}
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());

	}

	@Override
	public void end() {
		super.end();

		this.model.calculateResults();

		FenResults fenResults = new FenResults(this);
		fenResults.setMainTab(new TabMainMIBG(fenResults, "Main", captures));
		fenResults.addTab(new TabContrastMIBG("Contrast", "timed", fenResults, captures));
		fenResults.setVisible(true);
	}

}
