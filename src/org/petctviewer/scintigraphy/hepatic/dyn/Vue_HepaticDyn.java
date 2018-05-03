package org.petctviewer.scintigraphy.hepatic.dyn;

import org.petctviewer.scintigraphy.dynamic.Vue_Dynamic;
import org.petctviewer.scintigraphy.scin.FenApplication;
import ij.IJ;
import ij.gui.Toolbar;

public class Vue_HepaticDyn extends Vue_Dynamic {

	public Vue_HepaticDyn() {
		super("Biliary scintigraphy");
	}
	
	@Override
	public void ouvertureImage(String[] titresFenetres) {
		super.ouvertureImage(titresFenetres);
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_HepaticDyn(this));
		IJ.setTool(Toolbar.RECT_ROI);
	}

}
