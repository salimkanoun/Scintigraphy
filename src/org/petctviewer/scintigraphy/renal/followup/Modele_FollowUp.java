package org.petctviewer.scintigraphy.renal.followup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

public abstract class Modele_FollowUp {
	
	private ArrayList<ArrayList<String>> allLines ;

	
	public Modele_FollowUp(ArrayList<String> chemins)  {
		
		allLines = readAllCsvContent(chemins);
	
		HashMap<String, HashMap<String, Double[][ ]>>tableaux = new HashMap();		
		for(int i =0; i< allLines.size(); i++) {
			//map contenant tout les tableaux pour un seul examen
			HashMap<String, Double[][]> unExamen = new HashMap<>();
			unExamen.put("nora", readNora(i));
			unExamen.put("excretion",readExcretionRatio(i));
			unExamen.put("timing",readTiming(i));
			unExamen.put("roe", readROE(i));
			unExamen.put("integral",readIntegral(i));
			tableaux.put(getDateExamen(i), unExamen);
		}
	}
	
	/*
	 * A partir d'une liste de chemins (path) de fichier,
	 * la methode lis leurs contenu
	 */
	private ArrayList<ArrayList<String>>  readAllCsvContent(ArrayList<String> chemins) {
		ArrayList<ArrayList<String>> allLines = new ArrayList<>();
		// for each path
		for(int i = 0; i<chemins.size(); i++) {
			//contenu d'une ligne
			ArrayList<String >lines = null ;
			//to read CSV file
			try {
				File file = new File(chemins.get(i));
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				
				//add CSV file content in lines
				lines = new ArrayList<>();
				for(String line = br.readLine(); line != null; line = br.readLine()) {
					lines.add(line);
				}
				br.close();
				fr.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			allLines.add( lines);
		}
		return allLines;
	}
	
	//to read excretion ratio
	protected Double[][] readExcretionRatio(int indiceExamen) {
		Double[][] excr = new Double[3][3];
		int ligneDansCsv = rechercheLineContains(indiceExamen, "Excretion ratio");
		int colonneDansCsv = 1;
		for(int i = 0; i< 3; i++) {
			for(int j = 0; j<3; j++) {
				if(!allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null"))
					excr[i][j] = Double.parseDouble(allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
				else
					excr[i][j] = null;
			}	
		}
		return excr;
	}
	
	//search a line where first apparition of word "mot"
	protected int rechercheLineContains(int indiceExamen, String mot) {
		for(int i =0; i< allLines.get(indiceExamen).size(); i++) {
			if(allLines.get(indiceExamen).get(i).contains(mot)) {
				return i;
			}
		}
		return 0;
	}
	
	
	/************ Public Methods tab main ***********/

	public XYSeriesCollection getLeftKidneyCollection() {
		XYSeriesCollection leftKidneyCollection = new XYSeriesCollection();		
		// for each csv
		for(int i = 0; i<allLines.size(); i++) {
			//je recupere le temps (abcsisse) et rein gauche (ordonnee)			
			XYSeries leftKidneySerie = new XYSeries(getDateExamen(i));
			//for each cols (j) in line 4 (time) and 5 (left kidney) 
			for(int j =1; j < this.allLines.get(i).get(4).split(",").length; j++) {
				leftKidneySerie.add(Double.parseDouble(this.allLines.get(i).get(4).split(",")[j]),Double.parseDouble( this.allLines.get(i).get(5).split(",")[j]));
			}	
			leftKidneyCollection.addSeries(leftKidneySerie);
		}
		return leftKidneyCollection;
	}
	
	public XYSeriesCollection getRightKidneyCollection() {
		XYSeriesCollection rightKidneyCollection = new XYSeriesCollection();
		//for each csv
		for(int i = 0; i<allLines.size(); i++) {
			//je recupere le temps (abcsisse) et  rein droite  (ordonnee)
			XYSeries rightKidneySerie = new XYSeries(getDateExamen(i));
			for(int j = 1; j<this.allLines.get(i).get(4).split(",").length;j++) {
				rightKidneySerie.add(Double.parseDouble(this.allLines.get(i).get(4).split(",")[j]),Double.parseDouble(this.allLines.get(i).get(6).split(",")[j]));
			}	
			rightKidneyCollection.addSeries(rightKidneySerie);
		}
		return rightKidneyCollection;
	}
	
	public String getNomPatient() {
		return this.allLines.get(0).get(0).split(",")[1];
	}
	
	public String getIDPatient() {
		return this.allLines.get(0).get(1).split(",")[1];
	}
	
	public HashMap<String, Double[][]> getExcretionsRatios() {
		return this.excretionsRatios;
	}

	/************ Public Methods tab details ***********/

	private Double[][] readNora(int indiceExamen){
		Double[][] nora = new Double[3][3];
		int ligneDansCsv = rechercheLineContains(indiceExamen, "NORA");
		int colonneDansCsv = 1;
		for(int i =0; i< 3; i++) {
			for(int j =0 ; j<3 ;j++) {
				if(!this.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null"))
					nora[i][j] = Double.parseDouble(this.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
				else
					nora[i][j] = null;
			}
		}
		return nora;
	}
	
	private Double[][] readTiming(int indiceExamen){
		Double[][] timing = new Double[2][2];
		int ligneDansCsv = rechercheLineContains(indiceExamen, "Timing tmax");
		int colonneDansCsv = 2;
		for(int i =0; i< 2; i++) {
			for(int j =0 ; j<2 ;j++) {
				if(!this.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null"))
					timing[i][j] = Double.parseDouble(this.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
				else
					timing[i][j] = null;
			}
		}
		return timing;
	}

	private Double[][] readROE(int indiceExamen){
		Double[][] roe = new Double[3][3];
		int ligneDansCsv = rechercheLineContains(indiceExamen, "Time ROE (min)");
		int colonneDansCsv = 1;
		for(int i =0; i< 3; i++) {
			for(int j =0 ; j<3 ;j++) {
				if(!this.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null")) 
					roe[i][j] = Double.parseDouble(this.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
				else
					roe[i][j] = null;
			}
		}
		return roe;
	}
	
	private Double[][] readIntegral(int indiceExamen){
		Double[][] roe = new Double[1][2];
		int ligneDansCsv = rechercheLineContains(indiceExamen, "Separated function integral");
		int colonneDansCsv = 2;
			for(int j =0 ; j<2 ;j++) {
				if(!this.allLines.get(indiceExamen).get(ligneDansCsv).split(",")[j+colonneDansCsv].equals("null")) 
					roe[0][j] = Double.parseDouble(this.allLines.get(indiceExamen).get(ligneDansCsv).split(",")[j+colonneDansCsv]);
				else
					roe[0][j] = null;
			}
		return roe;
	}
	
	private String getDateExamen(int indiceExamen){
		String datePatient = allLines.get(indiceExamen).get(2).split(",")[1];	
		Date result = null;
		try {
			result = new SimpleDateFormat("yyyyMMdd").parse(datePatient);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		datePatient = new SimpleDateFormat(Prefs.get("dateformat.preferred", "MM/dd/yyyy")).format(result);
		
		return datePatient;
	}

	public ArrayList<HashMap<String, Double[][]>> getTableaux(){	
		return this.tableaux;
	}

	
	
	/************ Private Methods ***********/
	//to print CSV content in console
 	private static void printCSVContent(String chemin) throws IOException {
		File file = new File(chemin);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		System.out.println("******** Contenu du fichier**********");
		for(String line = br.readLine(); line != null; line = br.readLine()) {
			System.out.println(line);	
		}
		System.out.println("******** Fin du ontenu du fichier**********");		
		
		br.close();
		fr.close();
	}

	
	// à supprimer
	private static void printDoubleTab2Dim(Double[][] d) {
		for(int i=0;i<d.length;i++) {
			for(int j=0;j<d[i].length;j++) {
				System.out.print(d[i][0]+ " ");
			}
			System.out.println();
		}
	}
}
