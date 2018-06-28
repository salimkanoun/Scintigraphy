package org.petctviewer.scintigraphy.CSV;

import java.util.ArrayList;
import java.util.HashMap;

public class Modele_FollowUp_TabDetails extends Modele_FollowUp{

	private ArrayList<HashMap<String, Double[][]>> tableaux ;
	
	
	public Modele_FollowUp_TabDetails(ArrayList<String> chemins) {
		super(chemins);
		
		// to get all keys of all csv
		
		tableaux = new ArrayList<>();

		//for each csv
		for(int i =0; i< allLines.size(); i++) {
			//map contenant tout les tableaux pour un seul examen
			HashMap<String, Double[][]> unExamen = new HashMap<>();
			unExamen.put("nora", readNora(i));
			unExamen.put("excretion",readExcretionRatio(i));
			unExamen.put("timing",readTiming(i));
			
			
			
			tableaux.add( unExamen);
		}

	}
	
	private Double[][] readNora(int indiceExamen){
		Double[][] nora = new Double[3][3];
		int ligneDansCsv = 10;
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
		int ligneDansCsv = 17;
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

	public ArrayList<HashMap<String, Double[][]>> getTableaux(){
		
		
		return this.tableaux;
	}

}
