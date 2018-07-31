package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JCheckBox;

public class Controleur_Resultats_EsophagealTransit implements ItemListener {

	private Modele_Resultats_EsophagealTransit modele;
	private FenResultats_EsophagealTransit fen;
	
	public Controleur_Resultats_EsophagealTransit(FenResultats_EsophagealTransit fen, ArrayList<HashMap<String, ArrayList<Double>>> arrayList) {
		modele = new Modele_Resultats_EsophagealTransit(arrayList);
		this.fen = fen;
		this.fen.setGraphDataset(modele.getDataSet());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox selected = (JCheckBox)e.getSource();

	
		//coordonnes de la cjeckbox coché
		this.modele.actualiserDatasetFromCheckbox(
				Integer.parseInt(selected.getName().split("\\|")[0]), 
				Integer.parseInt(selected.getName().split("\\|")[1]), 
				selected.isSelected());		

		
		// set des nouvelles valeurs
		fen.setGraphDataset(modele.getDataSet());
	}
}
