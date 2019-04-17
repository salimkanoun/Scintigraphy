package org.petctviewer.scintigraphy.esophageus.resultats;

import java.util.ArrayList;
import java.util.HashMap;

import org.petctviewer.scintigraphy.esophageus.application.Modele_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCondense;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCurves;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabRentention;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabTransitTime;
import org.petctviewer.scintigraphy.shunpo.FenResults;

@SuppressWarnings("serial")
public class FenResultats_EsophagealTransit extends FenResults {

//	private Modele_Resultats_EsophagealTransit modele ;

	/*
	 * un partie main avec graph main et un jtablecheckbox main un partie transit
	 * time avec hraph , jvalue stter, checkbox (1 collonnne pour les acqui entier)
	 * et un couple de controleur par acqui
	 */
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList, ArrayList<Object[]> dicomRoi, Modele_EsophagealTransit modeleApp, String studyName) {
		super(new Modele_Resultats_EsophagealTransit(arrayList,dicomRoi, studyName));
		
		this.addTab(new TabCurves(arrayList.size(), this, modeleApp));
		this.addTab(new TabTransitTime(arrayList.size(), this, modeleApp));
		this.addTab(new TabRentention(arrayList.size(), this, modeleApp));
		this.addTab(new TabCondense(arrayList.size(), this, modeleApp));
		
	}

}
