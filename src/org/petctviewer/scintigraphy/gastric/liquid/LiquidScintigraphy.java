package org.petctviewer.scintigraphy.gastric.liquid;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LiquidScintigraphy extends Scintigraphy {

	public LiquidScintigraphy() {
		super("Liquid Scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException, ReadTagException {
		// Check number of images
		if (openedImages.length < 2) throw new WrongNumberImagesException(openedImages.length, 2, Integer.MAX_VALUE);

		// Check orientation
		Orientation[] acceptedOrientations = new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT,
		                                                       Orientation.ANT};
		List<ImageSelection> selection = new ArrayList<>(openedImages.length);
		for (ImageSelection ims : openedImages) {
			if (Arrays.stream(acceptedOrientations).noneMatch(o -> o == ims.getImageOrientation()))
				throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
						acceptedOrientations);

			// Sort orientation to always have Ant
			if (ims.getImageOrientation() != Orientation.ANT) {
				selection.add(Library_Dicom.ensureAntPostFlipped(ims));
			}
		}

		// Close previous images
		Arrays.stream(openedImages).forEach(i -> i.getImagePlus().close());

		// Order images by time
		selection.sort(new ChronologicalAcquisitionComparator());

		return selection.toArray(new ImageSelection[0]);
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplicationWorkflow(selectedImages[0], this.getStudyName()));
		this.getFenApplication().setController(
				new LiquidController(this, (FenApplicationWorkflow) this.getFenApplication(), selectedImages));
	}
}
