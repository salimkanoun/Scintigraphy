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

	private void computeModel() {
		Roi roi = getRoiManager().getRoisAsArray()[0];

		ImageSelection image = getModel().getImageSelection()[0];

		// Create durations array
		int[] durations = Library_Dicom.buildFrameDurations(image.getImagePlus());
		for (int i = 1; i < durations.length; i++)
			durations[i] = durations[i] + durations[i - 1];

		// Save results in series
		for (int i = 0; i < image.getImagePlus().getStackSize(); i++) {
			// Apply ROI
			image.getImagePlus().setRoi(roi);
			image.getImagePlus().setSlice(i);
			getModel().calculateCounts(image.getImagePlus(), durations[i]);
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
		this.computeModel();

		// Display results
		FenResults fenResults = new FenResults(this);
		fenResults.addTab(new MainTab(fenResults));
		fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[1];
		this.workflows[0] = new Workflow(this, getModel().getImageSelection()[0]);

		// Ant
		ImageState stateAnt = new ImageState(Orientation.ANT, 0, ImageState.LAT_RL, ImageState.ID_CUSTOM_IMAGE);
		System.out.println(getModel().getImageSelection()[0]);
		ImageSelection image = getModel().getImageSelection()[0].clone();
		image.setImagePlus(Library_Dicom.projeter(image.getImagePlus(), 0, image.getImagePlus().getStackSize(), "avg"
		));
		stateAnt.specifieImage(image);


		this.workflows[0].addInstruction(new DrawRoiInstruction("Stomach", stateAnt));

		this.workflows[0].addInstruction(new EndInstruction());
	}

	@Override
	public void start() {
		super.start();

		// Get isotope
		getModel().setIsotope(Library_Dicom.getIsotope(getModel().getImagePlus(), this.vue));
	}
}
