package org.petctviewer.scintigraphy.CSV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ij.Prefs;

public class Modele_FollowUp_TabMain extends Modele_FollowUp{

	private XYSeriesCollection leftKidneyCollection;
	private XYSeriesCollection rightKidneyCollection;

	private HashMap<String, Double[][]> excretionsRatios ;
	

	
	public Modele_FollowUp_TabMain(ArrayList<String> chemins) {
		super(chemins);
		
		
		XYSeries [] leftKidneySerieTab = new XYSeries [chemins.size()];
		XYSeries [] rightKidneySerieTab = new XYSeries [chemins.size()];
		
		excretionsRatios = new HashMap<>();
		
		
		//for each csv content
		for(int i = 0; i<allLines.size(); i++) {
			
			//calcul date
			String datePatient = allLines.get(i).get(2).split(",")[1];	
			Date result = null;
			try {
				result = new SimpleDateFormat("yyyyMMdd").parse(datePatient);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			datePatient = new SimpleDateFormat(Prefs.get("dateformat.preferred", "MM/dd/yyyy")).format(result);
			
			
			//je recupere le temps (abcsisse) et les reins droite et gauche (ordonnee)
			String[] time = super.allLines.get(i).get(4).split(",");
			String[] leftKidney = super.allLines.get(i).get(5).split(",");
			String[] rightKidney = super.allLines.get(i).get(6).split(",");

			//stock tout les relevés droite et gauche pour chaque patient			
			XYSeries leftKidneySerie = new XYSeries(datePatient);
			for(int j = 1; j<leftKidney.length;j++) {
				leftKidneySerie.add(Double.parseDouble(time[j]),Double.parseDouble(leftKidney[j]));
			}		
			leftKidneySerieTab[i] = leftKidneySerie;
			
			
			XYSeries rightKidneySerie = new XYSeries(datePatient);
			for(int j = 1; j<rightKidney.length;j++) {
				rightKidneySerie.add(Double.parseDouble(time[j]),Double.parseDouble(rightKidney[j]));
			}		
			rightKidneySerieTab[i] = rightKidneySerie;
			
			
			//put all excretions ratio tab in a map
			excretionsRatios.put(datePatient,readExcretionRatio(i));
			
		}
		
		//LEFT KIDNEY collection graphic
		this.leftKidneyCollection = new XYSeriesCollection();
		for(int i =0; i<leftKidneySerieTab.length;i++) {
			leftKidneyCollection.addSeries(leftKidneySerieTab[i]);
		}
		
		//RIGHT KIDNEY collection graphic
		this.rightKidneyCollection = new XYSeriesCollection();
		for(int i =0; i<rightKidneySerieTab.length;i++) {
			rightKidneyCollection.addSeries(rightKidneySerieTab[i]);
		}	
	}
	

	/************ Public Methods ***********/

	public XYSeriesCollection getLeftKidneyCollection() {
		return this.leftKidneyCollection;
	}
	
	public XYSeriesCollection getRightKidneyCollection() {
		return this.rightKidneyCollection;
	}
	
	public String getNomPatient() {
		return super.allLines.get(0).get(0).split(",")[1];
	}
	
	public String getIDPatient() {
		return super.allLines.get(0).get(1).split(",")[1];
	}
	
	public HashMap<String, Double[][]> getExcretionsRatios() {
		return this.excretionsRatios;
	}




	
	
	
	
	
}
