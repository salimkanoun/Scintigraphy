package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.util.Arrays;

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
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

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
		ImageSelection ims = this.model.getImageSelection()[0];

		// Remove point 0
		getModel().deactivateTime0();
		ImageSelection[] timeOrderedSelection = Arrays.copyOf(getModel().getImageSelection(),
				getModel().getImageSelection().length);
		Arrays.parallelSort(timeOrderedSelection, new ChronologicalAcquisitionComparator());
		getModel().setTimeIngestion(Library_Dicom.getDateAcquisition(timeOrderedSelection[0].getImagePlus()));

		final int NB_ROI_PER_IMAGE = 3;

		ImageState previousState = null;
		for (int image = 0; image < getModel().getImageSelection().length; image++) {
			ims = this.model.getImageSelection()[image];

			ImageState state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, image);
			// - Stomach
			Model_Gastric.REGION_STOMACH.inflate(state,
					this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE]);
			getModel().calculateCounts(Model_Gastric.REGION_STOMACH);

			// - Intestine (value)
			ims.getImagePlus().setRoi(this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE + 1]);
			ims.getImagePlus().setSlice(state.getSlice());
			double intestineValue = Library_Quantif.getCounts(ims.getImagePlus());

			// - Antre
			Model_Gastric.REGION_ANTRE.inflate(state,
					this.getRoiManager().getRoisAsArray()[image * NB_ROI_PER_IMAGE + 2]);
			getModel().calculateCounts(Model_Gastric.REGION_ANTRE);

			// - Fundus
			Model_Gastric.REGION_FUNDUS.inflate(state, null);
			getModel().forceCountsDataValue(Model_Gastric.REGION_FUNDUS,
					getModel().getCounts(Model_Gastric.REGION_STOMACH, Orientation.ANT)
							- getModel().getCounts(Model_Gastric.REGION_ANTRE, Orientation.ANT));

			// - Intestine
			Model_Gastric.REGION_INTESTINE.inflate(state, null);
			getModel().forceCountsDataValue(Model_Gastric.REGION_INTESTINE,
					intestineValue - getModel().getCounts(Model_Gastric.REGION_ANTRE, Orientation.ANT));

			// The numActualImage is reversed because the images are in reversed order
			getModel().computeDynamicData(state, previousState, getModel().getImageSelection().length - image,
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

		DrawRoiInstruction dri_1 = null, dri_2 = null;

		PromptBkgNoise promptBkgNoise = new PromptBkgNoise(this);

		for (int i = 0; i < getModel().getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, getModel().getImageSelection()[i]);

			ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state.specifieImage(this.workflows[i].getImageAssociated());

			dri_1 = new DrawRoiInstruction("Stomach", state, dri_1);
			dri_2 = new DrawRoiInstruction("Intestine", state, dri_2);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2, "Antre"));
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
