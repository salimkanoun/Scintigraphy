package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.util.Arrays;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Region;
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
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class ControllerWorkflow_DynGastric extends ControllerWorkflow {

	private FenResults fenResults;

	public ControllerWorkflow_DynGastric(Scintigraphy main, FenApplication vue, ModeleScin model,
			ImageSelection[] selectedImages, FenResults fenResults) {
		super(main, vue, model);
		this.getRoiManager().reset();
		this.model.setImages(selectedImages);

		this.fenResults = fenResults;

		this.generateInstructions();
		this.start();
	}

	private void computeModel() {
		// Remove point 0
		getModel().deactivateTime0();
		ImageSelection[] timeOrderedSelection = Arrays.copyOf(getModel().getImageSelection(),
				getModel().getImageSelection().length);
		Arrays.parallelSort(timeOrderedSelection, new ChronologicalAcquisitionComparator());
		getModel().setTimeIngestion(Library_Dicom.getDateAcquisition(timeOrderedSelection[0].getImagePlus()));

		/*
		 * TODO: this seems incorrect: this relies on the assumption that the first
		 * dynamic image (in chronological order) is only noise, which might be wrong
		 * depending on the image
		 */
		// Set background noise for stomach
		Region bkgNoise_stomach = new Region("Background Noise " + Model_Gastric.REGION_STOMACH);
		ImageState bkgState = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_CUSTOM_IMAGE);
		bkgState.specifieImage(timeOrderedSelection[0]);
		Workflow workflowOfFirstImage = this.getWorkflowAssociatedWithImage(timeOrderedSelection[0]);
		Instruction instructionSelected = workflowOfFirstImage.getInstructionAt(0);
		bkgNoise_stomach.inflate(bkgState, getRoiManager().getRoi(instructionSelected.roiToDisplay()));

		bkgState.getImage().getImagePlus().setRoi(getRoiManager().getRoi(
				this.getWorkflowAssociatedWithImage(timeOrderedSelection[0]).getInstructionAt(0).roiToDisplay()));
		getModel().setBkgNoise(bkgNoise_stomach);

		final int NB_ROI_PER_IMAGE = 3;
		ImageState previousState = null;
		for (int image = 0; image < getModel().getImageSelection().length; image++) {
			ImageState state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, image);
			// - Stomach
			Region regionStomach = new Region(Model_Gastric.REGION_STOMACH);
			regionStomach.inflate(state, this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE]);
			getModel().calculateCounts(regionStomach);

			// - Antre
			Region regionAntre = new Region(Model_Gastric.REGION_ANTRE);
			regionAntre.inflate(state, this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE + 2]);
			getModel().calculateCounts(regionAntre);

			// - Fundus
			Region regionFundus = new Region(Model_Gastric.REGION_FUNDUS);
			regionFundus.inflate(state, null);
			getModel().calculateCounts(regionFundus);

			// - Intestine
			Region regionIntestine = new Region(Model_Gastric.REGION_INTESTINE);
			regionIntestine.inflate(state, this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE + 1]);
			getModel().calculateCounts(regionIntestine);

			// The numActualImage is reversed because the images are in reversed order
			getModel().computeData(state, previousState, getModel().getImageSelection().length - image,
					getModel().getImageSelection().length);
			previousState = state;
		}
		getModel().calculerResultats();
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
		((TabMainResult) this.fenResults.getMainTab()).displayTimeIngestion(getModel().getTimeIngestion());
		fenResults.reloadAllTabs();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_antre = null, dri_intestine = null;

		PromptBkgNoise promptBkgNoise = new PromptBkgNoise(this);

		for (int i = 0; i < getModel().getImageSelection().length; i++) {
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
		this.workflows[getModel().getImageSelection().length - 1].addInstruction(new EndInstruction());
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
			PromptBkgNoise dialog = (PromptBkgNoise) this.dialog;
			if (dialog.shouldBeDisplayed())
				dialog.setVisible(true);
			
			/*
			 * Trouver un moyen de récupérer la ROI créée par la DRI de l'estomac
			 */

			// Inform model if this instruction got the background
			if (dialog.antreIsNowSelected()) {
				Region bkgNoiseAntre = new Region("Background Noise " + Model_Gastric.REGION_ANTRE);
				System.out.println("State for ANTRE: " + controller.getCurrentImageState());
				bkgNoiseAntre.inflate(controller.getCurrentImageState(),
						controller.getRoiManager().getRoi(workflows[indexCurrentImage].getCurrentInstruction()));
				((Model_Gastric) controller.getModel()).setBkgNoise(bkgNoiseAntre);
			}
			if (dialog.intestineIsNowSelected()) {
				Region bkgNoiseIntestine = new Region("Background Noise " + Model_Gastric.REGION_INTESTINE);
				System.out.println("State for INTESTINE: " + controller.getCurrentImageState());
				bkgNoiseIntestine.inflate(controller.getCurrentImageState(),
						controller.getRoiManager().getRoi(dri_intestine.roiToDisplay()));
				((Model_Gastric) controller.getModel()).setBkgNoise(bkgNoiseIntestine);
			}
		}

		@Override
		public void afterPrevious(ControllerWorkflow controller) {
			if (((PromptBkgNoise) this.dialog).shouldBeDisplayed())
				super.afterPrevious(controller);
		}
	}

}
