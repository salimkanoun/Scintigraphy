package org.petctviewer.scintigraphy.cardiac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.StaticMethod;

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
	
	private ImagePlus imp;

	private HashMap<String, String> resultats;

	public Modele_Cardiac(ImagePlus imp) {
		this.imp= (ImagePlus) imp.duplicate();
		this.resultats = new HashMap<>();
		this.data = new HashMap<>();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		Double counts = StaticMethod.getCounts(imp);
		this.data.put(nomRoi, counts);
	}

	@Override
	public void calculerResultats() {
		// on fait les moyennes geometriques de chaque ROI Late

		this.fixBkgNoise = StaticMethod.moyGeom(this.data.get("Bkg noise A"), this.data.get("Bkg noise P"));
		this.fixCoeurL = StaticMethod.moyGeom(this.data.get("Heart A"), this.data.get("Heart P"));
		this.fixReinGL = StaticMethod.moyGeom(this.data.get("Kidney L A"), this.data.get("Kidney L P"));
		this.fixReinDL = StaticMethod.moyGeom(this.data.get("Kidney R A"), this.data.get("Kidney R P"));
		this.fixVessieL = StaticMethod.moyGeom(this.data.get("Bladder A"), this.data.get("Bladder P"));

		// on somme les moyennes geometriques des contaminations
		List<Double> contAntPost = new ArrayList<>();
		for (String s : this.data.keySet()) {
			if (s.startsWith("Cont")) {
				contAntPost.add(this.data.get(s));
				if (contAntPost.size() == 2) {
					Double moyCont = StaticMethod.moyGeom(contAntPost.get(0), contAntPost.get(1));
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
			imp.setSlice(1);
			long timeEarly = StaticMethod.getDateAcquisition(imp).getTime();
			imp.setSlice(2);
			long timeLate = StaticMethod.getDateAcquisition(imp).getTime();

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
		Double countAnt = StaticMethod.getCounts(imp);

		imp.setRoi(imp.getWidth() / 2, 0, imp.getWidth() / 2, imp.getHeight());
		Double countPost = StaticMethod.getCounts(imp);

		return StaticMethod.moyGeom(countAnt, countPost);
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
		
		s += "Heart," + StaticMethod.round(this.fixCoeurL,2) + "\n";
		s += "Left Kidney," + StaticMethod.round(this.fixReinGL,2) + "\n";
		s += "Right Kidney," + StaticMethod.round(this.fixReinDL, 2) + "\n";
		if(this.deuxPrises)
			s += "WB early (5mn)," + StaticMethod.round(this.totEarly,2) + "\n";
		s += "WB late (3h)," + StaticMethod.round(this.totLate,2) + "\n";
		s += "Bladder," + StaticMethod.round(this.fixVessieL,2) + "\n";
		s += "Bkg noise," + StaticMethod.round(this.fixBkgNoise,2) + "\n";
		if(this.deuxPrises)
			s += "WB retention %," + StaticMethod.round(this.fixBkgNoise * 100,2) + "\n";
		s += "Ratio H/WB %," + StaticMethod.round(this.hwb, 2) + "\n";
		if(this.deuxPrises)
			s += "Cardiac retention %," + StaticMethod.round(this.retCardiaque * 100, 2) + "\n";

		return s;
	}

	public HashMap<String, String> getResultsHashMap() {
		if (this.deuxPrises) {
			this.resultats.put("WB early (5mn)", "" + StaticMethod.round(this.totEarly, 2));
			this.resultats.put("Cardiac retention %", "" + StaticMethod.round(this.retCardiaque * 100, 2));
			this.resultats.put("WB retention %", "" + StaticMethod.round(this.retCe * 100, 2));
		}

		this.resultats.put("WB late (3h)", "" + StaticMethod.round(this.totLate, 2));

		this.resultats.put("Bladder", "" + StaticMethod.round(this.fixVessieL, 2));
		this.resultats.put("Heart", "" + StaticMethod.round(this.fixCoeurL, 2));
		this.resultats.put("Bkg noise", "" + StaticMethod.round(this.fixBkgNoise, 2));
		this.resultats.put("Right Kidney", "" + StaticMethod.round(this.fixReinDL, 2));
		this.resultats.put("Left Kidney", "" + StaticMethod.round(this.fixReinGL, 2));
		
		this.resultats.put("Ratio H/WB %", "" + StaticMethod.round(this.hwb * 100, 2));

		return this.resultats;
	}

}
