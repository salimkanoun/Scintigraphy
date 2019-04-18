package org.petctviewer.scintigraphy.hepatic.statique;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.IJ;
import ij.ImagePlus;

public class HepaticScintigraphy extends Scintigraphy {

	public HepaticScintigraphy() {
		super("Hepatic retention");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] images) throws WrongInputException {
		if (images.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}

		ImagePlus imp = images[0].getImagePlus();
		String info = imp.getInfoProperty();
		ImagePlus impSorted = Library_Dicom.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);


		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(impSorted.duplicate(), null, null);
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication(selectedImages[0].getImagePlus(), this.getStudyName()));
		this.getFenApplication().setControleur(new Controleur_Hepatic(this, selectedImages, "Hepatic retention"));
	}
}
