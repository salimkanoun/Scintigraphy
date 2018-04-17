package org.petctviewer.scintigraphy.cardiac;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.scin.controleur.ControleurScin;
import org.petctviewer.scintigraphy.scin.modele.ModeleScin;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;

public class Modele_Cardiac extends ModeleScin {

	private HashMap<String, Double> results = new HashMap<String ,Double>();
	
	/** Valeurs mesurées **/
	//valeurs de la prise late
	private Double fixCoeurL, fixReinGL, fixReinDL, fixVessieL;
	//valeurs de la prise early
	private Double fixReinGE, fixReinDE;
	//valeurs des contamination
	private Double sumContE = 0.0, sumContL = 0.0;
	//valeurs totales
	private Double totEarly, totLate;
	
	/** Valeurs calculées **/
	//valeurs finales
	private Double finalEarly, finalLate;
	private Double hwb, retCardiaque, retCe;
	
	private Boolean deuxPrises;
	private ImagePlus imp2;

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		Double counts = this.getCounts(imp);
		results.put(nomRoi, counts);		
		System.out.println(nomRoi + " : " + counts);
	}

	public void afficherResultats() {
		
		this.calculerResultats();
		
		System.out.println("");
		
		System.out.println("Fixation Vessie: " + this.fixVessieL);
		System.out.println("Fixation fixReinG: " + this.fixReinGL);
		System.out.println("Fixation fixReinD: " + this.fixReinDL);
		System.out.println("Fixation fixCoeur: " + this.fixCoeurL);
		System.out.println("Fixation totale tardif: " + this.totLate);
		System.out.println("Fixation finale tardif: " + this.finalLate);
		System.out.println("Ratio H/WB: " + this.hwb);
		
		if (this.deuxPrises) {
			System.out.println("Fixation fixReinG precoce: " + this.fixReinGL);
			System.out.println("Fixation fixReinD precoce: " + this.fixReinDL);
			System.out.println("Fixation totale precoce: " + this.totEarly);
			System.out.println("Fixation finale precoce: " + this.finalEarly);
			System.out.println("Retention cardiaque: " + this.retCardiaque);
			System.out.println("Retention corps entier: " + this.retCe);
			
		}
	}
	
	private void calculerResultats() {
		//on fait les moyennes geometriques de chaque ROI Late
		this.fixCoeurL = moyGeom(results.get("Heart AL"), results.get("Heart PL"))
				- moyGeom(results.get("Bkg noise AL"), results.get("Bkg noise PL"));
		this.fixReinGL = moyGeom(results.get("Kidney L AL"), results.get("Kidney L PL"));
		this.fixReinDL = moyGeom(results.get("Kidney R AL"), results.get("Kidney R PL"));
		this.fixVessieL = moyGeom(results.get("Bladder AL"), results.get("Bladder PL"));
		
		//on fait les moyennes geometriques de chaque ROI Early si elles existent
		if(this.deuxPrises)	{
			this.fixReinGE = moyGeom(results.get("Left Liver AE"), results.get("Left Liver PE"));
			this.fixReinDE = moyGeom(results.get("Right Liver AE"), results.get("Right Liver PE"));
		}

		//on somme les moyennes geometriques des contaminations
		List<Double> contAntPost = new ArrayList<Double>();
		boolean early = true;
		for(String s : this.results.keySet()) {
			if(s.contains("Contamination")) {
				contAntPost.add(this.results.get(s));
				if(contAntPost.size() == 2) {
					Double moyCont = moyGeom(contAntPost.get(0), contAntPost.get(1));
					if(this.deuxPrises && early) {
						this.sumContE += moyCont;
					}else {
						this.sumContL += moyCont;
					}
					contAntPost.clear();					
				}
			} else {
				early = false;
			}
		}
		
		this.finalLate = this.totLate - this.sumContL;
		if(this.deuxPrises) {
			this.finalEarly = this.totEarly - this.sumContE;
		}
		
		this.hwb = (this.fixCoeurL) / (this.totLate - (this.fixReinDL + this.fixReinGL + this.fixVessieL + this.sumContL));
		
		//calcul des retentions
		if(this.deuxPrises) {
			imp2.setSlice(1);
			long timeEarly = ModeleScin.getDateAcquisition(imp2).getTime();
			imp2.setSlice(2);
			long timeLate = ModeleScin.getDateAcquisition(imp2).getTime();
			
			int delaySeconds =(int) (timeEarly - timeLate) / 1000;
			Double facDecroissance = this.getDecayFraction(delaySeconds, (int) (6.02 * 3600));
			
			this.retCardiaque = (this.fixCoeurL * facDecroissance) / this.finalEarly;
			this.retCe = ((this.totLate - (this.fixReinDL + this.fixVessieL + this.sumContL)) * facDecroissance) / this.finalEarly;
		}
		
	}
	
	private Double getGlobalCountAvg() {
		imp2.setRoi(0, 0, imp2.getWidth()/2, imp2.getHeight());
		Double countAnt = this.getCounts(imp2);
		System.out.println("CoutAnt = " + countAnt);
		
		imp2.setRoi(imp2.getWidth()/2, 0, imp2.getWidth()/2, imp2.getHeight());
		Double countPost = this.getCounts(imp2);
		System.out.println("CoutPost = " + countPost);
		
		return moyGeom(countAnt, countPost);	
	}
	
	public void calculerMoyGeomTotale(ImagePlus imp) {
		this.imp2 = new ImagePlus("", imp.getProcessor());
		
		imp2.setSlice(1);
		if(this.deuxPrises) {
			this.totEarly = getGlobalCountAvg();
			imp2.setSlice(2);
			this.totLate = getGlobalCountAvg();
		}else {
			this.totLate = getGlobalCountAvg();
		}
		
		imp2.killRoi();
		imp2.setSlice(1);
	}
	
	private double moyGeom(Double a, Double b) {
		return Math.sqrt(a * b);
	}
	
	public void setDeuxPrise(Boolean b) {
		this.deuxPrises = b;
	}
	
	private Double getCounts(ImagePlus imp) {
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		Analyzer.setMeasurement(Measurements.MEAN, true);
		Analyzer analyser = new Analyzer(imp);
		analyser.measure();
		ResultsTable density = Analyzer.getResultsTable();		
		return density.getValueAsDouble(ResultsTable.RAW_INTEGRATED_DENSITY, 0);
	}
}
