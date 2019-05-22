package org.petctviewer.scintigraphy.scin.instructions.drawing;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.model.ModeleScin;

import ij.gui.Roi;
import ij.plugin.RoiScaler;

public class DrawSymmetricalRoiInstruction extends DrawRoiInstruction {

	private Instruction dri_1;

	private ModeleScin model;

	public enum Organ {
		DEMIE, QUART
	};

	private Organ organ;

	private String organToDelimit;

	public DrawSymmetricalRoiInstruction(String organToDelimit, ImageState state, Instruction instructionToCopy,
			String roiName, ModeleScin model, Organ organ) {
		super(organToDelimit, state, null, roiName);
		this.model = model;
		this.organ = organ;
		this.dri_1 = instructionToCopy;
		this.organToDelimit = organToDelimit;
	}

	@Override
	public String getMessage() {
		return this.dri_1 == null ? "Delimit the " + this.organToDelimit : "Adjust the " + this.organToDelimit;
	}

	@Override
	public String getRoiName() {
		String name = this.organToDelimit;

		Roi thisRoi = (Roi) this.getImageState().getImage().getImagePlus().getRoi();
		boolean OrganPost = thisRoi.getXBase() > this.getImageState().getImage().getImagePlus().getWidth() / 2;

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
			// symetrique du coeur
			if (this.dri_1 != null && this.organ == Organ.QUART) {
				Roi roi = (Roi) this.model.getRoiManager().getRoi(dri_1.roiToDisplay()).clone();

				// on fait le symetrique de la roi
				roi = RoiScaler.scale(roi, -1, 1, true);

				int quart = (this.getImageState().getImage().getImagePlus().getWidth() / 4);
				int newX = (int) (roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart) % quart)
						- roi.getFloatWidth());
				roi.setLocation(newX, roi.getYBase());

				roi.setStrokeColor(Color.RED);
				controller.getCurrentImageState().getImage().getImagePlus().setRoi(roi);
				return;
			}

			// recupere la roi de l'organe symetrique
			Roi lastOrgan = (Roi) this.model.getRoiManager().getRoi(dri_1.roiToDisplay());
			if (lastOrgan == null) { // si elle n'existe pas, on renvoie null
				return;
			}
			lastOrgan = (Roi) lastOrgan.clone();

			// si la derniere roi etait post ou ant
			boolean OrganPost = lastOrgan.getXBase() > this.getImageState().getImage().getImagePlus().getWidth() / 2;

			// si on doit faire le symetrique et que l'on a appuye sur next
			if (this.dri_1 != null) {

				if (OrganPost) { // si la prise est ant, on decale l'organe precedent vers la droite
					lastOrgan.setLocation(
							lastOrgan.getXBase() - (this.getImageState().getImage().getImagePlus().getWidth() / 2),
							lastOrgan.getYBase());
				} else { // sinon vers la gauche
					lastOrgan.setLocation(
							lastOrgan.getXBase() + (this.getImageState().getImage().getImagePlus().getWidth() / 2),
							lastOrgan.getYBase());
				}

				lastOrgan.setStrokeColor(Color.RED);
				controller.getCurrentImageState().getImage().getImagePlus().setRoi(lastOrgan);
				return;
			}
		}
	}
}
