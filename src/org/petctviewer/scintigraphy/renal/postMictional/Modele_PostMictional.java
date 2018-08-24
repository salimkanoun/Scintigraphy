package org.petctviewer.scintigraphy.renal.postMictional;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class Modele_PostMictional extends ModeleScin {

	private HashMap<String, Double> hm = new HashMap<>();
	
	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		hm.put(nomRoi, Library_Quantif.getCounts(imp));
	}

	@Override
	public void calculerResultats() {
		//pas de calcul
	}
	
	public HashMap<String, Double> getData() {
		return this.hm;
	}

}
