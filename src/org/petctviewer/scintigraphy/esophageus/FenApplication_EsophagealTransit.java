package org.petctviewer.scintigraphy.esophageus;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.ImagePlus;

public class FenApplication_EsophagealTransit extends FenApplication{
 
	private static final long serialVersionUID = 1L;

	public FenApplication_EsophagealTransit(ImagePlus imp) {
		super(imp,"Eso");
		
		Controleur_EsophagealTransit cet = new Controleur_EsophagealTransit(this);
		this.setControleur(cet);
	}



	

}
