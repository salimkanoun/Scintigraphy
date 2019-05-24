package org.petctviewer.scintigraphy.lympho.gui;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.ImagePlus;

public class FenResultatsLympho extends FenResults {

	private static final long serialVersionUID = -4147119228896005654L;

	public FenResultatsLympho(ControllerScin controller, ImagePlus[] captures) {
		super(controller);

		this.setMainTab(new TabPrincipalLympho(this, "Result", controller.getModel(), captures));
		this.addTab(new TabPelvis(this, "Pelvis", true));
		controller.getModel().getImagePlus().show();

		this.addTab(new TabTest3(this, "Test3", true));

	}

}
