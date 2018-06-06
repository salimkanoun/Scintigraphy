package org.petctviewer.scintigraphy.dynamic;

import org.petctviewer.scintigraphy.scin.VueScinDyn;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class Vue_GeneralDyn extends VueScinDyn{

	public Vue_GeneralDyn() {
		super("Dynamic scintigraphy");
	}

	@Override
	protected ImagePlus preparerImp(String[] titresFenetres) {
		return super.preparerImp(titresFenetres);
	}

	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication_GeneralDyn(this.getImp(), this.getExamType()));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this));
	}
	
}
