package org.petctviewer.scintigraphy.hepatic;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;
import ij.gui.Roi;

public class Modele_Hepatic extends ModeleScin {

	private HashMap<String, Double> data = new HashMap<String, Double>();
	private Double MGFoie, MGIntes, MGTot;

	public Modele_Hepatic(ImagePlus imp) {
		this.imp = (ImagePlus) imp.clone();;
	}

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		System.out.println(nomRoi);
		Double counts = this.getCounts(imp);
		data.put(nomRoi, counts);
	}
	
	@Override
	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> resultats = new HashMap<String, String>();
		
		resultats.put("MG Liver", ModeleScin.round(this.MGFoie, 2) + " (" + ModeleScin.round((this.MGFoie / this.MGTot*100), 2) + "%)");
		resultats.put("MG Intestine", "" + round(this.MGIntes, 2));
		resultats.put("MG Total", "" + round(this.MGTot, 2));
		
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
		s += "\nMG Foie : " + ModeleScin.round(this.MGFoie, 2) + " (" + ModeleScin.round((this.MGFoie / this.MGTot*100), 2) + "%)";
		s += "\nMG Intestin : " + ModeleScin.round(this.MGIntes, 2);
		s += "\nMG Total : " + ModeleScin.round(this.MGTot, 2);
		
		return s;
	}

}
