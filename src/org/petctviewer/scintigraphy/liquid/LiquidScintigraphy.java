package org.petctviewer.scintigraphy.liquid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

public class LiquidScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Liquid Scintigraphy";

	public LiquidScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new LiquidController((FenApplicationWorkflow) this.getFenApplication(),
									 preparedImages.toArray(new ImageSelection[0])));
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException,
			ReadTagException {
		// Check number of images
		if (openedImages.size() != 1) throw new WrongNumberImagesException(openedImages.size(), 1);

		// Check orientation
		Orientation[] acceptedOrientations =
				new Orientation[]{Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT, Orientation.DYNAMIC_ANT};
		List<ImageSelection> selection = new ArrayList<>(1);
		ImageSelection ims = openedImages.get(0);
		if (Arrays.stream(acceptedOrientations).noneMatch(o -> o == ims.getImageOrientation()))
			throw new WrongColumnException.OrientationColumn(ims.getRow(), ims.getImageOrientation(),
															 acceptedOrientations);

		// Get Ant image
		if (ims.getImageOrientation() == Orientation.DYNAMIC_ANT_POST ||
				ims.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) selection.add(
				Library_Dicom.splitAntPost(ims)[0]);
		else selection.add(ims.clone());

		// Close previous images
		openedImages.forEach(ImageSelection::close);

		return selection;
	}

	@Override
	public String instructions() {
		return "1 image. Dynamic Ant-Post / Dynamic Post-Ant / Dynamic Ant orientation accepted.";
	}
}
