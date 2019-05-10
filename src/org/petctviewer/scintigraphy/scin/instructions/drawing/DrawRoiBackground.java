package org.petctviewer.scintigraphy.scin.instructions.drawing;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.gui.Roi;

public class DrawRoiBackground extends DrawRoiInstruction {
	
	private DrawRoiInstruction dri_1;
	
	private ModeleScin model;

	public DrawRoiBackground(String organToDelimit, ImageState state, DrawRoiInstruction roi1, ModeleScin model) {
		super(organToDelimit, state);
		this.dri_1 = roi1;
		this.model = model;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		Roi r1 = this.model.getRoiManager().getRoi(this.dri_1.roiToDisplay());
		controller.getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus()
				.setRoi(Library_Roi.createBkgRoi(r1, this.model.getImagePlus(),
						Library_Roi.KIDNEY));
	}

}
