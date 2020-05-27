package org.petctviewer.scintigraphy.mibg;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;
import org.petctviewer.scintigraphy.mibg.tabResults.TabMainMIBG;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabContrastModifier;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerWorkflowMIBG extends ControllerWorkflow {

	private List<ImagePlus> captures;

	public ControllerWorkflowMIBG(String studyName, FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelMIBG(selectedImages, studyName));

		this.captures = new ArrayList<>(4);

		this.generateInstructions();
		this.start();
	}

	@Override
	public void end() {
		super.end();

		this.model.calculateResults();

		FenResults fenResults = new FenResults(this);
		fenResults.setMainTab(new TabMainMIBG(fenResults, "Main", captures));
		// Image for the tab contrast
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(Arrays.stream(getModel().getImageSelection()).map(
				ImageSelection::getImagePlus).toArray(ImagePlus[]::new));

		MontageMaker mm = new MontageMaker();
		ImagePlus montage = new ImagePlus("Results MIBG", stackCapture);
		montage = mm.makeMontage2(montage, 1, 2, 1, 1, 2, 1, 10, false);

		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		fenResults.addTab(new TabContrastModifier(fenResults, "Contrast", montage));
		fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_heart = null, dri_mediastinum = null, 
							dri_heart1 = null, dri_mediastinum1 = null;


		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
		state.specifieImage(this.model.getImageSelection()[0]);

		dri_heart = new DrawRoiInstruction("Heart", state);
		dri_mediastinum = new DrawRoiInstruction("Mediastinum", state);
		
		this.workflows[0].addInstruction(dri_heart);
		this.workflows[0].addInstruction(dri_mediastinum);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));

		dri_heart1 = new DrawRoiInstruction("Heart", state, dri_heart);
		dri_mediastinum1 = new DrawRoiInstruction("Mediastinum", state, dri_mediastinum);
		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
		this.workflows[1].addInstruction(dri_heart1);
		this.workflows[1].addInstruction(dri_mediastinum1);
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));


		this.workflows[1].addInstruction(new EndInstruction());

	}

}
