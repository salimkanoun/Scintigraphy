package org.petctviewer.scintigraphy.CSV;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ij.Prefs;

public class Modele_FollowUp_TabDetails extends Modele_FollowUp{

	private ArrayList<HashMap<String, Double[][]>> tableaux ;
	private String[] dateExamen;
	
	public Modele_FollowUp_TabDetails(ArrayList<String> chemins) {
		super(chemins);
		
		// to get all keys of all csv
		
		tableaux = new ArrayList<>();
		dateExamen = new String[allLines.size()];
		//for each csv
		for(int i =0; i< allLines.size(); i++) {
			//map contenant tout les tableaux pour un seul examen
			HashMap<String, Double[][]> unExamen = new HashMap<>();
			unExamen.put("nora", readNora(i));
			unExamen.put("excretion",readExcretionRatio(i));
			unExamen.put("timing",readTiming(i));
			unExamen.put("roe", readROE(i));
			unExamen.put("integral",readIntegral(i));
			tableaux.add( unExamen);
			dateExamen[i] = readDateExamen(i);

		}

	}
	
	private Double[][] readNora(int indiceExamen){
		Double[][] nora = new Double[3][3];
		int ligneDansCsv = rechercheLineContains(indiceExamen, "NORA");
		int colonneDansCsv = 1;
		for(int i =0; i< 3; i++) {
			for(int j =0 ; j<3 ;j++) {
				if(!super.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null"))
					nora[i][j] = Double.parseDouble(super.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
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
				if(!super.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null"))
					timing[i][j] = Double.parseDouble(super.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
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
				if(!super.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv].equals("null")) 
					roe[i][j] = Double.parseDouble(super.allLines.get(indiceExamen).get(i+ligneDansCsv).split(",")[j+colonneDansCsv]);
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
				if(!super.allLines.get(indiceExamen).get(ligneDansCsv).split(",")[j+colonneDansCsv].equals("null")) 
					roe[0][j] = Double.parseDouble(super.allLines.get(indiceExamen).get(ligneDansCsv).split(",")[j+colonneDansCsv]);
				else
					roe[0][j] = null;
			}
		return roe;
	}
	
	private String readDateExamen(int indiceExamen){
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

	public String[] getDateExamen() {
		return dateExamen;
	}
}
