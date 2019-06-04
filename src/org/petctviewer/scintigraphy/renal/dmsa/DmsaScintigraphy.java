package org.petctviewer.scintigraphy.renal.dmsa;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import ij.plugin.StackReverser;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;

public class DmsaScintigraphy extends Scintigraphy {

	public DmsaScintigraphy() {
		super("dmsa");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		if (selectedImages.length > 1) throw new WrongNumberImagesException(selectedImages.length, 1);

		ImageSelection imp = selectedImages[0].clone();

		if (selectedImages[0].getImageOrientation() == Orientation.ANT_POST) {
			imp.getImagePlus().getStack().getProcessor(1).flipHorizontal();
			// SK REVERSE DES METADATA A VERIFIER !!!!
			StackReverser reverser = new StackReverser();
			reverser.flipStack(imp.getImagePlus());
			imp.getImagePlus().getStack().getProcessor(2).flipHorizontal();
		} else if (selectedImages[0].getImageOrientation() == Orientation.POST_ANT) {
			imp.getImagePlus().getStack().getProcessor(2).flipHorizontal();
		} else if (selectedImages[0].getImageOrientation() == Orientation.POST) {

		} else {
			throw new WrongOrientationException(selectedImages[0].getImageOrientation(),
					new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.POST});
		}

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = imp;

		for(ImageSelection ims : selectedImages)
			ims.close();

		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus());
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.yellow);

		FenApplication fen = new FenApplicationWorkflow(selectedImages[0], this.getStudyName());
		this.setFenApplication(fen);
		selectedImages[0].getImagePlus().setOverlay(overlay);
		IJ.setTool(Toolbar.POLYGON);

		fen.setController(
				new ControllerWorkflowDMSA(this, (FenApplicationWorkflow) this.getFenApplication(), selectedImages));
	}

}