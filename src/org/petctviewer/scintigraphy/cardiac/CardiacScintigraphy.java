package org.petctviewer.scintigraphy.cardiac;

import ij.IJ;
import ij.gui.Overlay;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;

public class CardiacScintigraphy extends Scintigraphy {

	public CardiacScintigraphy() {
		super("Cardiac");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		// Check number
		if(selectedImages.length > 2 || selectedImages.length < 1)
			throw new WrongNumberImagesException(selectedImages.length, 1, 2);

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
				throw new WrongColumnException.OrientationColumn(selectedImages[i].getRow(),
						selectedImages[i].getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
			}
			selectedImages[i].getImagePlus().close();
		}

		ImageSelection[] mountedSorted = new ImageSelection[mountedImages.size()];
		mountedImages.sort(new ChronologicalAcquisitionComparator());
		mountedSorted = mountedImages.toArray(mountedSorted);

		ImageSelection impStacked = mountedSorted[0].clone();
		// si la prise est early/late
		impStacked.setImagePlus(Library_Dicom.concatenate(mountedSorted, false));
		// si il y a plus de 3 minutes de diff�rence entre les deux prises
		if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
			IJ.log("Warning, frame duration differ by "
					+ Math.abs(frameDuration[0] - frameDuration[1]) / (1000 * 60) + " minutes");
		}

		return new ImageSelection[] { impStacked };
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 7);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);
		
		String[] infoOfAllImages = new String[selectedImages.length];
		for(int indexImage = 0 ; indexImage < selectedImages.length ; indexImage++)
			infoOfAllImages[indexImage] = selectedImages[indexImage].getImagePlus().duplicate().getInfoProperty();
		

		// fenetre de l'application
		this.setFenApplication(new FenApplication_Cardiac(selectedImages[0], this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);

		// Cree controller
		this.getFenApplication()
				.setController(new ControllerWorkflowCardiac(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Model_Cardiac(this, selectedImages, "Cardiac", infoOfAllImages)));

	}

}
