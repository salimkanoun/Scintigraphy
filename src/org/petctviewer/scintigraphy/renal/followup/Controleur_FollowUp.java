package org.petctviewer.scintigraphy.renal.followup;

import java.util.ArrayList;

public class Controleur_FollowUp {

	public Controleur_FollowUp(FenApplication_FollowUp fen , ArrayList<String> chemins) {
		
		Modele_FollowUp modele = new Modele_FollowUp(chemins);
		
		//creation des graphique
		fen.createLeftKidneyGraph(modele.getLeftKidneyCollection());
		fen.createRightKidneyGraph(modele.getRightKidneyCollection());

		//patient infos
		fen.setPatientName(modele.getNomPatient());
		fen.setIdPatient(modele.getIDPatient());
		
		//all Examens
		fen.setAllExamens(modele.getAllExamens());
	}
}
