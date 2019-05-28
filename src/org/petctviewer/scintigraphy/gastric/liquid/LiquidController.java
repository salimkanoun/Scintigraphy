package org.petctviewer.scintigraphy.gastric.liquid;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class LiquidController extends ControllerWorkflow {

	private final boolean HAS_POST_IMAGES;

	/**
	 * @param main Reference to the main class
	 * @param vue  View of the MVC pattern
	 */
	public LiquidController(Scintigraphy main, FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(main, vue, new LiquidModel(selectedImages, main.getStudyName()));

		this.HAS_POST_IMAGES = selectedImages[0].getImageOrientation().hasBothFacingOrientations();

		this.generateInstructions();
		this.start();
	}

	private void computeModel() {
		Roi[] rois = getModel().getRoiManager().getRoisAsArray();

		int increment = 1;
		if (HAS_POST_IMAGES) increment = 2;

		for (int i = 0; i < rois.length; i += increment) {
			ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, i / 2);

			// - Stomach
			getModel().calculateCounts(stateAnt, rois[i]);

			if (HAS_POST_IMAGES) {
				ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, i / 2);

				// - Stomach
				getModel().calculateCounts(statePost, rois[i]);
			}
		}

		getModel().calculateResults();
	}

	@Override
	public LiquidModel getModel() {
		return (LiquidModel) super.getModel();
	}

	@Override
	protected void start() {
		// Get isotope
		getModel().setIsotope(Library_Dicom.getIsotope(getModel().getImagePlus(), this.vue));

		super.start();
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Display results
		FenResults fenResults = new FenResults(this);
		fenResults.addTab(new MainTab(fenResults));
		fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[getModel().getImageSelection().length];

		DrawRoiInstruction dri_ant = null, dri_post;
		for (int i = 0; i < this.workflows.length; i++) {
			this.workflows[i] = new Workflow(this, getModel().getImageSelection()[i]);

			// Ant
			ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

			dri_ant = new DrawRoiInstruction("Stomach", stateAnt, dri_ant);

			this.workflows[i].addInstruction(dri_ant);

			// Post
			if (HAS_POST_IMAGES) {
				ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

				dri_post = new DrawRoiInstruction("Stomach", statePost, dri_ant);

				this.workflows[i].addInstruction(dri_post);
			}
		}

		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());

		// Update view
		this.getVue().setNbInstructions(this.allInputInstructions().size());
	}
}
