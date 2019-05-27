package org.petctviewer.scintigraphy.renal.postMictional;

import ij.gui.Overlay;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.gui.TabPostMict;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.image.BufferedImage;

public class PostMictional extends Scintigraphy {

	private final TabPostMict resultFrame;

	public PostMictional(String[] organes, TabPostMict resultFrame) {
		super("Post-mictional");

		this.resultFrame = resultFrame;
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		ImageSelection impSorted = null;
		if (selectedImages[0].getImageOrientation() == Orientation.ANT_POST) {
			impSorted = Library_Dicom.ensureAntPostFlipped(selectedImages[0]);

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
		// this.getFenApplication().setController(ctrl);
		this.getFenApplication().setController(
				new ControllerWorkflowPostMictional(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Model_PostMictional(selectedImages, "Post-mictional"),
						((Model_Renal) this.resultFrame.getParent().getModel()).getKidneys()));
	}

	// public HashMap<String, Double> getData() {
	// return ((Model_PostMictional) this.getModele()).getData();
	// }

	public BufferedImage getCapture() {
		return null;
	}

	public TabPostMict getResultFrame() {
		return resultFrame;

	}

}
