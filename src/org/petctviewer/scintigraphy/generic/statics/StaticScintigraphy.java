package org.petctviewer.scintigraphy.generic.statics;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class StaticScintigraphy extends Scintigraphy {

	public StaticScintigraphy() {
		super("General static scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		if (selectedImages.length != 1) {
			throw new WrongNumberImagesException(selectedImages.length, 1);
		}
		ImageSelection imp = null;
		// SK ETENDRE A SEULEMENT UNE INCIDENCE ??
		// SK PAS SUR QUE POST ANT SOIT BIEN PRIS EN COMPTE DANS LE FLIP / Ordre du
		// stack
		if (selectedImages[0].getImageOrientation() == Orientation.ANT_POST
				|| selectedImages[0].getImageOrientation() == Orientation.POST_ANT) {
			imp = Library_Dicom.ensureAntPostFlipped(selectedImages[0]);
			selectedImages[0].getImagePlus().close();
		} else {
			throw new WrongOrientationException(selectedImages[0].getImageOrientation(),
					new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
		}

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = imp;
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.white);

		this.setFenApplication(new FenApplication_ScinStatic(selectedImages[0].getImagePlus(), this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);

		((FenApplicationWorkflow) this.getFenApplication()).setControleur(new ControllerWorkflow_ScinStatic(this,
				(FenApplicationWorkflow) getFenApplication(), selectedImages, getStudyName()));
		IJ.setTool(Toolbar.POLYGON);
	}

}
