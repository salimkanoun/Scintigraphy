package org.petctviewer.scintigraphy.scin.instructions.drawing;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction.Organ;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.awt.*;

public class DrawSymmetricalLoopInstruction extends DrawLoopInstruction {

	private static final long serialVersionUID = 1L;

	private final transient Instruction dri_1;

	private final transient ModelScin model;

	private final transient Organ organ;

	private boolean drawRoi;
	
	private final String RoiName;

	public DrawSymmetricalLoopInstruction(Workflow workflow, GeneratorInstruction parent, ImageState state,
			ModelScin model, Organ organ, String RoiName) {

		super(workflow, parent, state);

		this.model = model;
		this.organ = organ;
		this.dri_1 = parent;
		this.RoiName = RoiName == null ? "" : RoiName;
		this.drawRoi = true;

		this.InstructionType = DrawInstructionType.DRAW_SYMMETRICAL_LOOP;
	}

	@Override
	public Instruction generate() {
		if (!this.isStopped) {
			this.stop();
			return new DrawSymmetricalLoopInstruction(this.workflow, this, this.getImageState(), model, organ,
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

		Roi thisRoi = this.getImageState().getImage().getImagePlus().getRoi();
		if (thisRoi == null)
			return this.RoiName;

		boolean OrganPost = thisRoi.getXBase() > this.getImageState().getImage().getImagePlus().getWidth() / 2;

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
		this.drawRoi = false;
	}

	@Override
	public boolean saveRoi() {
		return this.drawRoi;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		if (this.indexLoop % 2 != 0) {
			// recupere la roi de l'organe symetrique
			Roi lastOrgan = this.model.getRoiManager().getRoi(dri_1.getRoiIndex());
			if (lastOrgan == null) { // si elle n'existe pas, on renvoie null
				return;
			}
			lastOrgan = (Roi) lastOrgan.clone();

			// si la derniere roi etait post ou ant
			boolean OrganPost = lastOrgan.getXBase() > this.getImageState().getImage().getImagePlus().getWidth() / 2;

			// si on doit faire le symetrique et que l'on a appuye sur next

			if (OrganPost) { // si la prise est ant, on decale l'organe precedent vers la droite
				lastOrgan.setLocation(
						lastOrgan.getXBase() - (this.getImageState().getImage().getImagePlus().getWidth() / 2),
						lastOrgan.getYBase());
			} else { // sinon vers la gauche
				lastOrgan.setLocation(
						lastOrgan.getXBase() + (this.getImageState().getImage().getImagePlus().getWidth() / 2),
						lastOrgan.getYBase());
			}
			if (this.indexLoop % 2 != 0)
				lastOrgan.setStrokeColor(Color.RED);
			controller.getCurrentImageState().getImage().getImagePlus().setRoi(lastOrgan);
		}
	}

}
