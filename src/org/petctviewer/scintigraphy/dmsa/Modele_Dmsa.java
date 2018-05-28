package org.petctviewer.scintigraphy.dmsa;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class Modele_Dmsa extends ModeleScin {

	HashMap<String, Double> data = new HashMap<String, Double>();
	HashMap<String, Integer> areas = new HashMap<String, Integer>();
	double pct = 0.0;
	
	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		data.put(nomRoi, ModeleScin.getCounts(imp));
		
		int area = imp.getStatistics().pixelCount;
		String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));

		// si on n'a pas deja enregistre son aire, on l'ajout a la hashmap
		if (this.areas.get(name) == null) {
			this.areas.put(name, area);
		}
	}

	@Override
	public void calculerResultats() {
		double lkP = data.get("L. Kidney P0") - areas.get("L. Kidney") * (data.get("L. bkg P0") / areas.get("L. bkg"));
		double rkP = data.get("R. Kidney P0") - areas.get("R. Kidney") * (data.get("R. bkg P0") / areas.get("L. bkg"));
		
		if(data.get("L. Kidney A0") != null) {
			double lkA = data.get("L. Kidney A0") - areas.get("L. Kidney") * (data.get("L. bkg A0") / areas.get("L. bkg"));
			double rkA = data.get("R. Kidney A0") - areas.get("R. Kidney") * (data.get("R. bkg A0") / areas.get("L. bkg"));
			this.pct = ModeleScin.moyGeom(lkP, lkA) / (ModeleScin.moyGeom(lkP, lkA) + ModeleScin.moyGeom(rkP, rkA));
		}else {
			this.pct = lkP/(lkP+rkP);
		}
	}
	
	public double getPct() {
		return pct;
	}

}
