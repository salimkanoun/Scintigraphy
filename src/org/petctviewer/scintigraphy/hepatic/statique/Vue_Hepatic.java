package org.petctviewer.scintigraphy.hepatic.statique;

import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;

public class Vue_Hepatic extends VueScin {

	public Vue_Hepatic() {
		super("Hepatic retention");
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}

		ImagePlus imp = images[0];
		String info = imp.getInfoProperty();
		ImagePlus impSorted = VueScin.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);

		return impSorted.duplicate();
	}

	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		this.getFenApplication().setControleur(new Controleur_Hepatic(this));
	}
}
