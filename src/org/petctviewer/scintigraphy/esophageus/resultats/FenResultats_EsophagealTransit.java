package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.esophageus.application.Modele_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCondense;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCurves;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabRentention;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabTransitTime;

@SuppressWarnings("serial")
public class FenResultats_EsophagealTransit extends JFrame {
	
	private Modele_Resultats_EsophagealTransit modele ;

	/*
	 * un partie main avec graph main et un jtablecheckbox main
	 * un partie transit time avec hraph , jvalue stter, checkbox (1 collonnne pour les acqui entier) et un couple de controleur par acqui
	 */
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList, ArrayList<Object[]> dicomRoi, Modele_EsophagealTransit modeleApp) {
		
		modele = new Modele_Resultats_EsophagealTransit(arrayList,dicomRoi);
		this.setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Curves", new TabCurves(arrayList.size(), this.modele, modeleApp));	
		tabbedPane.addTab("Transit Time", new TabTransitTime(arrayList.size(), this.modele, modeleApp));	 
		tabbedPane.addTab("Retention", new TabRentention(arrayList.size(), this.modele, modeleApp));
		tabbedPane.addTab("Condensed Dynamic images", new TabCondense(arrayList.size(), this.modele, modeleApp));

		this.add(tabbedPane);
		
	}

}
