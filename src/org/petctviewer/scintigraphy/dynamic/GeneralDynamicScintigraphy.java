package org.petctviewer.scintigraphy.dynamic;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;

import ij.ImagePlus;

public class GeneralDynamicScintigraphy extends DynamicScintigraphy{

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}
	
	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		super.preparerImp(images);
		//SK ICI CHOIX SI ANT OU POST pour retourner
		return impProjeteeAnt.duplicate();
	}
	

	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication_GeneralDyn(this.getImp(), this.getExamType(), this));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this));
	}
	
	
}
