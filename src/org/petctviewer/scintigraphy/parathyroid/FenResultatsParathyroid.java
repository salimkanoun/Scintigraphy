package org.petctviewer.scintigraphy.parathyroid;

import java.util.List;

import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.ImagePlus;

public class FenResultatsParathyroid extends FenResults {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FenResultatsParathyroid(ControllerWorkflowParathyroid controller, ImagePlus montage1, ImagePlus montage2, List<ImagePlus> captures, ImagePlus imgResult) {
        super(controller);

        this.addTab(new MainResult(this, montage1, "Résultats"));
        this.addTab(new MainResult(this, montage2, "Captures", imgResult));
        this.addTab(new TabMediastinale(this));
        
		this.pack();
		this.setVisible(true);
    }

}