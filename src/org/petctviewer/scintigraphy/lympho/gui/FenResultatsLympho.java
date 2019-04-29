package org.petctviewer.scintigraphy.lympho.gui;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.ImagePlus;

public class FenResultatsLympho extends FenResults {

	private static final long serialVersionUID = -4147119228896005654L;

	public FenResultatsLympho(ModeleScin model, ImagePlus[] captures) {
		super(model);

		this.setMainTab(new TabPrincipalLympho(this, "Result", model, captures));
		this.addTab(new TabPost(this, "Pelvis", true));
		model.getImagePlus().show();

		this.addTab(new TabTest3(this, "Test3", true));

	}

}
