package org.petctviewer.scintigraphy.CSV;

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
	
	//liste de tous les contenu des examens
	protected ArrayList<ArrayList<String>> allLines;
	
	public Modele_FollowUp(ArrayList<String> chemins)  {
		
		allLines = new ArrayList<>();
		for(int i = 0; i<chemins.size(); i++) {
			//contenu de un examen
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
	
	}
	
	//to read excretion ratio
	protected Double[][] readExcretionRatio(int indiceExamen) {
		Double[][] excr = new Double[3][3];
		int ligneDansCsv = 13;
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
