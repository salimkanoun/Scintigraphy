package org.petctviewer.scintigraphy.renal.dmsa;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import ij.plugin.StackReverser;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DmsaScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "dmsa";

	public DmsaScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public String getName() {
		return STUDY_NAME;
	}

	@Override
	public void start(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus());
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.yellow);

		FenApplication fen = new FenApplicationWorkflow(selectedImages[0], this.getStudyName());
		this.setFenApplication(fen);
		selectedImages[0].getImagePlus().setOverlay(overlay);
		IJ.setTool(Toolbar.POLYGON);

		fen.setController(
				new ControllerWorkflowDMSA(this, (FenApplicationWorkflow) this.getFenApplication(), selectedImages));
	}

	@Override
	public Column[] getColumns() {
		return Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		if (selectedImages.size() > 1) throw new WrongNumberImagesException(selectedImages.size(), 1);

		ImageSelection imp = selectedImages.get(0).clone();

		if (selectedImages.get(0).getImageOrientation() == Orientation.ANT_POST) {
			imp.getImagePlus().getStack().getProcessor(1).flipHorizontal();
			// SK REVERSE DES METADATA A VERIFIER !!!!
			StackReverser reverser = new StackReverser();
			reverser.flipStack(imp.getImagePlus());
			imp.getImagePlus().getStack().getProcessor(2).flipHorizontal();
		} else if (selectedImages.get(0).getImageOrientation() == Orientation.POST_ANT) {
			imp.getImagePlus().getStack().getProcessor(2).flipHorizontal();
		} else if (selectedImages.get(0).getImageOrientation() == Orientation.POST) {
			// TODO: is this normal, having nothing in this case???
		} else {
			throw new WrongOrientationException(selectedImages.get(0).getImageOrientation(),
												new Orientation[]{Orientation.ANT_POST, Orientation.POST_ANT, Orientation.POST});
		}

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(imp);

		for(ImageSelection ims : selectedImages)
			ims.close();

		return selection;
	}

	@Override
	public String instructions() {
		return "Minimum 1 image in Ant-Post, Post-Ant or Post orientation";
	}

}