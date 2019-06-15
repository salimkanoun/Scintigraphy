package org.petctviewer.scintigraphy.gastric.liquid;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LiquidScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Liquid Scintigraphy";

	public LiquidScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new LiquidController(this, (FenApplicationWorkflow) this.getFenApplication(),
									 preparedImages.toArray(new ImageSelection[0])));
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException {
		// Check number of images
		if (openedImages.size() < 2) throw new WrongNumberImagesException(openedImages.size(), 2, Integer.MAX_VALUE);

		// Check orientation
		Orientation[] acceptedOrientations =
				new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.ANT};
		List<ImageSelection> selection = new ArrayList<>(openedImages.size());
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
		openedImages.forEach(ImageSelection::close);

		// Order images by time
		selection.sort(new ChronologicalAcquisitionComparator());

		return selection;
	}

	@Override
	public String instructions() {
		return "Minimum 2 images. Ant-Post / Post-Ant / Ant images accepted.";
	}
}
