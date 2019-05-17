package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.awt.event.ActionEvent;

import javax.swing.JButton;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabMainResult;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class ControllerWorkflow_DynGastric extends ControllerWorkflow {

	public static final String COMMAND_FIT_BEST_1 = "cfb_method1", COMMAND_FIT_BEST_2 = "cfb_method2", COMMAND_FIT_BEST_ALL = "cfb_all";

	private FenResults fenResults;

	public ControllerWorkflow_DynGastric(Scintigraphy main, FenApplication vue, ModeleScin model,
			ImageSelection[] selectedImages, FenResults fenResults) {
		super(main, vue, model);
		this.getRoiManager().reset();

		// Create projection for each image
		ImageSelection[] projections = new ImageSelection[selectedImages.length];
		for (int i = 0; i < selectedImages.length; i++)
			projections[i] = Library_Dicom.project(selectedImages[i], 1, 10, "sum");
		getModel().setImages(projections);

		// Set first image
		getModel().setFirstImage(selectedImages[selectedImages.length - 1]);

		this.fenResults = fenResults;

		this.generateInstructions();
		this.start();
	}

	private void computeModel() {
		// Remove point 0
		getModel().deactivateTime0();
		getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());

		// Set background noise for stomach
		/*
		 * The background is calculated with the first dynamic image (in chronological
		 * order).
		 */
		ImageState bkgState = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_CUSTOM_IMAGE);
		bkgState.specifieImage(getModel().getFirstImage());
		// Assumption: the order of the workflows is the same as the order of the images
		// (which is reverse chronological)
		Workflow workflowOfFirstImage = this.workflows[this.workflows.length - 1];
		// Assumption: the first instruction is dri_stomach
		Instruction instructionSelected = workflowOfFirstImage.getInstructionAt(0);

		getModel().setBkgNoise(Model_Gastric.REGION_STOMACH, bkgState,
				getRoiManager().getRoi(instructionSelected.roiToDisplay()));

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
		getModel().calculerResultats();
	}

	private void fitBest(String command) {
		TabMainResult tabMain = (TabMainResult) this.fenResults.getMainTab();
		if (command.equals(COMMAND_FIT_BEST_1))
			tabMain.selectFit(tabMain.findBestFit());
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Update results
		TabMainResult tabMain = (TabMainResult) this.fenResults.getMainTab();
		
		tabMain.displayTimeIngestion(getModel().getTimeIngestion());
		tabMain.createGraph();

		// Set the best fit by default
		this.fitBest(COMMAND_FIT_BEST_ALL);
		
		// Do not reload the method 2
		tabMain.reloadDisplay();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_antre = null, dri_intestine = null;

		PromptBkgNoise promptBkgNoise = new PromptBkgNoise(this, this.workflows.length);

		for (int i = 0; i < this.workflows.length; i++) {
			this.workflows[i] = new Workflow(this, getModel().getImageSelection()[i]);

			ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
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
	}

	@Override
	public Model_Gastric getModel() {
		return (Model_Gastric) super.getModel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		// Auto-fit
		if(e.getSource() instanceof JButton) {
			JButton source = (JButton) e.getSource();
			this.fitBest(source.getActionCommand());
		}
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
