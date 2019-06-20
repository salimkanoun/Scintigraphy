package org.petctviewer.scintigraphy.renal.postMictional;

import ij.gui.Overlay;
import org.petctviewer.scintigraphy.renal.gui.TabPostMict;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PostMictional extends Scintigraphy {

	public static final String STUDY_NAME = "Post-mictional";
	private final TabPostMict resultFrame;

	public PostMictional(String[] organes, TabPostMict resultFrame) {
		super(STUDY_NAME);

		this.resultFrame = resultFrame;
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		// Check number of images
		if (selectedImages.size() != 1) throw new WrongNumberImagesException(selectedImages.size(), 1);

		ImageSelection impSorted = null;
		if (selectedImages.get(0).getImageOrientation() == Orientation.ANT_POST) {
			impSorted = Library_Dicom.ensureAntPostFlipped(selectedImages.get(0));

		} else if (selectedImages.get(0).getImageOrientation() == Orientation.POST_ANT) {
			impSorted = selectedImages.get(0).clone();

		} else if (selectedImages.get(0).getImageOrientation() == Orientation.POST) {
			impSorted = selectedImages.get(0).clone();
		}

//		selectedImages.get(0).close();

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(impSorted);
		return selection;
	}

	@Override
	public String instructions() {
		return "1 image in Ant-Post (or Post-Ant) or Post orientation";
	}

	public BufferedImage getCapture() {
		return null;
	}

	public TabPostMict getResultFrame() {
		return resultFrame;

	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		Overlay ov = Library_Gui.initOverlay(preparedImages.get(0).getImagePlus());

		FenApplicationWorkflow fen = new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName());
		fen.setVisible(true);
		this.setFenApplication(fen);
		preparedImages.get(0).getImagePlus().setOverlay(ov);
		this.getFenApplication().setController(
				new ControllerWorkflowPostMictional((FenApplicationWorkflow) this.getFenApplication(),
													new Model_PostMictional(
															preparedImages.toArray(new ImageSelection[0]),
															this.getStudyName()), this.resultFrame));
	}

}
