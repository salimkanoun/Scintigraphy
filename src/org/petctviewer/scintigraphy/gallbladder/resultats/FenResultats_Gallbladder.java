package org.petctviewer.scintigraphy.gallbladder.resultats;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.gallbladder.resultats.tabs.TabGallbladder;

public class FenResultats_Gallbladder extends FenResults{
    private static final long serialVersionUID = 1L;

    public FenResultats_Gallbladder(BufferedImage capture, ControllerScin controller){
                                        super(controller);


                                        this.addTab(new TabGallbladder(this, capture));
                                    }
}