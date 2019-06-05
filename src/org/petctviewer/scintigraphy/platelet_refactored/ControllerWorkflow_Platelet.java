package org.petctviewer.scintigraphy.platelet_refactored;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.platelet_refactored.tabs.CountsTab;
import org.petctviewer.scintigraphy.platelet_refactored.tabs.MeansTab;
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
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflow_Platelet extends ControllerWorkflow {

	private List<ImagePlus> captures;

	/**
	 * @param main           Reference to the main class
	 * @param vue            View of the MVC pattern
	 * @param selectedImages Images used for this study
	 */
	public ControllerWorkflow_Platelet(PlateletScintigraphy main, FenApplicationWorkflow vue,
									   ImageSelection[] selectedImages) {
		super(main, vue, new ModelPlatelet(selectedImages, main.getStudyName()));

		this.generateInstructions();
		this.start();

		this.fenResults = new FenResults(this);
	}

	private void computeModel() {
		final int ROI_PER_IMAGE = (((PlateletScintigraphy) this.main).isAntPost() ? 6 : 3);

		for (int i = 0; i < getRoiManager().getRoisAsArray().length; i += ROI_PER_IMAGE) {
			// Post
			// - Spleen
			ImageState state = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, i / ROI_PER_IMAGE);
			((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_SPLEEN, state,
												 getRoiManager().getRoisAsArray()[i]);
			// - Liver
			state = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, i / ROI_PER_IMAGE);
			((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_LIVER, state,
												 getRoiManager().getRoisAsArray()[i + 1]);
			// - Heart
			state = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, i / ROI_PER_IMAGE);
			((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_HEART, state,
												 getRoiManager().getRoisAsArray()[i + 2]);

			// Ant
			if (((PlateletScintigraphy) this.main).isAntPost()) {
				// - Spleen
				state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, i / ROI_PER_IMAGE);
				((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_SPLEEN, state,
													 getRoiManager().getRoisAsArray()[i + 3]);
				// - Liver
				state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, i / ROI_PER_IMAGE);
				((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_LIVER, state,
													 getRoiManager().getRoisAsArray()[i + 4]);
				// - Heart
				state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, i / ROI_PER_IMAGE);
				((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_HEART, state,
													 getRoiManager().getRoisAsArray()[i + 5]);
			}
		}

		getModel().calculateResults();
	}

	@Override
	public ModelWorkflow getModel() {
		return (ModelWorkflow) super.getModel();
	}

	@Override
	protected void start() {
		getModel().setIsotope(Library_Dicom.getIsotope(getModel().getImagePlus(), this.vue));

		super.start();
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Choose from geo avg or counts
		boolean geoAvg = ((PlateletScintigraphy) ControllerWorkflow_Platelet.this.main).isAntPost();

		// Display results
		this.fenResults.clearTabs();
		this.fenResults.setMainTab(new CountsTab(this.fenResults, this.captures, geoAvg));
		this.fenResults.addTab(new MeansTab(this.fenResults, this.captures, geoAvg));
		this.fenResults.pack();
		this.fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[getModel().getImageSelection().length];

		this.captures = new ArrayList<>(2);

		DrawRoiInstruction dri_spleen = null, dri_liver = null, dri_heart = null;
		DrawRoiInstruction dri_spleen_ant = null, dri_liver_ant = null, dri_heart_ant = null;

		for (int i = 0; i < this.workflows.length; i++) {
			this.workflows[i] = new Workflow(this, getModel().getImageSelection()[i]);

			ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

			dri_spleen = new DrawRoiInstruction("Spleen", statePost, dri_spleen);
			dri_liver = new DrawRoiInstruction("Liver", statePost, dri_liver);
			dri_heart = new DrawRoiInstruction("Heart", statePost, dri_heart);

			this.workflows[i].addInstruction(dri_spleen);
			this.workflows[i].addInstruction(dri_liver);
			this.workflows[i].addInstruction(dri_heart);
			this.workflows[i].addInstruction(new ScreenShotInstruction(this.captures, this.vue, 0, 0, 0));

			if (((PlateletScintigraphy) this.main).isAntPost()) {
				ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

				if (dri_spleen_ant == null) dri_spleen_ant = dri_spleen;
				if (dri_liver_ant == null) dri_liver_ant = dri_liver;
				if (dri_heart_ant == null) dri_heart_ant = dri_heart;

				dri_spleen_ant = new DrawRoiInstruction("Spleen", stateAnt, dri_spleen_ant);
				dri_liver_ant = new DrawRoiInstruction("Liver", stateAnt, dri_liver_ant);
				dri_heart_ant = new DrawRoiInstruction("Heart", stateAnt, dri_heart_ant);

				this.workflows[i].addInstruction(dri_spleen_ant);
				this.workflows[i].addInstruction(dri_liver_ant);
				this.workflows[i].addInstruction(dri_heart_ant);

				this.workflows[i].addInstruction(new ScreenShotInstruction(this.captures, this.vue, 1, 0, 0));
			}
		}
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());
	}
}
