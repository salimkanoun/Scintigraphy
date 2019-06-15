package org.petctviewer.scintigraphy.scin.instructions.drawing;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

public class DrawRoiBackground extends DrawRoiInstruction {

	private static final long serialVersionUID = 1L;

	private final transient DrawRoiInstruction dri_reference;

	protected final transient Workflow workflow;

	/**
	 * Instantiates a new instruction to draw a background ROI. This special Roi will automatically draw two other Roi
	 * around the specified Roi, folowing the shape.
	 *
	 * @param organToDelimit Name of the organ to delimit
	 * @param state          State of the image
	 * @param roi1           Instruction to take a copy of the ROI from
	 * @param roiName        Name of the Roi (displayed name)
	 */
	public DrawRoiBackground(String organToDelimit, ImageState state, DrawRoiInstruction roi1, Workflow workflow,
							 String roiName) {
		super(organToDelimit, state, roiName);
		this.dri_reference = roi1;
		this.workflow = workflow;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		Roi r1 = this.workflow.getController().getRoiManager().getRoi(this.dri_reference.getRoiIndex());
		controller.getCurrentImageState().getImage().getImagePlus().setRoi(
				Library_Roi.createBkgRoi(r1, controller.getCurrentImageState().getImage().getImagePlus(),
										 Library_Roi.KIDNEY));
	}

}
