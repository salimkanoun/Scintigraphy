package org.petctviewer.scintigraphy.CSV;

import java.util.ArrayList;

public class Controleur_FollowUp_TabDetails {
	
	public Controleur_FollowUp_TabDetails(TabDetails tab, ArrayList<String> chemins) {

		Modele_FollowUp_TabDetails modele = new Modele_FollowUp_TabDetails(chemins);
		tab.setTableaux(modele.getTableaux());
	}
	
}
