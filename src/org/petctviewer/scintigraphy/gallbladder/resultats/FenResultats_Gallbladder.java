package org.petctviewer.scintigraphy.gallbladder.resultats;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.util.ArrayList;
import java.util.HashMap;

import org.petctviewer.scintigraphy.gallbladder.application.ModelGallbladder;
import org.petctviewer.scintigraphy.gallbladder.resultats.tabs.TabGallbladder;

public class FenResultats_Gallbladder extends FenResults{
    private static final long serialVersionUID = 1L;

    public FenResultats_Gallbladder(ArrayList<HashMap<String, ArrayList<Double>>> arrayList,
                                    ArrayList<Object[]> dicomRoi, ModelGallbladder modelApp, String studyName,
                                    ControllerScin controller){
                                        super(controller);

                                        Model_Resultats_Gallbladder model = new Model_Resultats_Gallbladder(arrayList, dicomRoi,
                                        studyName, modelApp.gallPlugIn, modelApp.getImageSelection());

                                        modelApp.setModelResults(model);

                                        this.addTab(new TabGallbladder(arrayList.size(), this, model));
                                    }
}