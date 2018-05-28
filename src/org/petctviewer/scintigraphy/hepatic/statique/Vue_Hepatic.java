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
	protected void ouvertureImage(String[] titresFenetres) {
		if(titresFenetres.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}
		
		ImagePlus imp = WindowManager.getImage(titresFenetres[0]);
		String info = imp.getInfoProperty();
		ImagePlus impSorted = VueScin.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);
		imp.close();
		
		this.setImp(impSorted.duplicate());
		
		VueScin.setCustomLut(this.getImp());		
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		this.getFenApplication().setControleur(new Controleur_Hepatic(this));		
		IJ.setTool(Toolbar.POLYGON);
	}

}
