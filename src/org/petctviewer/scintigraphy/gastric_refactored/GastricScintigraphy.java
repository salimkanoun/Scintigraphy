package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;

public class GastricScintigraphy extends Scintigraphy {

	protected GastricScintigraphy() {
		super("Gastric Scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// Check number
		if(openedImages.length < 2)
			throw new WrongNumberImagesException(openedImages.length, 2, Integer.MAX_VALUE);
		
		// Check orientation
		for(ImageSelection ims : openedImages) {
			if(ims.getImageOrientation() != Orientation.ANT_POST)
				throw new WrongOrientationException(ims.getImageOrientation(), new Orientation[] {Orientation.ANT_POST});
		}
		
		return openedImages;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication_Grastric(selectedImages[0].getImagePlus(), getStudyName()));
		this.getFenApplication().setControleur(new Controleur_Gastric());
	}

}
