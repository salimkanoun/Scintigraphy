package org.petctviewer.scintigraphy.platelet;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.List;

public class PlateletScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Platelet Scintigraphy";
	private boolean isAntPost;

	public PlateletScintigraphy() {
		super(STUDY_NAME);
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

	@Override
	public String getName() {
		return STUDY_NAME;
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException,
			ReadTagException {
		// Check number
		if (openedImages.size() < 2) throw new WrongNumberImagesException(openedImages.size(), 2, Integer.MAX_VALUE);

		// Check orientation
		boolean isAntPost = false, isPostOnly = false;
		List<ImageSelection> selections = new ArrayList<>();
		for (ImageSelection ims : openedImages) {
			if (ims.getImageOrientation() == Orientation.ANT_POST) {
				if (!isPostOnly) {
					selections.add(Library_Dicom.ensureAntPostFlipped(ims));
					isAntPost = true;
				} else throw new WrongInputException("Cannot accept different orientations");
			} else if (ims.getImageOrientation() == Orientation.POST_ANT) {
				if (!isPostOnly) {
					selections.add(Library_Dicom.ensureAntPostFlipped(ims));
					isAntPost = true;
				} else throw new WrongInputException("Cannot accept different orientations");
			} else if (ims.getImageOrientation() == Orientation.POST) {
				if (!isAntPost) {
					selections.add(ims.clone());
					isPostOnly = true;
				} else throw new WrongInputException("Cannot accept different orientations");
			} else throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
																	new Orientation[]{Orientation.ANT_POST,
																					  Orientation.POST_ANT,
																					  Orientation.POST});
		}
		// Specify orientation
		this.isAntPost = isAntPost;

		// Close all images
		openedImages.forEach(ImageSelection::close);

		// Order images by time
		selections.sort(new ChronologicalAcquisitionComparator());

		return selections;
	}

	@Override
	public String instructions() {
		return "Minimum 2 images in Ant-Post, Post-Ant or Post orientation";
	}
}
