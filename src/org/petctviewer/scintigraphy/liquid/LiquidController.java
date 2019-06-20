package org.petctviewer.scintigraphy.liquid;

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

	/**
	 * @param main Reference to the main class
	 * @param vue  View of the MVC pattern
	 */
	public LiquidController(Scintigraphy main, FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(main, vue, new LiquidModel(selectedImages, main.getStudyName()));

		this.generateInstructions();
		this.start();
	}

	private int[] sumFrameDurations(int[] frameDurations) {
		int[] res = new int[frameDurations.length];
		res[0] = frameDurations[0];
		for (int i = 1; i < frameDurations.length; i++)
			res[i] = frameDurations[i] + res[i - 1];
		return res;
	}

	private void computeModel() {
		Roi roi = getRoiManager().getRoisAsArray()[0];

		ImageSelection image = getModel().getImageSelection()[0];

		// Create durations array
		int[] durations = sumFrameDurations(Library_Dicom.buildFrameDurations(image.getImagePlus()));

		// Save results in series
		for (int i = 0; i < image.getImagePlus().getStackSize(); i++) {
			// Apply ROI
			image.getImagePlus().setRoi(roi);
			image.getImagePlus().setSlice(i);
			getModel().calculateCounts(image.getImagePlus(), durations[i], Orientation.ANT);
		}

		if (getModel().getImageSelection().length > 1) {
			Roi roiPost = getRoiManager().getRoisAsArray()[1];
			ImageSelection imagePost = getModel().getImageSelection()[1];
			// Frame durations
			int[] durationsPost = this.sumFrameDurations(Library_Dicom.buildFrameDurations(imagePost.getImagePlus()));
			// Check frame durations are equals
			for (int i = 0; i < durations.length; i++) {
				if (durations[i] != durationsPost[i]) {
					System.out.println("#" + i + ": ant " + durations[i] + " != post " + durationsPost[i]);
				}
			}
			// Post
			for (int i = 0; i < imagePost.getImagePlus().getStackSize(); i++) {
				// Apply ROI
				imagePost.getImagePlus().setRoi(roiPost);
				imagePost.getImagePlus().setSlice(i);
				getModel().calculateCounts(imagePost.getImagePlus(), durationsPost[i], Orientation.POST);
			}
		}

		getModel().calculateResults();
	}

	@Override
	public LiquidModel getModel() {
		return (LiquidModel) super.getModel();
	}

	@Override
	protected void end() {
		super.end();

		// Compute series
		getModel().clearResults();
		this.computeModel();

		// Display results
		FenResults fenResults = new FenResults(this);
		fenResults.addTab(new MainTab(fenResults));
		fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[getModel().getImageSelection().length];
		this.workflows[0] = new Workflow(this, getModel().getImageSelection()[0]);

		// Ant
		ImageState stateAnt = new ImageState(Orientation.ANT, 0, ImageState.LAT_RL, ImageState.ID_CUSTOM_IMAGE);
		ImageSelection image = getModel().getImageSelection()[0].clone();
		image.setImagePlus(Library_Dicom.projeter(image.getImagePlus(), 0, image.getImagePlus().getStackSize(), "avg"
		));
		stateAnt.specifieImage(image);

		this.workflows[0].addInstruction(new DrawRoiInstruction("Stomach", stateAnt));

		if (this.workflows.length > 1) {
			this.workflows[1] = new Workflow(this, getModel().getImageSelection()[1]);
			// Post
			ImageState statePost = new ImageState(Orientation.POST, 0, ImageState.LAT_LR, ImageState.ID_CUSTOM_IMAGE);
			ImageSelection imagePost = getModel().getImageSelection()[1].clone();
			imagePost.setImagePlus(
					Library_Dicom.projeter(imagePost.getImagePlus(), 0, imagePost.getImagePlus().getStackSize(),
										   "avg"));
			statePost.specifieImage(imagePost);

			this.workflows[1].addInstruction(new DrawRoiInstruction("Stomach", statePost));
		}

		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());
	}

	@Override
	public void start() {
		super.start();

		// Get isotope
		getModel().setIsotope(Library_Dicom.getIsotope(getModel().getImagePlus(), this.vue));
	}
}
