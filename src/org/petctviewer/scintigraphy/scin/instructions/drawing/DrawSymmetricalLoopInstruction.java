package org.petctviewer.scintigraphy.scin.instructions.drawing;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction.Organ;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;
import org.petctviewer.scintigraphy.scin.json.InstructionFromGson;

import ij.gui.Roi;

public class DrawSymmetricalLoopInstruction extends DrawLoopInstruction {

	private static final long serialVersionUID = 1L;

	private final transient Instruction dri_1;

	private final transient Organ organ;

	private boolean drawRoi;
	
	private boolean expectingUserInput;

	public DrawSymmetricalLoopInstruction(Workflow workflow, GeneratorInstruction parent, ImageState state, Organ organ, String RoiName) {

		super(workflow, parent, state);

		this.organ = organ;
		this.dri_1 = parent;
		this.RoiName = RoiName == null ? "" : RoiName;
		this.drawRoi = true;

		this.InstructionType = InstructionFromGson.DrawInstructionType.DRAW_SYMMETRICAL_LOOP;
		this.expectingUserInput = true;
	}

	@Override
	public Instruction generate() {
		if (!this.isStopped) {
			this.stop();
			this.workflow.getController().getVue().setNbInstructions(this.workflow.getController().allInputInstructions().size() + 1);
			return new DrawSymmetricalLoopInstruction(this.workflow, this, this.getImageState(), organ,
					this.RoiName);
			
		}
		return null;
	}

	@Override
	public String getMessage() {
		return this.indexLoop % 2 == 0 ? "Delimit a new contamination" : "Adjust contamination zone";
	}

	@Override
	public String getRoiName() {
		String name = this.RoiName;

		Roi thisRoi = this.workflow.getController().getVue().getImagePlus().getRoi();
		if (thisRoi == null)
			return this.RoiName;

		boolean OrganPost = thisRoi.getXBase() > this.workflow.getController().getVue().getImagePlus().getWidth() / 2.;

		if (OrganPost)
			name += " P";
		else
			name += " A";

		name += "" + (indexLoop / 2);

		return name;
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public boolean saveRoi() {
		return this.drawRoi;
	}
	
	public void setExpectingUserInput(boolean expectingUserInput) {
		this.expectingUserInput = expectingUserInput;
	}
	
	@Override
	public boolean isExpectingUserInput() {
		return this.expectingUserInput;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		if (this.indexLoop % 2 != 0) {
			// recupere la roi de l'organe symetrique
			Roi lastOrgan = this.workflow.getController().getModel().getRoiManager().getRoi(dri_1.getRoiIndex());
			if (lastOrgan == null) { // si elle n'existe pas, on renvoie null
				return;
			}
			lastOrgan = (Roi) lastOrgan.clone();

			// si la derniere roi etait post ou ant
			boolean OrganPost =
					lastOrgan.getXBase() > this.workflow.getController().getVue().getImagePlus().getWidth() / 2.;

			// si on doit faire le symetrique et que l'on a appuye sur next

			if (OrganPost) { // si la prise est ant, on decale l'organe precedent vers la droite
				lastOrgan.setLocation(
						lastOrgan.getXBase() - (this.workflow.getController().getVue().getImagePlus().getWidth() / 2.),
						lastOrgan.getYBase());
			} else { // sinon vers la gauche
				lastOrgan.setLocation(
						lastOrgan.getXBase() + (this.workflow.getController().getVue().getImagePlus().getWidth() / 2.),
						lastOrgan.getYBase());
			}
			if (this.indexLoop % 2 != 0)
				lastOrgan.setStrokeColor(Color.RED);
			controller.getCurrentImageState().getImage().getImagePlus().setRoi(lastOrgan);
		}
	}

	@Override
	public String toString() {
		return "DrawSymmetricalLoopInstruction [ isRoiVisible :" + this.isRoiVisible() + ", RoiName" +
				this.getRoiName() + ", isStopped : "+this.isStopped+"]";
	}

}
