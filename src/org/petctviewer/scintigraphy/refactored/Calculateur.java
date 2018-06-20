package org.petctviewer.scintigraphy.refactored;

import ij.ImagePlus;

public interface Calculateur {

	public abstract void afficherResultats();
	public abstract void enregistrerMesure(String nomRoi, ImagePlus imp);
	public abstract void calculerResultats();
	
}
