package org.petctviewer.scintigraphy.scin.instructions.drawing;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

import ij.gui.Roi;
import ij.plugin.RoiScaler;

public class DrawSymmetricalRoiInstruction extends DrawRoiInstruction {

	private Instruction dri_1;

	private ModeleScin model;

	public enum Organ {
		HEART, OTHER
	};

	private Organ organ;

	public DrawSymmetricalRoiInstruction(String organToDelimit, ImageState state, Instruction instructionToCopy,
			String roiName, ModeleScin model, Organ organ) {
		super(organToDelimit, state, null, roiName);
		this.model = model;
		this.organ = organ;
		this.dri_1 = instructionToCopy;
	}
	
	
	@Override
	public String getMessage() {
		return "Adjust the "+this.getRoiName() ;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		super.afterNext(controller);
		// symetrique du coeur
		if (this.dri_1 != null && this.organ == Organ.HEART) {
			Roi roi = (Roi) this.model.getRoiManager().getRoi(dri_1.roiToDisplay()).clone();

			// on fait le symetrique de la roi
			roi = RoiScaler.scale(roi, -1, 1, true);

			int quart = (this.model.getImageSelection()[0].getImagePlus().getWidth() / 4);
			int newX = (int) (roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart) % quart) - roi.getFloatWidth());
			roi.setLocation(newX, roi.getYBase());

			controller.getCurrentImageState().getImage().getImagePlus()
					.setRoi(roi);
			return;
		}

		// recupere la roi de l'organe symetrique
		Roi lastOrgan = (Roi) this.model.getRoiManager().getRoi(dri_1.roiToDisplay());
		if (lastOrgan == null) { // si elle n'existe pas, on renvoie null
			return;
		}
		lastOrgan = (Roi) lastOrgan.clone();

		// si la derniere roi etait post ou ant
		boolean OrganPost = lastOrgan.getXBase() > this.model.getImageSelection()[0].getImagePlus().getWidth() / 2;

		// si on doit faire le symetrique et que l'on a appuye sur next
		if (this.dri_1 != null) {

			if (OrganPost) { // si la prise est ant, on decale l'organe precedent vers la droite
				lastOrgan.setLocation(
						lastOrgan.getXBase() - (this.model.getImageSelection()[0].getImagePlus().getWidth() / 2),
						lastOrgan.getYBase());
			} else { // sinon vers la gauche
				lastOrgan.setLocation(
						lastOrgan.getXBase() + (this.model.getImageSelection()[0].getImagePlus().getWidth() / 2),
						lastOrgan.getYBase());
			}

			controller.getCurrentImageState().getImage().getImagePlus().setRoi(lastOrgan);
			return;
		}
	}
}
