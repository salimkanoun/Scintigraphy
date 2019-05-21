package org.petctviewer.scintigraphy.cardiac;

import java.awt.Color;
import java.util.ArrayList;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Overlay;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class CardiacScintigraphy extends Scintigraphy {

	public CardiacScintigraphy() {
		super("Cardiac");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		ArrayList<ImageSelection> mountedImages = new ArrayList<>();

		int[] frameDuration = new int[2];

		for (int i = 0; i < selectedImages.length; i++) {

			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST
					|| selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				ImageSelection imp = selectedImages[i];
				String info = imp.getImagePlus().getInfoProperty();
				ImageSelection impReversed = Library_Dicom.ensureAntPostFlipped(imp);
				MontageMaker mm = new MontageMaker();
				ImageSelection montageImage = impReversed.clone();
				montageImage.setImagePlus(mm.makeMontage2(impReversed.getImagePlus(), 2, 1, 1.0, 1, 2, 1, 0, false));
				montageImage.getImagePlus().setProperty("Info", info);
				frameDuration[i] = Integer.parseInt(DicomTools.getTag(imp.getImagePlus(), "0018,1242").trim());
				mountedImages.add(montageImage);
			} else {
				new Exception("wrong input, need ant/post image");
			}
			selectedImages[i].getImagePlus().close();
		}

		ImageSelection[] mountedSorted = new ImageSelection[mountedImages.size()];
		mountedImages.sort(new ChronologicalAcquisitionComparator());
		mountedSorted = mountedImages.toArray(mountedSorted);

		ImageSelection impStacked = mountedSorted[0].clone();
		// si la prise est early/late
		if (selectedImages.length == 2) {
			impStacked.setImagePlus(Library_Dicom.concatenate(mountedSorted, false));
			// si il y a plus de 3 minutes de diff�rence entre les deux prises
			if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
				IJ.log("Warning, frame duration differ by "
						+ Math.abs(frameDuration[0] - frameDuration[1]) / (1000 * 60) + " minutes");
			}
		} else {
			impStacked = mountedSorted[0];
		}

		return new ImageSelection[] { impStacked };
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 7);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);

		// fenetre de l'application
		this.setFenApplication(new FenApplication_Cardiac(selectedImages[0], this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);

		// Cree controller
		((FenApplicationWorkflow) this.getFenApplication())
				.setControleur(new ControllerWorkflowCardiac(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Modele_Cardiac(this, selectedImages, "Cardiac")));

	}

}
