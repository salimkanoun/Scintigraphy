package org.petctviewer.scintigraphy.renal.followup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
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
