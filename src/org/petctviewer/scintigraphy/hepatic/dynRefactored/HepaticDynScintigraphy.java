package org.petctviewer.scintigraphy.hepatic.dynRefactored;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;

public class HepaticDynScintigraphy extends Scintigraphy {

	private ImagePlus impAnt, impPost;
	private int[] frameDurations;

	public HepaticDynScintigraphy() {
		super("Biliary scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		if (selectedImages.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		ImageSelection impSorted = null;
		ImageSelection[] impsSortedAntPost = new ImageSelection[2];

		ImageSelection impSelect = selectedImages[0];
		if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT) {
			impSorted = impSelect.clone();
		} else if (selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_ANT_POST
				|| selectedImages[0].getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
			impSorted = Library_Dicom.splitDynamicAntPost(impSelect)[0];
		} else {
			throw new WrongInputException(
					"Unexpected Image type.\n Accepted : DYNAMIC_ANT | DYNAMIC_ANT_POST | DYNAMIC_POST_ANT");
		}

		impsSortedAntPost[0] = impSorted;
		selectedImages[0].getImagePlus().close();
		
		ImagePlus imp = impSorted.getImagePlus();

		this.frameDurations = Library_Dicom.buildFrameDurations(imp);

		Library_Dicom.normalizeToCountPerSecond(imp, frameDurations);

		imp.getProcessor().setMinAndMax(0, imp.getStatistics().max * 1f / 1f);
		
		impsSortedAntPost[1] = impSelect;

		return impsSortedAntPost;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);
		System.out.println("STUDY NAME DEPUIS SCINTI"+this.getStudyName());
		this.setFenApplication(new FenApplicationHepaticDynamic(selectedImages[0].getImagePlus(), this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);
		((FenApplicationHepaticDynamic)this.getFenApplication()).setControleur(new ControllerHepaticDynamic(this, this.getFenApplication(),
				new ModelHepaticDynamic(selectedImages, this.getStudyName(), this.frameDurations)));
	}

	public ImagePlus getImpAnt() {
		return impAnt;
	}

	public ImagePlus getImpPost() {
		return impPost;
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

}
