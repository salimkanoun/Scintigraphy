package org.petctviewer.scintigraphy.parathyroid;


import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.ImagePlus;

public class FenResultatsParathyroid extends FenResults {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FenResultatsParathyroid(ControllerWorkflowParathyroid controller, ImagePlus montage1, ImagePlus montage2) {
        super(controller);

        this.addTab(new MainResult(this, montage1, "Résultats"));
        this.addTab(new MainResult(this, montage2, "Captures"));
        
		this.pack();
		this.setVisible(true);
    }

}