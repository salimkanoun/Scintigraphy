package org.petctviewer.scintigraphy.scin.instructions.drawing;

import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.gui.Roi;

public class DrawRoiBackgroundSymmetrical extends DrawRoiBackground {


	private static final long serialVersionUID = 1L;

	
	
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
	public DrawRoiBackgroundSymmetrical(String organToDelimit, ImageState state, DrawRoiInstruction roi1,
			ModelScin model) {
		super(organToDelimit, state, roi1, model);
		// TODO Auto-generated constructor stub
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
	public DrawRoiBackgroundSymmetrical(String organToDelimit, ImageState state, DrawRoiInstruction roi1, ModelScin model,
			String roiName) {
		super(organToDelimit, state, roi1, model, roiName);
	}
	
	@Override
	public String getRoiName() {
		String name = this.organToDelimit;

		Roi thisRoi = this.getImageState().getImage().getImagePlus().getRoi();
		if(thisRoi == null)
			return this.organToDelimit;
		
		boolean OrganPost = thisRoi.getXBase() > this.getImageState().getImage().getImagePlus().getWidth() / 2;

		if (OrganPost)
			name += " P";
		else
			name += " A";

		return name;
	}

}
