package org.petctviewer.scintigraphy.scin.instructions.drawing;

import ij.gui.Roi;
import ij.plugin.RoiScaler;
import org.petctviewer.scintigraphy.cardiac.CardiacScintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.json.InstructionFromGson;

import java.awt.*;

public class DrawSymmetricalRoiInstruction extends DrawRoiInstruction {

	private static final long serialVersionUID = 1L;
	private final transient Instruction dri_1;
	private final transient Workflow workflow;
	private final transient Organ organ;
	private final transient boolean isPostInverted;
	private final String roiName;

	public enum Organ {
		DEMIE, QUART
	}

	/**
	 * This Instruction draw a roi symmetrically from the width/2 or width/4 of the image.<br/> It take exemple from
	 * the
	 * ROI of the given Instruction.<br/> This Instruction was designed for the {@link CardiacScintigraphy}.
	 *
	 * @param organToDelimit    Name of organ. This will automatically add A for ANT and P for POST when the ROI is
	 *                          validated
	 * @param state             State of the image
	 * @param instructionToCopy Instruction to get the ROI from.
	 * @param roiName			Name of the ROI
	 * @param workflow          Workflow where this instruction is inserted
	 * @param organ             If you want to draw symmetrically from width/2(DEMIE) or width/4(QUART)
	 * @param isPostInverted 	true if post slice is inverted false otherwise
	 */
	public DrawSymmetricalRoiInstruction(String organToDelimit, ImageState state, Instruction instructionToCopy, String roiName, Workflow workflow, Organ organ, boolean isPostInverted) {
		super(organToDelimit, state, null, roiName);
		this.workflow = workflow;
		this.organ = organ;
		this.dri_1 = instructionToCopy;
		this.isPostInverted = isPostInverted;
		this.roiName = roiName;

		this.InstructionType = InstructionFromGson.DrawInstructionType.DRAW_SYMMETRICAL;
	}

	/**
	 * This Instruction draw a roi symmetrically from the width/2 or width/4 of the image.<br/> It take exemple from
	 * the
	 * ROI of the given Instruction.<br/> This Instruction was designed for the {@link CardiacScintigraphy}.
	 *
	 * @param organToDelimit    Name of organ. This will automatically add A for ANT and P for POST when the ROI is
	 *                          validated
	 * @param state             State of the image
	 * @param instructionToCopy Instruction to get the ROI from.
	 * @param roiName			Name of the ROI
	 * @param workflow          Workflow where this instruction is inserted
	 * @param organ             If you want to draw symmetrically from width/2(DEMIE) or width/4(QUART)
	 */
	public DrawSymmetricalRoiInstruction(String organToDelimit, ImageState state, Instruction instructionToCopy, String roiName, Workflow workflow, Organ organ) {
		this(organToDelimit, state, instructionToCopy, roiName, workflow, organ, true);
	}

	@Override
	public String getMessage() {
		return this.dri_1 == null ? "Delimit the " + this.organToDelimit : "Adjust the " + this.organToDelimit;
	}

	@Override
	public String getRoiName() {
		String name = this.roiName;

		Roi thisRoi = this.workflow.getController().getVue().getImagePlus().getRoi();

		if (name == null) return this.organToDelimit;
		if (name.equals("")) return "";
		if (thisRoi == null) return this.organToDelimit;
		boolean OrganPost = thisRoi.getXBase() > this.workflow.getController().getVue().getImagePlus().getWidth() / 2.;

		if (OrganPost)
			name += " P";
		else
			name += " A";

		return name;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		if (this.dri_1 != null) {
			Roi roi = (Roi) controller.getRoiManager().getRoi(dri_1.getRoiIndex()).clone();

			if (roi == null)
				return;

			if (super.getRoiName() != null)
				roi.setName(this.getRoiName());

			double newX = roi.getXBase();
			if (this.organ == Organ.QUART) {
				//flip the roi on the vertical axis
				roi = RoiScaler.scale(roi, -1, 1, true);
				double quart = controller.getVue().getImagePlus().getWidth() / 4.;
				newX = roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart) % quart) - roi.getFloatWidth();
			} else if (this.organ == Organ.DEMIE) {
				//get the width of the current ImagePlus
				double imageWidth = controller.getVue().getImagePlus().getWidth();

				if (this.isPostInverted) {
					// si la derniere roi etait post ou ant
					boolean impPost = roi.getXBase() > controller.getVue().getImagePlus().getWidth() / 2.;
					if (impPost) { // si la prise est ant, on decale l'organe precedent vers la droite
						newX = roi.getXBase() - (imageWidth / 2.);
					} else { // sinon vers la gauche
						newX = roi.getXBase() + (imageWidth / 2.);
					}
				} else {
					roi = RoiScaler.scale(roi, -1, 1, true);
					newX = imageWidth - (roi.getFloatWidth() + roi.getXBase());
				}
			}

			roi.setLocation(newX, roi.getYBase());
			roi.setStrokeColor(Color.RED);
			controller.getCurrentImageState().getImage().getImagePlus().setRoi(roi);
		}
	}
}
