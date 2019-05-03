package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.util.Arrays;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.ReversedChronologicalAcquisitionComparator;

public class DynGastricScintigraphy extends Scintigraphy {

	private Model_Gastric model;

	public DynGastricScintigraphy(Model_Gastric model) {
		super("Dynamic Gastric Scintigraphy");
		this.model = model;

		FenSelectionDicom fsd = new FenSelectionDicom(this.getStudyName(), this);
		fsd.setVisible(true);
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// Check number of images
		if (openedImages.length == 0)
			throw new WrongNumberImagesException(openedImages.length, 1, Integer.MAX_VALUE);

		// Check orientation
		Orientation[] acceptedOrientations = new Orientation[] { Orientation.DYNAMIC_ANT_POST,
				Orientation.DYNAMIC_POST_ANT, Orientation.DYNAMIC_ANT };
		ImageSelection[] selection = new ImageSelection[openedImages.length];
		for (int i = 0; i < openedImages.length; i++) {
			ImageSelection ims = openedImages[i];
			if (!Arrays.stream(acceptedOrientations).anyMatch(o -> o.equals(ims.getImageOrientation()))) {
				throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
						acceptedOrientations);
			}

			// Sort orientation to always have Ant
			if (ims.getImageOrientation() == Orientation.DYNAMIC_ANT_POST
					|| ims.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
				ImageSelection[] dyn = Library_Dicom.splitDynamicAntPost(ims);
				selection[i] = Library_Dicom.project(dyn[0], 1, 10, "sum");
			} else {
				selection[i] = ims.clone();
			}
		}

		// Close other images
		Arrays.stream(openedImages).forEach(ims -> ims.getImagePlus().close());

		// Order image by time (reversed)
		Arrays.parallelSort(selection, new ReversedChronologicalAcquisitionComparator());

		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(
				new FenApplication_DynGastric(selectedImages[0].getImagePlus(), "Dynamic Gastric Scintigraphy"));
		this.getFenApplication().setControleur(
				new ControllerWorkflow_DynGastric(this, this.getFenApplication(), this.model, selectedImages));
		this.getFenApplication().setVisible(true);
	}

}
