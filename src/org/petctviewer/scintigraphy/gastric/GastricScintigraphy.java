package org.petctviewer.scintigraphy.gastric;

import ij.Prefs;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabGastric;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GastricScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Gastric Scintigraphy";

	public GastricScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		// Create application window
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflow_Gastric(this, (FenApplicationWorkflow) this.getFenApplication(),
											   preparedImages.toArray(new ImageSelection[0]), STUDY_NAME));
		this.getFenApplication().setPreferences(new PrefTabGastric(null));
		this.getFenApplication().setVisible(true);
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException {
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
		// Find min and max durations
		int min = selection.stream().map(ims -> Library_Dicom.getFrameDuration(ims.getImagePlus())).min(
				Comparator.naturalOrder()).orElse(0);
		int max = selection.stream().map(ims -> Library_Dicom.getFrameDuration(ims.getImagePlus())).max(
				Comparator.naturalOrder()).orElse(0);
		// Delta
		int delta = Math.abs(max - min) / 1000;
		// Tolerance
		int tolerance = Math.max((int) Prefs.get(PrefTabGastric.PREF_FRAME_DURATION_TOLERANCE, 1), 1);
		if (delta > tolerance) {
			JOptionPane.showMessageDialog(this.getFenApplication(),
										  "Frame durations are not identical for every image" + ".\nMax delta is: " +
												  delta + " seconds.", "Frame durations different",
										  JOptionPane.WARNING_MESSAGE);
		}


		// Close all images
		openedImages.forEach(ImageSelection::close);

		// Order images by time
		selection.sort(new ChronologicalAcquisitionComparator());

		return selection;
	}

	@Override
	public String instructions() {
		return "Minimum 2 images in Ant-Post or Post-Ant orientation.";
	}
}
