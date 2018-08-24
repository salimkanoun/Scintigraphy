package org.petctviewer.scintigraphy.hepatic.statique;

import java.util.HashMap;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

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
		Double counts = Library_Quantif.getCounts(imp);
		this.data.put(nomRoi, counts);
	}
	
	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> resultats = new HashMap<>();
		
		resultats.put("GM Liver", Library_Quantif.round(this.MGFoie, 2) + " (" + Library_Quantif.round((this.MGFoie / this.MGTot*100), 2) + "%)");
		resultats.put("GM Intestine", "" + Library_Quantif.round(this.MGIntes, 2));
		resultats.put("GM Total", "" + Library_Quantif.round(this.MGTot, 2));
		
		return resultats;
	}

	@Override
	public void calculerResultats() {
		this.MGIntes = Library_Quantif.moyGeom(this.data.get("Intestine A0"), this.data.get("Intestine P0"));
		this.MGFoie = Library_Quantif.moyGeom(this.data.get("Liver A0"), this.data.get("Liver P0"));
		this.MGTot = this.MGIntes + this.MGFoie;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "GM Liver," + Library_Quantif.round(this.MGFoie, 2) + " (" + Library_Quantif.round((this.MGFoie / this.MGTot*100), 2) + "%) \n";
		s += "GM Intestine," + Library_Quantif.round(this.MGIntes, 2) + "\n";
		s += "GM Total," + Library_Quantif.round(this.MGTot, 2) + "\n";		
		return s;
	}

}
