package org.petctviewer.scintigraphy.gastric.dynamic;

import org.apache.commons.lang3.time.DateUtils;
import org.petctviewer.scintigraphy.gastric.ControllerWorkflow_Gastric;
import org.petctviewer.scintigraphy.gastric.Model_Gastric;
import org.petctviewer.scintigraphy.gastric.tabs.TabMethod1;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.util.Date;

public class ControllerWorkflow_DynGastric extends ControllerWorkflow {

	private final TabMethod1 tabResult;

	public ControllerWorkflow_DynGastric(FenApplicationWorkflow vue, ModelScin model,
										 ImageSelection[] selectedImages, TabMethod1 tabResult) {
		super(vue, model);
		this.getRoiManager().reset();

		// Create projection for each image
		ImageSelection[] projections = new ImageSelection[selectedImages.length];
		for (int i = 0; i < selectedImages.length; i++)
			projections[i] = Library_Dicom.project(selectedImages[i], 1, 10, "sum");
		getModel().setImages(projections);

		// Set first image
		getModel().setFirstImage(selectedImages[selectedImages.length - 1]);

		this.tabResult = tabResult;

		this.generateInstructions();
		this.start();
	}

	private void computeModel() {
		// Remove point 0
		getModel().deactivateTime0();
		getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());

		final int NB_ROI_PER_IMAGE = 3;
		ImageState previousState = null;
		for (int image = 0; image < getModel().getImageSelection().length; image++) {
			ImageState state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, image);
			// - Stomach
			getModel().calculateCounts(Model_Gastric.REGION_STOMACH, state,
					this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE]);

			// - Antre
			getModel().calculateCounts(Model_Gastric.REGION_ANTRE, state,
					this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE + 2]);

			// - Fundus
			getModel().calculateCounts(Model_Gastric.REGION_FUNDUS, state, null);

			// - Intestine
			getModel().calculateCounts(Model_Gastric.REGION_INTESTINE, state,
					this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE + 1]);

			// The numActualImage is reversed because the images are in reversed order
			getModel().computeDynamicData(state, previousState, getModel().getImageSelection().length - image,
					getModel().getImageSelection().length);
			previousState = state;
		}
		getModel().calculateResults();
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Update results
		this.tabResult.displayTimeIngestion(getModel().getTimeIngestion());
		this.tabResult.createGraph();

		// Set the best fit by default
		this.tabResult.selectBestFit();

		// Do not reload the method 2
		this.tabResult.reloadDisplay();

		this.tabResult.enableDynamicAcquisition(false);
		this.tabResult.getParent().requestFocus();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		((ControllerWorkflow_Gastric) this.tabResult.getParent().getController()).specifiedTimeIngestion =
				DateUtils.addMinutes(this.getModel().getFirstImage().getDateAcquisition(), -5);

		DrawRoiInstruction dri_antre = null, dri_intestine = null;

		PromptBkgNoise promptBkgNoise = new PromptBkgNoise(this, this.workflows.length);

		for (int i = 0; i < this.workflows.length; i++) {
			this.workflows[i] = new Workflow(this, getModel().getImageSelection()[i]);

			ImageState state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_CUSTOM_IMAGE);
			state.specifieImage(this.workflows[i].getImageAssociated());

			dri_antre = new DrawRoiInstruction("Stomach", state, dri_antre);
			dri_intestine = new DrawRoiInstruction("Intestine", state, dri_intestine);
			CheckIntersectionInstruction checkIntersection = new CheckIntersectionInstruction(this, dri_antre,
					dri_intestine, "Antre");

			this.workflows[i].addInstruction(dri_antre);
			this.workflows[i].addInstruction(dri_intestine);
			this.workflows[i].addInstruction(checkIntersection);
			this.workflows[i].addInstruction(new BkgNoiseInstruction(promptBkgNoise));
		}
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());
	}

	@Override
	public Model_Gastric getModel() {
		return (Model_Gastric) super.getModel();
	}

	private class BkgNoiseInstruction extends PromptInstruction {

		public BkgNoiseInstruction(PromptBkgNoise dialog) throws IllegalArgumentException {
			super(dialog);
		}

		@Override
		public boolean isExpectingUserInput() {
			if (!this.dialog.shouldBeDisplayed())
				return false;
			return super.isExpectingUserInput();
		}

		@Override
		public void afterNext(ControllerWorkflow controller) {
			PromptBkgNoise dialog = (PromptBkgNoise) this.dialog;

			if (dialog.shouldBeDisplayed()) {
				this.displayDialog(controller.getVue());

				// Inform model if this instruction got the background
				if (dialog.antreIsNowSelected()) {
					((Model_Gastric) controller.getModel()).setBkgNoise(Model_Gastric.REGION_ANTRE,
							controller.getCurrentImageState(),
							controller.getRoiManager().getRoi(controller.getIndexLastRoiSaved()));

					// Assumption: the order of the workflows is the same as the order of the images
					// (which is reverse chronological)
					Workflow workflowOfFirstImage = ControllerWorkflow_DynGastric.this.workflows[ControllerWorkflow_DynGastric.this.workflows.length - 1];
					// Assumption: the first instruction is dri_stomach
					Instruction instructionSelected = workflowOfFirstImage.getInstructionAt(0);

					getModel().setBkgNoise(Model_Gastric.REGION_STOMACH, controller.getCurrentImageState(),
							getRoiManager().getRoi(instructionSelected.getRoiIndex()));
				}
				if (dialog.intestineIsNowSelected()) {
					((Model_Gastric) controller.getModel()).setBkgNoise(Model_Gastric.REGION_INTESTINE,
							controller.getCurrentImageState(),
							controller.getRoiManager().getRoi(controller.getIndexLastRoiSaved() - 1));
				}

				ControllerWorkflow_DynGastric.this.skipInstruction();
			}
		}

		@Override
		public void afterPrevious(ControllerWorkflow controller) {
			if (dialog.shouldBeDisplayed()) {
				this.displayDialog(controller.getVue());
				ControllerWorkflow_DynGastric.this.skipInstruction();
			}
		}
	}

}
