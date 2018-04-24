package org.petctviewer.scintigraphy.hepaticdyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class Modele_HepaticDyn extends ModeleScin {
	
	private List<Double> vasc, foieD, foieG;
	
	public Modele_HepaticDyn(ImagePlus imp) {
		this.imp = imp;
		this.vasc = new ArrayList<Double>();
		this.foieD = new ArrayList<Double>();
		this.foieG = new ArrayList<Double>();
	}

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		System.out.println(nomRoi);
		
		Double counts = this.getCounts(imp);
		if(nomRoi.contains("Blood Pool")) {
			vasc.add(counts);
		}else if(nomRoi.contains("Liver R")){
			foieD.add(counts);
		}else if(nomRoi.contains("Liver L")){
			foieG.add(counts);
		}
	}

	@Override
	public String[] getResultsAsArray() {
		return null;
	}

	@Override
	public void calculerResultats() {
		
	}

	@Override
	public HashMap<String, String> getResultsHashMap() {
		return null;
	}
	
	@Override
	public String toString() {
		return this.vasc.toString() + this.foieD.toString() + this.foieG.toString();
	}

}
