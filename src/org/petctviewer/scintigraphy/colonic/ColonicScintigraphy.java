package org.petctviewer.scintigraphy.colonic;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class ColonicScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Colonic Scintigraphy";

	public ColonicScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {

		this.setFenApplication(new FenApplicationColonicTransit(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowColonicTransit(this, (FenApplicationColonicTransit) this.getFenApplication(),
													 preparedImages.toArray(new ImageSelection[0])));
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException {
		// Check number
		if (openedImages.size() < 2 || openedImages.size() > 4) throw new WrongNumberImagesException(
				openedImages.size(), 2, 4);

		List<ImageSelection> impSelect = new ArrayList<>();
		for (ImageSelection openedImage : openedImages) {
			if (openedImage.getImageOrientation() == Orientation.ANT_POST ||
					openedImage.getImageOrientation() == Orientation.POST_ANT) {
				impSelect.add(Library_Dicom.ensureAntPostFlipped(openedImage));
			} else {
				throw new WrongColumnException.OrientationColumn(openedImage.getRow(),
																 openedImage.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST,
																				   Orientation.POST_ANT});
			}
			openedImage.getImagePlus().close();
		}

		// Order images by time
		impSelect.sort(new ChronologicalAcquisitionComparator());
		impSelect.get(0).getImagePlus().duplicate().show();
		return impSelect;
	}

	@Override
	public String instructions() {
		return "2 to 4 images. Ant-Post or Post-Ant orientations accepted.";
	}
}
