package org.petctviewer.scintigraphy.cardiac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class Modele_Cardiac extends ModeleScin {

	private HashMap<String, Double[]> data;

	/** Valeurs mesur�es **/
	// valeurs de la prise late
	private Double fixCoeurL, fixReinGL, fixReinDL, fixVessieL;
	//fixBkgNoise;
	// valeurs des contamination
	private Double sumContE = 0.0, sumContL = 0.0;
	// valeurs totales
	private Double totEarly, totLate;

	/** Valeurs calcul�es **/
	// valeurs finales
	private Double finalEarly;
	private Double hwb, retCardiaque, retCe;

	private Boolean deuxPrises;
	
	private ImagePlus imp;

	private HashMap<String, String> resultats;

	public Modele_Cardiac(ImagePlus imp) {
		this.imp= (ImagePlus) imp.duplicate();
		this.resultats = new HashMap<>();
		this.data = new HashMap<>();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		
		//Array of Double, in 0 raw count, in 1 average count, in 2 number of pixels
		Double[] counts=new Double[3];
		
		counts[0] =Library_Quantif.getCounts(imp);
		counts[1] =Library_Quantif.getAvgCounts(imp);
		counts[2] =(double) Library_Quantif.getPixelNumber(imp);
		
		this.data.put(nomRoi, counts);
		
	}

	@Override
	public void calculerResultats() {
		//ICI A FAIRE
		
		//Avg background value of ant and post images
		Double meanBdfAnt=this.data.get("Bkg noise A")[1];
		Double meanBdfPost=this.data.get("Bkg noise P")[1];
		
		//calculation of corrected heart uptake
		Double correctedHeartAnt=this.data.get("Heart A")[0]-(meanBdfAnt*this.data.get("Heart A")[2]);
		Double correctedHeartPost=this.data.get("Heart P")[0]-(meanBdfPost*this.data.get("Heart P")[2]);
		
		//Calculation of corrected Left Renal uptake
		Double correctedKLAnt=this.data.get("Kidney L A")[0]-(meanBdfAnt*this.data.get("Kidney L A")[2]);
		Double correctedKLPost=this.data.get("Kidney L P")[0]-(meanBdfPost*this.data.get("Kidney L P")[2]);
		
		//Calculation of corrected Right Renal uptake
		Double correctedKRAnt=this.data.get("Kidney R A")[0]-(meanBdfAnt*this.data.get("Kidney R A")[2]);
		Double correctedKRPost=this.data.get("Kidney R P")[0]-(meanBdfPost*this.data.get("Kidney R P")[2]);
		
		//Calculation of corrected Blader uptake
		Double correctedBladAnt=this.data.get("Bladder A")[0]-(meanBdfAnt*this.data.get("Bladder A")[2]);
		Double correctedBladPost=this.data.get("Bladder P")[0]-(meanBdfPost*this.data.get("Bladder P")[2]);
		
		
		
		// on fait les moyennes geometriques de chaque ROI Late

		//SK DEVRAIT SAUVER QUE LA MOYENNE
		//this.fixBkgNoise = Library_Quantif.moyGeom(this.data.get("Bkg noise A"), this.data.get("Bkg noise P"));
		
		this.fixCoeurL = Library_Quantif.moyGeom(correctedHeartAnt, correctedHeartPost);
		this.fixReinGL = Library_Quantif.moyGeom(correctedKLAnt, correctedKLPost);
		this.fixReinDL = Library_Quantif.moyGeom(correctedKRAnt, correctedKRPost);
		this.fixVessieL = Library_Quantif.moyGeom(correctedBladAnt, correctedBladPost);

		// on somme les moyennes geometriques des contaminations
		//SK RISQUE DE MELANGER ANT ET POST DE DEUX CONTAMINATION DIFFERENTES !
		//List<Double> contAntPost = new ArrayList<>();
		
		HashMap<Integer, Double[]> contE = new HashMap<>();
		HashMap<Integer, Double[]> contL = new HashMap<>();
		
		for (String s : this.data.keySet()) {
			if (s.startsWith("Cont")) {
				String label=s.substring(s.indexOf(" ")+2);
				int number=Integer.parseInt(label);
				System.out.println(s+"_"+number);
				
				if (s.startsWith("ContE")) {
					if(!contE.containsKey(number)) {
						contE.put(number, new Double[2]);
					}
					if(s.contains("A")) {
						contE.get(number)[0]=this.data.get(s)[0];
					}else if (s.contains("P")) {
						contE.get(number)[1]=this.data.get(s)[0];
						
					}
					
					
					
				} else if (s.startsWith("ContL")) {
					if(!contL.containsKey(number)) {
						contL.put(number, new Double[2]);
					}
					if(s.contains("A")) {
						contL.get(number)[0]=this.data.get(s)[0];
					}else if (s.contains("P")) {
						contL.get(number)[1]=this.data.get(s)[0];
						
					}
					
					
				}
			}
		}
		
		for (Integer i : contE.keySet()) {
			this.sumContE += Library_Quantif.moyGeom(contE.get(i)[0], contE.get(i)[1]);
		}
		
		for (Integer i : contL.keySet()) {
			this.sumContL += Library_Quantif.moyGeom(contL.get(i)[0], contL.get(i)[1]);
		}
		
		
		/*//SK A RETIRER QUAND LA METHODE AU DESSUS SERA OK
		for (String s : this.data.keySet()) {
			if (s.startsWith("Cont")) {
				contAntPost.add(this.data.get(s)[0]);
				if (contAntPost.size() == 2) {
					Double moyCont = Library_Quantif.moyGeom(contAntPost.get(0), contAntPost.get(1));
					if (s.startsWith("ContE")) {
						this.sumContE += moyCont;
					} else {
						this.sumContL += moyCont;
					}
					contAntPost.clear();
				}
			}
		}*/

		//calcul heart/whole body
		this.hwb = (this.fixCoeurL)
				/ (this.totLate - (this.fixReinDL + this.fixReinGL + this.fixVessieL + this.sumContL));

		// calcul des retentions
		if (this.deuxPrises) {
			
			this.finalEarly = this.totEarly - this.sumContE;
			
			imp.setSlice(1);
			long timeEarly = Library_Dicom.getDateAcquisition(imp).getTime();
			imp.setSlice(2);
			long timeLate = Library_Dicom.getDateAcquisition(imp).getTime();

			int delaySeconds = (int) (timeEarly - timeLate) / 1000;
			Double facDecroissance = ModeleScin.getDecayFraction(delaySeconds, (int) (6.02 * 3600));

			this.retCardiaque = (this.fixCoeurL * facDecroissance) / this.finalEarly;
			this.retCe = ((this.totLate - (this.fixReinDL + this.fixVessieL + this.sumContL)) * facDecroissance)
					/ this.finalEarly;
		}

	}

	//renvoie la moyenne geometrique de la vue ant et post de la slice courante
	private Double getGlobalCountAvg() {
		imp.setRoi(0, 0, imp.getWidth() / 2, imp.getHeight());
		Double countAnt = Library_Quantif.getCounts(imp);

		imp.setRoi(imp.getWidth() / 2, 0, imp.getWidth() / 2, imp.getHeight());
		Double countPost = Library_Quantif.getCounts(imp);

		return Library_Quantif.moyGeom(countAnt, countPost);
	}

	public void calculerMoyGeomTotale() {
		imp.setSlice(1);
		if (this.deuxPrises) {
			this.totEarly = getGlobalCountAvg();
			imp.setSlice(2);
			this.totLate = getGlobalCountAvg();
		} else {
			this.totLate = getGlobalCountAvg();
		}

		imp.killRoi();
		imp.setSlice(1);
	}

	public void setDeuxPrise(Boolean b) {
		this.deuxPrises = b;
	}

	@Override
	public String toString() {
		String s = "";
		
		s += "Heart," + Library_Quantif.round(this.fixCoeurL,2) + "\n";
		s += "Left Kidney," + Library_Quantif.round(this.fixReinGL,2) + "\n";
		s += "Right Kidney," + Library_Quantif.round(this.fixReinDL, 2) + "\n";
		if(this.deuxPrises)
			s += "WB early (5mn)," + Library_Quantif.round(this.totEarly,2) + "\n";
		s += "WB late (3h)," + Library_Quantif.round(this.totLate,2) + "\n";
		s += "Bladder," + Library_Quantif.round(this.fixVessieL,2) + "\n";
		//SK A VOIR
		//s += "Bkg noise," + Library_Quantif.round(this.fixBkgNoise,2) + "\n";
		s += "Ratio H/WB %," + Library_Quantif.round(this.hwb, 2) + "\n";
		if(this.deuxPrises) {
			s += "Cardiac retention %," + Library_Quantif.round(this.retCardiaque * 100, 2) + "\n";
			s += "WB retention %," + Library_Quantif.round(this.retCe * 100,2) + "\n";
			
		}
	
		

		return s;
	}

	public HashMap<String, String> getResultsHashMap() {
		if (this.deuxPrises) {
			this.resultats.put("WB early (5mn)", "" + Library_Quantif.round(this.totEarly, 2));
			this.resultats.put("Cardiac retention %", "" + Library_Quantif.round(this.retCardiaque * 100, 2));
			this.resultats.put("WB retention %", "" + Library_Quantif.round(this.retCe * 100, 2));
		}

		this.resultats.put("WB late (3h)", "" + Library_Quantif.round(this.totLate, 2));

		this.resultats.put("Bladder", "" + Library_Quantif.round(this.fixVessieL, 2));
		this.resultats.put("Heart", "" + Library_Quantif.round(this.fixCoeurL, 2));
		//SK A VOIR
		//this.resultats.put("Bkg noise", "" + Library_Quantif.round(this.fixBkgNoise, 2));
		this.resultats.put("Right Kidney", "" + Library_Quantif.round(this.fixReinDL, 2));
		this.resultats.put("Left Kidney", "" + Library_Quantif.round(this.fixReinGL, 2));
		
		this.resultats.put("Ratio H/WB %", "" + Library_Quantif.round(this.hwb * 100, 2));

		return this.resultats;
	}

}
