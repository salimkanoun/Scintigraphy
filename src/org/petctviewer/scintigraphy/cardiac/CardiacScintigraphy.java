package org.petctviewer.scintigraphy.cardiac;

import java.awt.Color;
import java.util.ArrayList;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class CardiacScintigraphy extends Scintigraphy {

	public CardiacScintigraphy() {
		super("Cardiac");
	}

	@Override
	protected ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws Exception {

		ArrayList<ImagePlus> mountedImages = new ArrayList<>();

		int[] frameDuration = new int[2];

		for (int i = 0; i < selectedImages.length; i++) {
			
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST || selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				ImagePlus imp = selectedImages[i].getImagePlus();
				String info = imp.getInfoProperty();
				ImagePlus impReversed = Library_Dicom.sortImageAntPost(imp);
				MontageMaker mm = new MontageMaker();
				ImagePlus montageImage = mm.makeMontage2(impReversed, 2, 1, 1.0, 1, 2, 1, 0, false);
				montageImage.setProperty("Info", info);
				frameDuration[i] = Integer.parseInt(DicomTools.getTag(imp, "0018,1242").trim());
				mountedImages.add(montageImage);
			} else {
				new Exception("wrong input, need ant/post image");
			}
			selectedImages[i].getImagePlus().close();
		}

		ImagePlus[] mountedSorted = Library_Dicom.orderImagesByAcquisitionTime(mountedImages);
		Concatenator enchainer = new Concatenator();

		ImagePlus impStacked;
		// si la prise est early/late
		if (selectedImages.length == 2) {
			impStacked = enchainer.concatenate(mountedSorted, false);
			// si il y a plus de 3 minutes de diff�rence entre les deux prises
			if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
				IJ.log("Warning, frame duration differ by "
						+ Math.abs(frameDuration[0] - frameDuration[1]) / (1000 * 60) + " minutes");
			}
		} else {
			impStacked = mountedSorted[0];
		}

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(impStacked.duplicate(), null, null);
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 7);
		Library_Gui.setOverlayDG(overlay, selectedImages[0].getImagePlus(), Color.YELLOW);
		
		// fenetre de l'application
		this.setFenApplication(new FenApplication_Cardiac(selectedImages[0].getImagePlus(), this.getExamType()));
		selectedImages[0].getImagePlus().setOverlay(overlay);
		
		//Cree controller
		Controleur_Cardiac ctrl = new Controleur_Cardiac(this, selectedImages, "Cardiac");
		this.getFenApplication().setControleur(ctrl);
		
		
	}

}
