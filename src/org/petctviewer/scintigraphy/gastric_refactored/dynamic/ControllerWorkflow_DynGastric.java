package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;

public class ControllerWorkflow_DynGastric extends ControllerWorkflow {

	public ControllerWorkflow_DynGastric(Scintigraphy main, FenApplication vue, ModeleScin model,
			ImageSelection[] selectedImages) {
		super(main, vue, model);
		this.getRoiManager().reset();
		this.model.setImages(selectedImages);

		this.generateInstructions();
		this.start();
	}
	
	private void computeModel() {
		ImageSelection ims = this.model.getImageSelection()[0];
		
		// Remove point 0
		getModel().deactivateTime0();
		
		ImageSelection previousImage = null;
		for(int i = 0; i<this.getRoiManager().getRoisAsArray().length; i += 6) {
			ims = this.model.getImageSelection()[i / 6];
			
			for(Orientation orientation : Orientation.antPostOrder()) {
				int indexIncrementPost = 0;
				int slice = SLICE_ANT;
				if(orientation == Orientation.POST) {
					indexIncrementPost = 3;
					slice = SLICE_POST;
				}
			}
		}
	}

	@Override
	public Model_Gastric getModel() {
		return (Model_Gastric) super.getModel();
	}

	@Override
	protected void end() {
		super.end();
		
		// Compute model
		this.computeModel();
		
		// Update results
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1 = null, dri_2 = null;

		PromptBkgNoise promptBkgNoise = new PromptBkgNoise(this);

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.model.getImageSelection()[i]);

			dri_1 = new DrawRoiInstruction("Stomach", new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE),
					dri_1);
			dri_2 = new DrawRoiInstruction("Intestine", new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE),
					dri_2);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2, "Antre"));
			this.workflows[i].addInstruction(new BkgNoiseInstruction(promptBkgNoise));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	private class BkgNoiseInstruction extends PromptInstruction {

		public BkgNoiseInstruction(PromptBkgNoise dialog) throws IllegalArgumentException {
			super(dialog);
		}

		@Override
		public boolean isExpectingUserInput() {
			if (!((PromptBkgNoise) this.dialog).shouldBeDisplayed())
				return false;
			return super.isExpectingUserInput();
		}

		@Override
		public void afterNext(ControllerWorkflow controller) {
			if (((PromptBkgNoise) this.dialog).shouldBeDisplayed())
				super.afterNext(controller);
		}

		@Override
		public void afterPrevious(ControllerWorkflow controller) {
			if (((PromptBkgNoise) this.dialog).shouldBeDisplayed())
				super.afterPrevious(controller);
		}
	}

}
