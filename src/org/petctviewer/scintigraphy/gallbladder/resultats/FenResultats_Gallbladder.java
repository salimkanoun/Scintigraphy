package org.petctviewer.scintigraphy.gallbladder.resultats;

import org.petctviewer.scintigraphy.esophageus.application.Model_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCondense;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCurves;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabRentention;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabTransitTime;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.util.ArrayList;
import java.util.HashMap;

import org.petctviewer.scintigraphy.gallbladder.application.Model_Gallblader;

public class FenResultats_Gallbladder extends FenResults{
    private static final long serialVersionUID = 1L;

    public FenResultats_Gallbladder(ArrayList<HashMap<String, ArrayList<Double>>> arrayList,
                                    ArrayList<Object[]> dicomRoi, Model_Gallblader modelApp, String studyName,
                                    ControllerScin controller){
                                        super(controller);

                                        Model_Resultats_Gallblader model = new Model_Resultats_Gallblader(arrayList, dicomRoi,
                                        studyName, modelApp.gallPlugIn, modelApp.getImageSelection());

                                        modelApp.setModelResults(model);

                                        this.addTab(new TabCurves(arrayList.size(), this, model));
                                        this.addTab(new TabTransitTime(arrayList.size(), this, model));
                                        this.addTab(new TabRentention(arrayList.size(), this, model));
                                        this.addTab(new TabCondense(arrayList.size(), this, model));
                                    }
}