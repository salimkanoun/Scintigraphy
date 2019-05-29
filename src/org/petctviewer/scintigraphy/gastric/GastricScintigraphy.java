package org.petctviewer.scintigraphy.gastric;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.Arrays;

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
			if (ims.getImageOrientation() != Orientation.ANT_POST && ims.getImageOrientation() != Orientation.POST_ANT)
				throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST });
			selection[i] = Library_Dicom.ensureAntPostFlipped(ims);
		}

		// TODO: check images have same duration

		// Close all images
		Arrays.stream(openedImages).forEach(ims -> ims.getImagePlus().close());

		// Order images by time
		Arrays.parallelSort(selection, new ChronologicalAcquisitionComparator());

		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplicationWorkflow(selectedImages[0], getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflow_Gastric(this, (FenApplicationWorkflow) this.getFenApplication(), selectedImages,
						"Gastric Scintigraphy"));
		this.getFenApplication().setVisible(true);
	}

}
