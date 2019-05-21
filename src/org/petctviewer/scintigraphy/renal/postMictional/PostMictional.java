package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.gui.TabPostMict;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Overlay;

public class PostMictional extends Scintigraphy {

	private TabPostMict resultFrame;

	public PostMictional(String[] organes, TabPostMict resultFrame) {
		super("Post-mictional");

		this.resultFrame = resultFrame;
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		ImageSelection impSorted = null;
		if (selectedImages[0].getImageOrientation() == Orientation.ANT_POST) {
			impSorted = Library_Dicom.ensureAntPostFlipped(selectedImages[0]);
			impSorted = selectedImages[0].clone();
			IJ.run(impSorted.getImagePlus(), "Reverse", "");
			impSorted.getImagePlus().getStack().getProcessor(2).flipHorizontal();

		} else if (selectedImages[0].getImageOrientation() == Orientation.POST_ANT) {
			impSorted = selectedImages[0].clone();

		} else if (selectedImages[0].getImageOrientation() == Orientation.POST) {
			impSorted = selectedImages[0].clone();
		}

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = impSorted;
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay ov = Library_Gui.initOverlay(selectedImages[0].getImagePlus());

		FenApplicationWorkflow fen = new FenApplicationWorkflow(selectedImages[0], this.getStudyName());
		fen.setVisible(true);
		this.setFenApplication(fen);
		selectedImages[0].getImagePlus().setOverlay(ov);
		// Controleur_PostMictional ctrl = new Controleur_PostMictional(this,
		// this.organes, "Post-mictional");
		// this.getFenApplication().setControleur(ctrl);
		((FenApplicationWorkflow) this.getFenApplication()).setControleur(
				new ControllerWorkflowPostMictional(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Modele_PostMictional(selectedImages, "Post-mictional"),
						((Modele_Renal) this.resultFrame.getParent().getModel()).getKidneys()));
	}

	// public HashMap<String, Double> getData() {
	// return ((Modele_PostMictional) this.getModele()).getData();
	// }

	public BufferedImage getCapture() {
		return null;
	}

	public TabPostMict getResultFrame() {
		return resultFrame;

	}

}
