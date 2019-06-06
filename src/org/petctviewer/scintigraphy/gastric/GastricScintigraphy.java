package org.petctviewer.scintigraphy.gastric;

import ij.Prefs;
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
import org.petctviewer.scintigraphy.scin.preferences.PrefsTabGastric;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GastricScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Gastric Scintigraphy";

	public GastricScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplicationWorkflow(selectedImages[0], getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflow_Gastric(this, (FenApplicationWorkflow) this.getFenApplication(), selectedImages,
											   "Gastric Scintigraphy"));
		this.getFenApplication().setVisible(true);
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
		List<ImageSelection> selection = new ArrayList<>();
		for (ImageSelection ims : openedImages) {
			if (ims.getImageOrientation() != Orientation.ANT_POST && ims.getImageOrientation() != Orientation.POST_ANT)
				throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST});
			selection.add(Library_Dicom.ensureAntPostFlipped(ims));
		}

		// Check images have same duration
		int tolerance = Math.max((int) Prefs.get(PrefsTabGastric.PREF_FRAME_DURATION_TOLERANCE, 1) * 1000, 1);
		int frameDuration = Library_Dicom.getFrameDuration(selection.get(0).getImagePlus());
		boolean frameDurationDifferent = false;
		float deltaSeconds = 0;
		for (int i = 1; i < selection.size(); i++) {
			int fDuration = Library_Dicom.getFrameDuration(selection.get(i).getImagePlus());
			if (frameDuration / tolerance != fDuration / tolerance) {
				frameDurationDifferent = true;
				deltaSeconds = Math.max(deltaSeconds,
										Math.abs((float) frameDuration / 1000f - (float) fDuration / 1000f));
			}
		}
		if (frameDurationDifferent) JOptionPane.showMessageDialog(this.getFenApplication(), "Frame durations are not" +
																		  " " +
																		  "identical for every image.\nMax delta is: " + deltaSeconds + " seconds.", "Frame durations different",
																  JOptionPane.WARNING_MESSAGE);


		// Close all images
		openedImages.forEach(ImageSelection::close);

		// Order images by time
		selection.sort(new ChronologicalAcquisitionComparator());

		return selection;
	}
}
