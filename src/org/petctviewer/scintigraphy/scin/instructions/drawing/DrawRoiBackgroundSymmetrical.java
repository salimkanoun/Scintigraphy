package org.petctviewer.scintigraphy.scin.instructions.drawing;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;

public class DrawRoiBackgroundSymmetrical extends DrawRoiBackground {


	private static final long serialVersionUID = 1L;
	private final String roiName;


	/**
	 * Instantiates a new instruction to draw a background ROI. This special Roi will automatically draw two other Roi
	 * around the specified Roi, folowing the shape.
	 *
	 * @param organToDelimit Name of the organ to delimit
	 * @param state          State of the image
	 * @param roi1           Instruction to take a copy of the ROI from
	 * @param roiName        Name of the Roi (displayed name)
	 */
	public DrawRoiBackgroundSymmetrical(String organToDelimit, ImageState state, DrawRoiInstruction roi1,
										Workflow workflow, String roiName) {
		super(organToDelimit, state, roi1, workflow, roiName);
		this.roiName = roiName;
	}

	@Override
	public String getRoiName() {
		String name = this.roiName;

		Roi thisRoi = this.workflow.getController().getVue().getImagePlus().getRoi();

		if (name == null) return this.organToDelimit;
		if (name.equals("")) return "";
		if (thisRoi == null) return this.organToDelimit;
		boolean OrganPost = thisRoi.getXBase() > this.workflow.getController().getVue().getImagePlus().getWidth() / 2.;

		if (OrganPost) name += " P";
		else name += " A";

		return name;
	}

}
