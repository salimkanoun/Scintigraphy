package org.petctviewer.scintigraphy.liver;

import org.petctviewer.scintigraphy.hepatic.statique.Controleur_Hepatic;
import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class Vue_Liver extends VueScin {

	public Vue_Liver() {
		super("Liver");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		ImagePlus imp = this.splitAntPost(titresFenetres)[1];
		this.setImp(imp);
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_Liver(this));		
		IJ.setTool(Toolbar.POLYGON);
	}

}
