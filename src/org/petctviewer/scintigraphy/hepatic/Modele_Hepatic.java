package org.petctviewer.scintigraphy.hepatic;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;
import ij.gui.Roi;

public class Modele_Hepatic extends ModeleScin {

	private HashMap<String, Double> data = new HashMap<String, Double>();
	private Double MGFoie, MGIntes, MGTot;

	public Modele_Hepatic(ImagePlus imp) {
		this.imp = imp;
	}

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		Double counts = this.getCounts(imp);
		data.put(nomRoi, counts);
		System.out.println(nomRoi + " : " + counts);
	}

	@Override
	public void calculerResultats() {
		this.imp.setSlice(1);
		this.imp.setRoi(0, 0, this.imp.getWidth(), this.imp.getHeight());
		Double countAnt = this.getCounts(this.imp);
		this.imp.setSlice(2);
		this.imp.setRoi(0, 0, this.imp.getWidth(), this.imp.getHeight());
		Double countPost = this.getCounts(this.imp);		
		this.MGTot = ModeleScin.moyGeom(countAnt, countPost);
		
		this.MGIntes = ModeleScin.moyGeom(this.data.get("Intestine A0"), this.data.get("Intestine P0"));
		this.MGFoie = ModeleScin.moyGeom(this.data.get("Liver A0"), this.data.get("Liver P0"));
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "\nMG Foie : " + ModeleScin.round(this.MGFoie, 2) + " (" + ModeleScin.round((this.MGFoie / this.MGTot*100), 2) + "%)";
		s += "\nMG Intestin : " + ModeleScin.round(this.MGIntes, 2);
		s += "\nMG Total : " + ModeleScin.round(this.MGTot, 2);
		
		return s;
	}

	@Override
	public String[] getResultsAsArray() {
		return null;
	}

}
