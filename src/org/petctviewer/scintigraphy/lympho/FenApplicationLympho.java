package org.petctviewer.scintigraphy.lympho;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class FenApplicationLympho extends FenApplication {
	private static final long serialVersionUID = 1L;

	public FenApplicationLympho(ImagePlus imp, String nom) {
		super(imp, nom);

		IJ.setTool(Toolbar.RECTANGLE);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());
	}

}
