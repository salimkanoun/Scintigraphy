package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackground;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.gui.Roi;

public class ControllerWorkflowDMSA extends ControllerWorkflow {

	private List<ImagePlus> captures;
	private boolean antPost;

	public ControllerWorkflowDMSA(Scintigraphy main, FenApplicationWorkflow vue, ModeleScin model) {
		super(main, vue, model);

		this.antPost = this.model.getImagePlus().getNSlices() == 2;

		this.generateInstructions();
		this.start();

	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];
		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		DrawRoiInstruction dri_1 = null, dri_2 = null;

		DrawRoiBackground dri_Background_1 = null, dri_Background_2 = null;

		ScreenShotInstruction dri_capture_1 = null;

		this.captures = new ArrayList<>();

		ImageState statePost = new ImageState(Orientation.POST, 1, ImageState.LAT_LR, ImageState.ID_NONE);
		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);

		dri_1 = new DrawRoiInstruction("L. Kidney", statePost);

		dri_Background_1 = new DrawRoiBackground("L. Background", statePost, dri_1, this.model, "");

		dri_2 = new DrawRoiInstruction("R. Kidney", statePost);

		dri_Background_2 = new DrawRoiBackground("R. Background", statePost, dri_2, this.model, "");

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_Background_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_Background_2);

		this.workflows[0].addInstruction(dri_capture_1);

		this.workflows[0].addInstruction(new EndInstruction());

	}

	@Override
	public void end() {
		// Clear the result hashmap in case of a second validation
		((Modele_Dmsa) this.model).data.clear();

		int indexRoi = 0;
		BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 512, 0).getBufferedImage();

		for (Roi roi : this.model.getRoiManager().getRoisAsArray()) {

			this.model.getImagePlus().setRoi(roi);
			String nom = ((DrawRoiInstruction) this.workflows[0].getInstructionAt(indexRoi)).getOrganToDelimit();
			// String nom = roi.getName();
			this.model.getImagePlus().setSlice(1);
			((Modele_Dmsa) this.model).enregistrerMesure(nom + " P0", this.model.getImagePlus());
			if (this.antPost) {
				this.model.getImagePlus().setSlice(2);
				((Modele_Dmsa) this.model).enregistrerMesure(nom + " A0", this.model.getImagePlus());
			}
			indexRoi++;
		}

		this.model.calculerResultats();

		new FenResultats_Dmsa(capture, this);
	}

}
