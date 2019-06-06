package org.petctviewer.scintigraphy.platelet;

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

import java.util.Arrays;

public class PlateletScintigraphy extends Scintigraphy {

	private boolean isAntPost;

	public PlateletScintigraphy() {
		super("Platelet Scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException, ReadTagException {
		// Check number
		if (openedImages.length < 2) throw new WrongNumberImagesException(openedImages.length, 2, Integer.MAX_VALUE);

		// Check orientation
		boolean isAntPost = false, isPostOnly = false;
		ImageSelection[] selections = new ImageSelection[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {
			ImageSelection ims = openedImages[i];
			if (ims.getImageOrientation() == Orientation.ANT_POST) {
				if (!isPostOnly) {
					selections[i] = Library_Dicom.ensureAntPostFlipped(ims);
					isAntPost = true;
				} else throw new WrongInputException("Cannot accept different orientations");
			} else if (ims.getImageOrientation() == Orientation.POST_ANT) {
				if (!isPostOnly) {
					selections[i] = Library_Dicom.ensureAntPostFlipped(ims);
					isAntPost = true;
				} else throw new WrongInputException("Cannot accept different orientations");
			} else if (ims.getImageOrientation() == Orientation.POST) {
				if (!isAntPost) {
					selections[i] = ims.clone();
					isPostOnly = true;
				} else throw new WrongInputException("Cannot accept different orientations");
			} else throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
					new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.POST});
		}
		// Specify orientation
		this.isAntPost = isAntPost;

		// Close all images
		Arrays.stream(openedImages).forEach(ImageSelection::close);

		// Order images by time
		Arrays.parallelSort(selections, new ChronologicalAcquisitionComparator());

		return selections;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplicationWorkflow(selectedImages[0], getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflow_Platelet(this, (FenApplicationWorkflow) this.getFenApplication(),
						selectedImages));
		this.getFenApplication().setVisible(true);
	}

	public boolean isAntPost() {
		return this.isAntPost;
	}
}
