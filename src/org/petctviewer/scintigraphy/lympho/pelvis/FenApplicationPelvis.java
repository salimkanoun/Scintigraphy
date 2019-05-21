package org.petctviewer.scintigraphy.lympho.pelvis;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplicationPelvis extends FenApplicationWorkflow {

	private static final long serialVersionUID = 3082120736528170529L;

	public FenApplicationPelvis(ImageSelection ims, String nom) {
		super(ims, nom);

		IJ.setTool(Toolbar.POLYGON);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());
	}

}
