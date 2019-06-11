package org.petctviewer.scintigraphy.hepatic;

import ij.IJ;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HepaticDynScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Biliary scintigraphy";
	private int[] frameDurations;
	private ImageSelection impPost;
	private ImageSelection impProjeteePost;

	public HepaticDynScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {

		selectedImages[0].getImagePlus().changes = false;

		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);
		this.setFenApplication(new FenApplicationHepaticDynamic(selectedImages[0].getImagePlus(),
																this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);
		this.getFenApplication().setController(new ControllerHepaticDynamic(this, this.getFenApplication(),
																			new ModelHepaticDynamic(selectedImages,
																									this.getStudyName(),
																									this.frameDurations)));
	}

	@Override
	public String getName() {
		return STUDY_NAME;
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return new FenSelectionDicom.Column[0];
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check number of images
		if (selectedImages.size() != 1) throw new WrongNumberImagesException(selectedImages.size(), 1);

		ImageSelection impSelect = selectedImages.get(0);
		ImageSelection impAnt;
		if (impSelect.getImageOrientation() == Orientation.DYNAMIC_ANT) {
			impAnt = impSelect.clone();
		} else if (impSelect.getImageOrientation() == Orientation.DYNAMIC_ANT_POST ||
				impSelect.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
			ImageSelection[] imps = Library_Dicom.splitDynamicAntPost(impSelect);
			impAnt = imps[0];
			this.impPost = imps[1];
		} else {
			throw new WrongColumnException.OrientationColumn(impSelect.getRow(), impSelect.getImageOrientation(),
															 new Orientation[]{Orientation.DYNAMIC_ANT,
																			   Orientation.DYNAMIC_ANT_POST,
																			   Orientation.DYNAMIC_POST_ANT});
		}

		IJ.run(impAnt.getImagePlus(), "32-bit", "");

		if (this.impPost != null) {
			IJ.run(this.impPost.getImagePlus(), "32-bit", "");
			for (int i = 1; i <= this.impPost.getImagePlus().getStackSize(); i++) {
				this.impPost.getImagePlus().getStack().getProcessor(i).flipHorizontal();
			}
		}

		ImageSelection impProjeteeAnt = impAnt.clone();
		Library_Dicom.normalizeToCountPerSecond(impProjeteeAnt);
		impProjeteeAnt = Library_Dicom.project(impProjeteeAnt, 0, impProjeteeAnt.getImagePlus().getStackSize(), "avg");

		if (this.impPost != null) {
			impProjeteePost = Library_Dicom.project(this.impPost, 0, impPost.getImagePlus().getStackSize(), "avg");
		}

		impSelect.getImagePlus().close();

		this.frameDurations = Library_Dicom.buildFrameDurations(impAnt.getImagePlus());

		ImageSelection impAntNormalized = impAnt.clone();

		Library_Dicom.normalizeToCountPerSecond(impAntNormalized);

		impAntNormalized.getImagePlus().getProcessor().setMinAndMax(0,
																	impAntNormalized.getImagePlus().getStatistics().max *
																			1f / 1f);

		// In this array, the only used image is the first one, for the forst exam. All
		// th others are needed in the second exam, but we process it here to avoid a
		// second selection of the same image
		List<ImageSelection> result = new ArrayList<>();
		result.add(impAntNormalized);
		result.add(impProjeteeAnt);
		result.add(impAnt);
		result.add(this.impPost);
		result.add(this.impProjeteePost);
		return result;
	}

	@Override
	public String instructions() {
		return "1 image in Ant, Ant-Post or Post-Ant orientation";
	}
}
