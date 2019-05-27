package org.petctviewer.scintigraphy.scin.instructions.drawing;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.gui.Roi;

public class DrawRoiBackground extends DrawRoiInstruction {

	private static final long serialVersionUID = 1L;

	private DrawInstructionType InstructionType = DrawInstructionType.DRAW_ROI_BACKGROUND;

	private transient DrawRoiInstruction dri_1;

	private transient ModelScin model;

	/**
	 * Instantiates a new instruction to draw a background ROI. This special Roi
	 * will automatically draw two other Roi around the specified Roi, folowing the
	 * shape.
	 * 
	 * @param organToDelimit
	 *            Name of the organ to delimit
	 * @param state
	 *            State of the image
	 * @param roi1
	 *            Instruction to take a copy of the ROI from
	 * @param model
	 *            Model, to get the Roi manager and the ImagePlus
	 */
	public DrawRoiBackground(String organToDelimit, ImageState state, DrawRoiInstruction roi1, ModelScin model) {
		super(organToDelimit, state);
		this.dri_1 = roi1;
		this.model = model;
	}

	/**
	 * Instantiates a new instruction to draw a background ROI. This special Roi
	 * will automatically draw two other Roi around the specified Roi, folowing the
	 * shape.
	 * 
	 * @param organToDelimit
	 *            Name of the organ to delimit
	 * @param state
	 *            State of the image
	 * @param roi1
	 *            Instruction to take a copy of the ROI from
	 * @param model
	 *            Model, to get the Roi manager and the ImagePlus
	 * @param roiName
	 *            Name of the Roi (displayed name)
	 */
	public DrawRoiBackground(String organToDelimit, ImageState state, DrawRoiInstruction roi1, ModelScin model,
			String roiName) {
		super(organToDelimit, state, roiName);
		this.dri_1 = roi1;
		this.model = model;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		Roi r1 = this.model.getRoiManager().getRoi(this.dri_1.getRoiIndex());
		controller.getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus()
				.setRoi(Library_Roi.createBkgRoi(r1, this.model.getImagePlus(), Library_Roi.KIDNEY));
	}

}
