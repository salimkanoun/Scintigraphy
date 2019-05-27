package org.petctviewer.scintigraphy.colonic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.Arrays;

public class ColonicScintigraphy extends Scintigraphy {

	public ColonicScintigraphy() {
		super("ColonicScintigraphy");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// Check number
		if (openedImages.length < 2 || openedImages.length > 4)
			throw new WrongNumberImagesException(openedImages.length, 2, 4);

		ImageSelection[] impSelect = new ImageSelection[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {
			if (openedImages[i].getImageOrientation() == Orientation.ANT_POST
					|| openedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSelect[i] = Library_Dicom.ensureAntPostFlipped(openedImages[i]);
			} else {
				throw new WrongColumnException.OrientationColumn(openedImages[i].getRow(),
						openedImages[i].getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
			}
			openedImages[i].getImagePlus().close();
		}

		// Order images by time
		Arrays.parallelSort(impSelect, new ChronologicalAcquisitionComparator());

		return impSelect;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// TODO Auto-generated method stub

		this.setFenApplication(new FenApplicationColonicTransit(selectedImages[0], this.getStudyName()));
		this.getFenApplication().setController(new ControllerWorkflowColonicTransit(
				this, (FenApplicationColonicTransit) this.getFenApplication(), selectedImages));
	}

}
