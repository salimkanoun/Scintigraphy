package org.petctviewer.scintigraphy.lympho;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

public class FenApplicationLympho extends FenApplicationWorkflow {
	private static final long serialVersionUID = 1L;

	public FenApplicationLympho(ImageSelection ims, String nom) {
		super(ims, nom);

		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());

	}

}
