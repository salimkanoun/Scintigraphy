package org.petctviewer.scintigraphy.hepatic;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class Modele_Hepatic extends ModeleScin {

	public Modele_Hepatic(ImagePlus imp) {
		this.imp = imp;
	}

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		
	}

	@Override
	public String[] getResultsAsArray() {
		return null;
	}

}
