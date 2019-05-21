package org.petctviewer.scintigraphy.renal.dmsa;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class Modele_Dmsa extends ModeleScin {

	HashMap<String, Double> data = new HashMap<>();
	HashMap<String, Integer> areas = new HashMap<>();
	double[] pct = new double[2];
	
	public Modele_Dmsa(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
	}

	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		System.out.println(nomRoi);
		data.put(nomRoi, Library_Quantif.getCounts(imp));

		int area = imp.getStatistics().pixelCount;
		String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));

		// si on n'a pas deja enregistre son aire, on l'ajoute a la hashmap
		if (this.areas.get(name) == null) {
			System.out.println("name : "+name+"  //   area : "+area);
			this.areas.put(name, area);
		}
	}

	@Override
	public void calculerResultats() {
		
		System.out.println("Datas : ");
		for(String s : data.keySet())
			System.out.println("\t"+s);
		//soustraction du bruit de fond
		double lkP = data.get("L. Kidney P0")
				- (areas.get("L. Kidney") * (data.get("L. Background P0") / areas.get("L. Background")));
		double rkP = data.get("R. Kidney P0")
				- (areas.get("R. Kidney") * (data.get("R. Background P0") / areas.get("R. Background")));
		data.put("L. Kidney Post adjusted", lkP);
		data.put("R. Kidney Post adjusted", rkP);

		//si il y a une prise ant, on fait les moyennes geometriques
		if (data.get("L. Kidney A0") != null) {
			double lkA = data.get("L. Kidney A0")
					- (areas.get("L. Kidney") * (data.get("L. Background A0") / areas.get("L. Background")));
			double rkA = data.get("R. Kidney A0")
					- (areas.get("R. Kidney") * (data.get("R. Background A0") / areas.get("R. Background")));

			//ajout des donnes dans la hashmap des coups
			data.put("L. Kidney Ant adjusted", lkA);
			data.put("R. Kidney Ant adjusted", rkA);
			
			//calcul des moyennes geometriques
			Double lkGM = Library_Quantif.moyGeom(lkP, lkA);
			Double rkGM = Library_Quantif.moyGeom(rkP, rkA);
			
			//ajout des donnes dans la hashmap des aires
			data.put("L. Kidney GM", lkGM);
			data.put("R. Kidney GM", rkGM);
			
			this.pct[0] =  Math.max(lkGM / (lkGM + rkGM), 0);
			this.pct[1] =  Math.max(rkGM / (lkGM + rkGM), 0);
		} else { //sinon on calcule avec les valeurs brutes
			this.pct[0] = Math.max(lkP / (lkP + rkP), 0);
			this.pct[1] = Math.max(rkP / (lkP + rkP), 0);
		}
	}
	
	@Override
	public String toString() {
		String s = "\n";
	
		//ajoute tous les coups
		for(String k : this.data.keySet()) {
			s += "count " + k + "," + this.data.get(k) + "\n";
		}
		
		s += "\n";
		
		//ajoute toutes les aires
		for(String k : this.areas.keySet()) {
			s += "areas " + k + " (px)," + this.areas.get(k) + "\n";
		}
		
		s += "\n";
		
		s += "Excretion ratio Left Kidney," + this.pct[0] + "\n";
		s += "Excretion ratio Right Kidney," + this.pct[1] + "\n";
		
		return s;
	}

	public double[] getPct() {
		return pct;
	}

}
