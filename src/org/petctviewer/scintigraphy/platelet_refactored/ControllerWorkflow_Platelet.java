package org.petctviewer.scintigraphy.platelet_refactored;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

public class ControllerWorkflow_Platelet extends ControllerWorkflow {

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
	}

	private void computeModel() {
		final int ROI_PER_IMAGE = (((PlateletScintigraphy) this.main).isAntPost() ? 6 : 3);

		int index = 0;
		for (Roi roi : getRoiManager().getRoisAsArray()) {
			// Post
			// - Spleen
			ImageState state = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, index / ROI_PER_IMAGE);
			((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_SPLEEN, state, roi);
			// - Liver
			state = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, (index + 1) / ROI_PER_IMAGE);
			((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_LIVER, state, roi);
			// - Heart
			state = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, (index + 2) / ROI_PER_IMAGE);
			((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_HEART, state, roi);

			// Ant
			if (((PlateletScintigraphy) this.main).isAntPost()) {
				// - Spleen
				state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, (index + 3) / ROI_PER_IMAGE);
				((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_SPLEEN, state, roi);
				// - Liver
				state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, (index + 4) / ROI_PER_IMAGE);
				((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_LIVER, state, roi);
				// - Heart
				state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, (index + 5) / ROI_PER_IMAGE);
				((ModelPlatelet) getModel()).addData(ModelPlatelet.REGION_HEART, state, roi);
			}
		}

		getModel().calculateResults();
	}

	@Override
	public ModelWorkflow getModel() {
		return (ModelWorkflow) super.getModel();
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Display results
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[getModel().getImageSelection().length];

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

			if (((PlateletScintigraphy) this.main).isAntPost()) {
				ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

				dri_spleen_ant = new DrawRoiInstruction("Spleen", stateAnt, dri_spleen_ant);
				dri_liver_ant = new DrawRoiInstruction("Liver", stateAnt, dri_liver_ant);
				dri_heart_ant = new DrawRoiInstruction("Heart", stateAnt, dri_heart_ant);

				this.workflows[i].addInstruction(dri_spleen_ant);
				this.workflows[i].addInstruction(dri_liver_ant);
				this.workflows[i].addInstruction(dri_heart_ant);
			}
		}
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());
	}
}
