package org.petctviewer.scintigraphy.dynamic;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class GeneralDynamicScintigraphy extends DynamicScintigraphy{

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}

	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication_GeneralDyn(this.getImp(), this.getExamType(), this));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this));
	}
	
}
