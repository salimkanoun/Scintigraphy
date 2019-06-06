package org.petctviewer.scintigraphy.generic.statics;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;

public class StaticScintigraphy extends Scintigraphy {
	
	private boolean isSingleSlice;
	private boolean isAnt;

	public StaticScintigraphy() {
		super("General static scintigraphy");
		
		this.isSingleSlice = true;
		this.isAnt = true;
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		// Check number
		if (selectedImages.length != 1) {
			throw new WrongNumberImagesException(selectedImages.length, 1);
		}
		ImageSelection imp;
		// SK ETENDRE A SEULEMENT UNE INCIDENCE ??
		// SK PAS SUR QUE POST ANT SOIT BIEN PRIS EN COMPTE DANS LE FLIP / Ordre du
		// stack
		if (selectedImages[0].getImageOrientation() == Orientation.ANT_POST
				|| selectedImages[0].getImageOrientation() == Orientation.POST_ANT) {
			imp = Library_Dicom.ensureAntPostFlipped(selectedImages[0]);
			this.isSingleSlice = false;
		}else if (selectedImages[0].getImageOrientation() == Orientation.ANT){
			imp = selectedImages[0];
		}else if(selectedImages[0].getImageOrientation() == Orientation.POST) {
			imp = selectedImages[0];
			this.isAnt = false;
		} else {
			throw new WrongColumnException.OrientationColumn(selectedImages[0].getRow(),
					selectedImages[0].getImageOrientation(),
					new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
		}
		selectedImages[0].getImagePlus().close();
		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = imp;
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.white);

		this.setFenApplication(new FenApplication_ScinStatic(selectedImages[0], this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);

		this.getFenApplication().setController(new ControllerWorkflow_ScinStatic(this,
				(FenApplicationWorkflow) getFenApplication(), selectedImages, getStudyName()));

		((ModelScinStatic) this.getFenApplication().getController().getModel()).setIsSingleSlide(this.isSingleSlice);
		((ModelScinStatic) this.getFenApplication().getController().getModel()).setIsAnt(this.isAnt);
		IJ.setTool(Toolbar.POLYGON);
	}

}
