package org.petctviewer.scintigraphy.CSV;

import java.util.ArrayList;

import javax.swing.JPanel;

public class Controleur_FollowUp_TabMain {
	
	
	public Controleur_FollowUp_TabMain(TabMain tab, ArrayList<String> chemins) {
		
		Modele_FollowUp_TabMain modele = new Modele_FollowUp_TabMain(chemins);
		tab.createLeftKidneyGraph(modele.getLeftKidneyCollection());
		tab.createRightKidneyGraph(modele.getRightKidneyCollection());
		
		tab.setPatientName(modele.getNomPatient());
		tab.setIdPatient(modele.getIDPatient());
		tab.setExcretionsRatios(modele.getExcretionsRatios());
		
		
	}
	
}
