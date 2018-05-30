package org.petctviewer.scintigraphy.dmsa;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class Modele_Dmsa extends ModeleScin {

	HashMap<String, Double> data = new HashMap<>();
	HashMap<String, Integer> areas = new HashMap<>();
	double[] pct = new double[2];
	
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
		double lkP = data.get("L. Kidney P0") - (areas.get("L. Kidney") * (data.get("L. bkg P0") / areas.get("L. bkg")));
		double rkP = data.get("R. Kidney P0") - (areas.get("R. Kidney") * (data.get("R. bkg P0") / areas.get("R. bkg")));
		
		if(data.get("L. Kidney A0") != null) {
			double lkA = data.get("L. Kidney A0") - (areas.get("L. Kidney") * (data.get("L. bkg A0") / areas.get("L. bkg")));
			double rkA = data.get("R. Kidney A0") - (areas.get("R. Kidney") * (data.get("R. bkg A0") / areas.get("R. bkg")));
			
			this.pct[0] = ModeleScin.moyGeom(lkP, lkA) / (ModeleScin.moyGeom(lkP, lkA) + ModeleScin.moyGeom(rkP, rkA));
			this.pct[1] = ModeleScin.moyGeom(rkP, rkA) / (ModeleScin.moyGeom(lkP, lkA) + ModeleScin.moyGeom(rkP, rkA));
		}else {
			this.pct[0] = lkP/(lkP+rkP);
			this.pct[1] = rkP/(lkP+rkP);
		}
		
		if(this.pct[0] < 0) {
			this.pct[0] = 0.0;
		}
		
		if(this.pct[1] < 0) {
			this.pct[1] = 0.0;
		}
	}
	
	public double[] getPct() {
		return pct;
	}

}
