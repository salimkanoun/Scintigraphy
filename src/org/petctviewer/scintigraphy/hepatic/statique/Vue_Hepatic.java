package org.petctviewer.scintigraphy.hepatic.statique;

import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class Vue_Hepatic extends VueScin {

	public Vue_Hepatic() {
		super("Hepatic retention");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		if(titresFenetres.length > 1) {
			IJ.log("There must be only one dicom opened");
		}
		
		ImagePlus imp = WindowManager.getImage(titresFenetres[0]);
		String info = imp.getInfoProperty();
		ImagePlus impSorted = VueScin.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);
		imp.close();
		
		this.setImp(impSorted);
		
		VueScin.setCustomLut(this.getImp());		
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_Hepatic(this));		
		IJ.setTool(Toolbar.POLYGON);
	}

}
