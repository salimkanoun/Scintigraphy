package org.petctviewer.scintigraphy.lympho.post;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class FenApplicationPost extends FenApplication {

	private static final long serialVersionUID = 3082120736528170529L;

	public FenApplicationPost(ImagePlus imp, String nom) {
		super(imp, nom);

		IJ.setTool(Toolbar.POLYGON);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());
	}

}
