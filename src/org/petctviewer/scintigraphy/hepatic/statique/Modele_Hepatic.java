package org.petctviewer.scintigraphy.hepatic.statique;

import java.util.HashMap;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.StaticMethod;

import ij.ImagePlus;

public class Modele_Hepatic extends ModeleScin {

	private HashMap<String, Double> data = new HashMap<>();
	private Double MGFoie, MGIntes, MGTot;
	private ImagePlus imp;

	public Modele_Hepatic(ImagePlus imp) {
		this.imp=imp.duplicate();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		Double counts = StaticMethod.getCounts(imp);
		this.data.put(nomRoi, counts);
	}
	
	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> resultats = new HashMap<>();
		
		resultats.put("GM Liver", StaticMethod.round(this.MGFoie, 2) + " (" + StaticMethod.round((this.MGFoie / this.MGTot*100), 2) + "%)");
		resultats.put("GM Intestine", "" + StaticMethod.round(this.MGIntes, 2));
		resultats.put("GM Total", "" + StaticMethod.round(this.MGTot, 2));
		
		return resultats;
	}

	@Override
	public void calculerResultats() {
		this.MGIntes = StaticMethod.moyGeom(this.data.get("Intestine A0"), this.data.get("Intestine P0"));
		this.MGFoie = StaticMethod.moyGeom(this.data.get("Liver A0"), this.data.get("Liver P0"));
		this.MGTot = this.MGIntes + this.MGFoie;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "GM Liver," + StaticMethod.round(this.MGFoie, 2) + " (" + StaticMethod.round((this.MGFoie / this.MGTot*100), 2) + "%) \n";
		s += "GM Intestine," + StaticMethod.round(this.MGIntes, 2) + "\n";
		s += "GM Total," + StaticMethod.round(this.MGTot, 2) + "\n";		
		return s;
	}

}
