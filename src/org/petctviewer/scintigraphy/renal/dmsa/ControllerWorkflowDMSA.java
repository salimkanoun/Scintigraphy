package org.petctviewer.scintigraphy.renal.dmsa;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackground;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflowDMSA extends ControllerWorkflow {

	private final boolean antPost;
	private List<ImagePlus> captures;

	public ControllerWorkflowDMSA(FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new Model_Dmsa(selectedImages, vue.getStudyName()));

		this.antPost = this.model.getImagePlus().getNSlices() == 2;

		this.generateInstructions();
		this.start();

	}

	@Override
	public void end() {
		super.end();

		// Clear the result hashmap in case of a second validation
		((Model_Dmsa) this.model).data.clear();

		ImagePlus[] impCapture = new ImagePlus[1];
		impCapture[0] = this.captures.get(0);

		int indexRoi = 0;
		for (Roi roi : this.model.getRoiManager().getRoisAsArray()) {

			this.model.getImagePlus().setRoi(roi);
			String nom = ((DrawRoiInstruction) this.workflows[0].getInstructionAt(indexRoi)).getOrganToDelimit();
			// String studyName = roi.getStudyName();
			this.model.getImagePlus().setSlice(1);
			((Model_Dmsa) this.model).enregistrerMesure(nom + " P0", this.model.getImagePlus());
			if (this.antPost) {
				this.model.getImagePlus().setSlice(2);
				((Model_Dmsa) this.model).enregistrerMesure(nom + " A0", this.model.getImagePlus());
			}
			indexRoi++;
		}

		this.model.calculateResults();

		ImageSelection ims = new ImageSelection(impCapture[0], null, null);

		// Display results
		FenResults fenResults = new FenResults(this);
		fenResults.addTab(new MainTab(fenResults, ims));
		fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];
		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		this.captures = new ArrayList<>();
		ImageState statePost = new ImageState(Orientation.POST, 1, ImageState.LAT_LR, ImageState.ID_WORKFLOW);

		DrawRoiInstruction dri_1 = new DrawRoiInstruction("L. Kidney", statePost);

		DrawRoiInstruction dri_2 = new DrawRoiInstruction("R. Kidney", statePost);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(
				new DrawRoiBackground("L. Background", statePost, dri_1, this.workflows[0], ""));
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(
				new DrawRoiBackground("R. Background", statePost, dri_2, this.workflows[0], ""));

		this.workflows[0].addInstruction(new ScreenShotInstruction(this.captures, this.getVue(), 0));

		this.workflows[0].addInstruction(new EndInstruction());
	}

}
