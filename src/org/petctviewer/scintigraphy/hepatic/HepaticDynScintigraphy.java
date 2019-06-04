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
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;

public class HepaticDynScintigraphy extends Scintigraphy {

	private int[] frameDurations;
	private ImageSelection impPost;
	private ImageSelection impProjeteePost;

	public HepaticDynScintigraphy() {
		super("Biliary scintigraphy");
	}

	/**
	 * Also prepare the images for the second exam.
	 */
	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException, ReadTagException {
		// Check number of images
		if (selectedImages.length != 1) throw new WrongNumberImagesException(selectedImages.length, 1);

		ImageSelection impSelect = selectedImages[0];
		ImageSelection impAnt;
		if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT) {
			impAnt = impSelect.clone();
		} else if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT_POST || selectedImages[0]
				.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
			ImageSelection[] imps = Library_Dicom.splitDynamicAntPost(impSelect);
			impAnt = imps[0];
			this.impPost = imps[1];
		} else {
			throw new WrongColumnException.OrientationColumn(selectedImages[0].getRow(),
					selectedImages[0].getImageOrientation(),
					new Orientation[]{Orientation.DYNAMIC_ANT, Orientation.DYNAMIC_ANT_POST,
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
		impProjeteeAnt = Library_Dicom
				.project(impProjeteeAnt, 0, impProjeteeAnt.getImagePlus().getStackSize(), "avg");

		if (this.impPost != null) {
			impProjeteePost = Library_Dicom.project(this.impPost, 0, impPost.getImagePlus().getStackSize(), "avg");
		}

		selectedImages[0].getImagePlus().close();

		this.frameDurations = Library_Dicom.buildFrameDurations(impAnt.getImagePlus());

		ImageSelection impAntNormalized = impAnt.clone();

		Library_Dicom.normalizeToCountPerSecond(impAntNormalized);

		impAntNormalized.getImagePlus().getProcessor()
				.setMinAndMax(0, impAntNormalized.getImagePlus().getStatistics().max * 1f / 1f);

		// In this array, the only used image is the first one, for the forst exam. All
		// th others are needed in the second exam, but we process it here to avoid a
		// second selection of the same image
		return new ImageSelection[]{impAntNormalized, impProjeteeAnt, impAnt, this.impPost,
		                            this.impProjeteePost};
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
				new ModelHepaticDynamic(selectedImages, this.getStudyName(), this.frameDurations)));
	}

}
