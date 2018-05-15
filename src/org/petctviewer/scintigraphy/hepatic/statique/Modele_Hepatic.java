package org.petctviewer.scintigraphy.hepatic.statique;

import java.util.HashMap;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import ij.ImagePlus;

public class Modele_Hepatic extends ModeleScin {

	private HashMap<String, Double> data = new HashMap<>();
	private Double MGFoie, MGIntes, MGTot;

	public Modele_Hepatic(ImagePlus imp) {
		this.imp = (ImagePlus) imp.clone();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		Double counts = ModeleScin.getCounts(imp);
		this.data.put(nomRoi, counts);
	}
	
	@Override
	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> resultats = new HashMap<>();
		
		resultats.put("GM Liver", ModeleScin.round(this.MGFoie, 2) + " (" + ModeleScin.round((this.MGFoie / this.MGTot*100), 2) + "%)");
		resultats.put("GM Intestine", "" + round(this.MGIntes, 2));
		resultats.put("GM Total", "" + round(this.MGTot, 2));
		
		return resultats;
	}

	@Override
	public void calculerResultats() {
		this.MGIntes = ModeleScin.moyGeom(this.data.get("Intestine A0"), this.data.get("Intestine P0"));
		this.MGFoie = ModeleScin.moyGeom(this.data.get("Liver A0"), this.data.get("Liver P0"));
		this.MGTot = this.MGIntes + this.MGFoie;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "GM Liver," + ModeleScin.round(this.MGFoie, 2) + " (" + ModeleScin.round((this.MGFoie / this.MGTot*100), 2) + "%) \n";
		s += "GM Intestine," + ModeleScin.round(this.MGIntes, 2) + "\n";
		s += "GM Total," + ModeleScin.round(this.MGTot, 2) + "\n";		
		return s;
	}

}
