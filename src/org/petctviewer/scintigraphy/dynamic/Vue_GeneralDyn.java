package org.petctviewer.scintigraphy.dynamic;

import org.petctviewer.scintigraphy.scin.VueScinDyn;

import ij.IJ;
import ij.gui.Toolbar;

public class Vue_GeneralDyn extends VueScinDyn{

	public Vue_GeneralDyn() {
		super("Dynamic scintigraphy");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		super.ouvertureImage(titresFenetres);
		this.setFenApplication(new FenApplication_GeneralDyn(this.getImp(), this.getExamType()));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this));
		IJ.setTool(Toolbar.POLYGON);
	}
	
}
