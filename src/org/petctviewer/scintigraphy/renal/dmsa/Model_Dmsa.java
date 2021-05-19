package org.petctviewer.scintigraphy.renal.dmsa;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.util.HashMap;

public class Model_Dmsa extends ModelScin {

	final HashMap<String, Double> data = new HashMap<>();
	final HashMap<String, Integer> areas = new HashMap<>();
	final double[] pct = new double[2];
	
	public Model_Dmsa(ImageSelection[] selectedImages, String studyName) {
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
	public void calculateResults() {
		
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
			double lkGM = Library_Quantif.moyGeom(lkP, lkA);
			double rkGM = Library_Quantif.moyGeom(rkP, rkA);
			
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
		StringBuilder s = new StringBuilder("\n");
	
		//ajoute tous les coups
		for(String k : this.data.keySet()) {
			s.append("count ").append(k).append(",").append(Library_Quantif.round(this.data.get(k), 2)).append("\n");
		}
		
		s.append("\n");
		
		//ajoute toutes les aires
		for(String k : this.areas.keySet()) {
			s.append("areas ").append(k).append(" (px),").append(this.areas.get(k)).append("\n");
		}
		
		s.append("\n");
		
		s.append("Excretion ratio Left Kidney,").append(Library_Quantif.round(this.pct[0], 2)).append("\n");
		s.append("Excretion ratio Right Kidney,").append(Library_Quantif.round(this.pct[1], 2)).append("\n");
		
		return s.toString();
	}

	public double[] getPct() {
		return pct;
	}

}
