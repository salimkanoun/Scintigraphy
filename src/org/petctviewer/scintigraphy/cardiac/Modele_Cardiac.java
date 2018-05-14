package org.petctviewer.scintigraphy.cardiac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import ij.ImagePlus;

public class Modele_Cardiac extends ModeleScin {

	private HashMap<String, Double> data;

	/** Valeurs mesur�es **/
	// valeurs de la prise late
	private Double fixCoeurL, fixReinGL, fixReinDL, fixVessieL, fixBkgNoise;
	// valeurs des contamination
	private Double sumContE = 0.0, sumContL = 0.0;
	// valeurs totales
	private Double totEarly, totLate;

	/** Valeurs calcul�es **/
	// valeurs finales
	private Double finalEarly;
	private Double hwb, retCardiaque, retCe;

	private Boolean deuxPrises;

	private HashMap<String, String> resultats;

	public Modele_Cardiac(ImagePlus imp) {
		this.imp = (ImagePlus) imp.clone();
		this.resultats = new HashMap<String, String>();
		this.data = new HashMap<String, Double>();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		Double counts = ModeleScin.getCounts(imp);
		data.put(nomRoi, counts);
	}

	public void calculerResultats() {
		// on fait les moyennes geometriques de chaque ROI Late

		this.fixBkgNoise = moyGeom(data.get("Bkg noise A"), data.get("Bkg noise P"));
		this.fixCoeurL = moyGeom(data.get("Heart A"), data.get("Heart P"));
		this.fixReinGL = moyGeom(data.get("Kidney L A"), data.get("Kidney L P"));
		this.fixReinDL = moyGeom(data.get("Kidney R A"), data.get("Kidney R P"));
		this.fixVessieL = moyGeom(data.get("Bladder A"), data.get("Bladder P"));

		// on somme les moyennes geometriques des contaminations
		List<Double> contAntPost = new ArrayList<Double>();
		for (String s : this.data.keySet()) {
			if (s.startsWith("Cont")) {
				contAntPost.add(this.data.get(s));
				if (contAntPost.size() == 2) {
					Double moyCont = moyGeom(contAntPost.get(0), contAntPost.get(1));
					if (s.startsWith("ContE")) {
						this.sumContE += moyCont;
					} else {
						this.sumContL += moyCont;
					}
					contAntPost.clear();
				}
			}
		}

		if (this.deuxPrises) {
			this.finalEarly = this.totEarly - this.sumContE;
		}

		//calcul heart/whole body
		this.hwb = (this.fixCoeurL)
				/ (this.totLate - (this.fixReinDL + this.fixReinGL + this.fixVessieL + this.sumContL));

		// calcul des retentions
		if (this.deuxPrises) {
			this.imp.setSlice(1);
			long timeEarly = ModeleScin.getDateAcquisition(this.imp).getTime();
			this.imp.setSlice(2);
			long timeLate = ModeleScin.getDateAcquisition(this.imp).getTime();

			int delaySeconds = (int) (timeEarly - timeLate) / 1000;
			Double facDecroissance = this.getDecayFraction(delaySeconds, (int) (6.02 * 3600));

			this.retCardiaque = (this.fixCoeurL * facDecroissance) / this.finalEarly;
			this.retCe = ((this.totLate - (this.fixReinDL + this.fixVessieL + this.sumContL)) * facDecroissance)
					/ this.finalEarly;
		}

	}

	//renvoie la moyenne geometrique de la vue ant et post de la slice courante
	private Double getGlobalCountAvg() {
		this.imp.setRoi(0, 0, this.imp.getWidth() / 2, this.imp.getHeight());
		Double countAnt = ModeleScin.getCounts(this.imp);

		this.imp.setRoi(this.imp.getWidth() / 2, 0, this.imp.getWidth() / 2, this.imp.getHeight());
		Double countPost = ModeleScin.getCounts(this.imp);

		return moyGeom(countAnt, countPost);
	}

	public void calculerMoyGeomTotale() {
		this.imp.setSlice(1);
		if (this.deuxPrises) {
			this.totEarly = getGlobalCountAvg();
			this.imp.setSlice(2);
			this.totLate = getGlobalCountAvg();
		} else {
			this.totLate = getGlobalCountAvg();
		}

		this.imp.killRoi();
		this.imp.setSlice(1);
	}

	public void setDeuxPrise(Boolean b) {
		this.deuxPrises = b;
	}

	@Override
	public String toString() {
		String s = "";
		
		s += "Heart," + round(this.fixCoeurL,2) + "\n";
		s += "Left Kidney," + round(this.fixReinGL,2) + "\n";
		s += "Right Kidney," + round(this.fixReinDL, 2) + "\n";
		if(this.deuxPrises)
			s += "WB early (5mn)," + round(this.totEarly,2) + "\n";
		s += "WB late (3h)," + round(this.totLate,2) + "\n";
		s += "Bladder," + round(this.fixVessieL,2) + "\n";
		s += "Bkg noise," + round(this.fixBkgNoise,2) + "\n";
		if(this.deuxPrises)
			s += "WB retention %," + round(this.fixBkgNoise * 100,2) + "\n";
		s += "Ratio H/WB %," + round(this.hwb, 2) + "\n";
		if(this.deuxPrises)
			s += "Cardiac retention %," + round(this.retCardiaque * 100, 2) + "\n";

		return s;
	}

	@Override
	public HashMap<String, String> getResultsHashMap() {
		if (this.deuxPrises) {
			this.resultats.put("WB early (5mn)", "" + round(this.totEarly, 2));
			this.resultats.put("Cardiac retention %", "" + round(this.retCardiaque * 100, 2));
			this.resultats.put("WB retention %", "" + round(this.retCe * 100, 2));
		}

		this.resultats.put("WB late (3h)", "" + round(this.totLate, 2));

		this.resultats.put("Bladder", "" + round(this.fixVessieL, 2));
		this.resultats.put("Heart", "" + round(this.fixCoeurL, 2));
		this.resultats.put("Bkg noise", "" + round(this.fixBkgNoise, 2));
		this.resultats.put("Right Kidney", "" + round(this.fixReinDL, 2));
		this.resultats.put("Left Kidney", "" + round(this.fixReinGL, 2));

		this.resultats.put("Ratio H/WB %", "" + round(this.hwb * 100, 2));

		return this.resultats;
	}

}
