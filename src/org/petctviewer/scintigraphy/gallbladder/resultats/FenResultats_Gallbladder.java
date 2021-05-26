package org.petctviewer.scintigraphy.gallbladder.resultats;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.gallbladder.application.ModelGallbladder;
import org.petctviewer.scintigraphy.gallbladder.resultats.tabs.TabGallbladder;

public class FenResultats_Gallbladder extends FenResults{
    private static final long serialVersionUID = 1L;

    public FenResultats_Gallbladder(List<ImagePlus> captures, ControllerScin controller){
                                        super(controller);


                                        this.addTab(new TabGallbladder(captures.get(0).getBufferedImage(), this));
                                    }
}