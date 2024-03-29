package org.petctviewer.scintigraphy.lympho.gui;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.ImagePlus;

public class FenResultatsLympho extends FenResults {

	private static final long serialVersionUID = -4147119228896005654L;
	
	private final TabPrincipalLympho mainTab;

	public FenResultatsLympho(ControllerScin controller, ImagePlus[] captures) {
		super(controller);

		this.mainTab = new TabPrincipalLympho(this, "Result", controller.getModel(), captures);
		this.setMainTab(this.mainTab);
		this.addTab(new TabVisualGradation(this, "Visual Gradation"));
		this.addTab(new TabPelvis(this, "Pelvis", true));

	}

	public void updateVisualGradation(String gradation) {
		this.mainTab.updateVisualGradation(gradation);
	}

}
