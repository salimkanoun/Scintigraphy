package org.petctviewer.scintigraphy.scin.basic;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class Modele_Basic extends ModeleScin {

	private HashMap<String, Double> hm = new HashMap<>();
	
	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		hm.put(nomRoi, ModeleScin.getCounts(imp));
	}

	@Override
	public void calculerResultats() {
		//pas de calcul
	}
	
	public HashMap<String, Double> getData() {
		return this.hm;
	}

}
