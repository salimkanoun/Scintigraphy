package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class GastricScintigraphy extends Scintigraphy {

	public GastricScintigraphy() {
		super("Gastric Scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// Check number
		if (openedImages.length < 2)
			throw new WrongNumberImagesException(openedImages.length, 2, Integer.MAX_VALUE);

		// Check orientation
		ImageSelection[] selection = new ImageSelection[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {
			ImageSelection ims = openedImages[i];
			if (ims.getImageOrientation() != Orientation.ANT_POST)
				throw new WrongOrientationException(ims.getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST });
			selection[i] = new ImageSelection(Library_Dicom.sortImageAntPost(ims.getImagePlus()), null, null);
			ims.getImagePlus().close();
		}

		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication_Grastric(selectedImages[0].getImagePlus(), getStudyName()));
		this.getFenApplication().setControleur(
				new Controller_Gastric(this, this.getFenApplication(), selectedImages, "Gastric Scintigraphy"));
		this.getFenApplication().setVisible(true);
	}

}
