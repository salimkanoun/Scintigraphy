/**
Copyright (C) 2017 KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy.platelet;

import ij.plugin.filter.Analyzer;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


import javax.swing.JTable;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.util.DicomTools;

public class Modele_Plaquettes {
	
	// Stockage des objets mesures, chaque image est identifee par sa date d'aqcisition
	// et l'objet contient toutes les mesusres realisee sur image ant(si presente) et post
	HashMap<Date, MesureImage> mesures=new HashMap<Date,MesureImage>();
	private Date dateHeureDebut;
	

	public Modele_Plaquettes() {
	}

	protected void enregisterMesure(String roi, ImagePlus imp) {
		//Parse de la date et heure d'acquisition
		String aquisitionDate = DicomTools.getTag(imp, "0008,0022");
		String aquisitionTime = DicomTools.getTag(imp, "0008,0032");
		String dateInput=aquisitionDate.trim()+aquisitionTime.trim();
		
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMddHHmmss.SS");
        Date dateAcquisition = null;
		try {
			dateAcquisition = parser.parse(dateInput);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	
		// BONNE METHODE DE CALCUL AVEC DENSITE A UPDATER DANS LES AUTRES PROGRAMMES
		// SK
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		Analyzer.setMeasurement(Measurements.MEAN, true);
		Analyzer analyser=new Analyzer(imp);
		analyser.measure();
		ResultsTable density=Analyzer.getResultsTable();
		double counts=density.getValueAsDouble(ResultsTable.RAW_INTEGRATED_DENSITY, 0);
		double mean=density.getValueAsDouble(ResultsTable.MEAN, 0);
		
	
		
		//Si premiere fois qu'on traite l'image on cree l'objet et on l'ajoute dans la hashMap
		if (!mesures.containsKey(dateAcquisition)) {
			MesureImage mesure=new MesureImage(dateAcquisition);
			// on calcule le delai par rapport a la premiere image et on enregistre la valeur en heure (utile pour le trie et les courbes ensuite)
			mesure.setDelayFromStart((double) ((dateAcquisition.getTime()-dateHeureDebut.getTime())/(1000*60*60)));
			IJ.log(String.valueOf(mesure.getDelayFromStart()));
			mesures.put(dateAcquisition,mesure);
		}
		
		// On calcul les valeurs et on l'ajoute dans l'objet adHoc
		if (mesures.containsKey(dateAcquisition)) {
			if (roi.equals("Spleen Post")) {
				double[] spleen=new double[2];
				spleen[0]=counts;
				spleen[1]=mean;
				mesures.get(dateAcquisition).setSpleenValue(spleen);
			}
			else if (roi.equals("Liver Post")) {
				double[] liver=new double[2];
				liver[0]=counts;
				liver[1]=mean;
				mesures.get(dateAcquisition).setLiverValue(liver);;
			}
			
			else if (roi.equals("Heart Post")) {
				double[] heart=new double[2];
				heart[0]=counts;
				heart[1]=mean;
				mesures.get(dateAcquisition).setHeartValue(heart);;
			}
			else if (roi.equals("Spleen Ant")) {
				double[] spleen=new double[2];
				spleen[0]=counts;
				spleen[1]=mean;
				mesures.get(dateAcquisition).setSpleenAntValue(spleen);
			}
			else if (roi.equals("Liver Ant")) {
				double[] liver=new double[2];
				liver[0]=counts;
				liver[1]=mean;
				mesures.get(dateAcquisition).setliverAntValue(liver);
				}
			
			else if (roi.equals("Heart Ant")) {
				double[] heart=new double[2];
				heart[0]=counts;
				heart[1]=mean;
				mesures.get(dateAcquisition).setHeartAntValue(heart);
			}
			
		}
		
		if (Controleur_Plaquettes.showLog) {
			IJ.log(roi + "counts= " + String.valueOf(counts));
			IJ.log(roi + "mean= " + String.valueOf(mean));
		}
	}
	
	protected JTable getResults() {
		//On boule la hashmap pour recuperer les resultats
		Date[] mapDate=new Date[mesures.size()];
		mapDate=mesures.keySet().toArray(mapDate);
		Arrays.sort(mapDate);
		
		String[] titreColonne=new String[mesures.size()+1];
		titreColonne[0]="Time (Hours)";
		
		String[][] data = null;
		
		// pour l'arrondi des resultats
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		//DateFormat dateFormat=new SimpleDateFormat("HH:mm");
		
		// On traite chaque acquisition
		for (int i=0 ; i<mesures.size(); i++) {
			//On ajoute le temps de mesure dans les titre de colonne
			// SK NECESSITE DE TRIER LES COLONNES et CALCULER LE DELTA TEMPS
			int tempsHeures=(int) Math.round(mesures.get(mapDate[i]).getDelayFromStart());
			titreColonne[i+1]=String.valueOf(tempsHeures);
			HashMap<String, Double> resultsImage =mesures.get(mapDate[i]).calculateandGetResults();
			
			//On set les data avec sa taille a la premiere boucle
			if (i==0) {
				data=new String[resultsImage.size()][mesures.size()+1]; 
			}
			
			String[] resultsLabel=new String[resultsImage.size()];
			//on traite touts les resultats d'une acquisition
			resultsLabel=resultsImage.keySet().toArray(resultsLabel);
			for (int j=0; j<resultsLabel.length; j++){
				if (i==0) data[j][0]=resultsLabel[j];
				//On file les data ligne par ligne pour chaque colonne
				data[j][i+1]=String.valueOf(decimalFormat.format(resultsImage.get(resultsLabel[j])));
			}
			
		}
		
		JTable table =new JTable(data, titreColonne);
		return table;
		
	}
	
	/**
	 * Calcul de la correction de decroissance de l'indium
	 * Retourne le nombre de coups corrig�
	 * @param count
	 * @param injectionDate
	 * @param mesureDate
	 * @return
	 */
	protected double calculer_countCorrected(double count, Date injectionDate, Date mesureDate) {
		double indiumLambda=(Math.log(2)/(2.8*24*3600));
		int delaySeconds = (int) (mesureDate.getTime()-injectionDate.getTime())/1000;
		double decayedFraction=Math.pow(Math.E, (indiumLambda*delaySeconds*(-1)));
		double correctedCount=count/(decayedFraction);
		return correctedCount;
	}
	
	public void setDateDebutHeure(Date dateDebutHeure) {
		this.dateHeureDebut=dateDebutHeure;
	}



	
	

}
