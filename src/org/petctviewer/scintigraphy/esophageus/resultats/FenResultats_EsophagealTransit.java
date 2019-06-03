package org.petctviewer.scintigraphy.esophageus.resultats;

import org.petctviewer.scintigraphy.esophageus.application.Model_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCondense;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCurves;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabRentention;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabTransitTime;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.util.ArrayList;
import java.util.HashMap;

public class FenResultats_EsophagealTransit extends FenResults {

	// private Model_Resultats_EsophagealTransit modele ;

	private static final long serialVersionUID = 1L;

	/*
	 * un partie main avec graph main et un jtablecheckbox main un partie transit
	 * time avec hraph , jvalue stter, checkbox (1 collonnne pour les acqui entier)
	 * et un couple de controleur par acqui
	 */
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList,
                                          ArrayList<Object[]> dicomRoi, Model_EsophagealTransit modeleApp, String studyName,
                                          ControllerScin controller) {
		super(controller);

		Model_Resultats_EsophagealTransit model = new Model_Resultats_EsophagealTransit(arrayList, dicomRoi,
				studyName, modeleApp.esoPlugIn, modeleApp.getImageSelection());

		this.addTab(new TabCurves(arrayList.size(), this, model));
		this.addTab(new TabTransitTime(arrayList.size(), this, model));
		this.addTab(new TabRentention(arrayList.size(), this, model));
		this.addTab(new TabCondense(arrayList.size(), this, model));

	}

}
