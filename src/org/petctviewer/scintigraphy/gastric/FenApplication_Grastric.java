package org.petctviewer.scintigraphy.gastric;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplication_Grastric extends FenApplicationWorkflow {
	private static final long serialVersionUID = 1L;

	public FenApplication_Grastric(ImageSelection ims, String nom) {
		super(ims, nom);
		
		IJ.setTool(Toolbar.POLYGON);
		Library_Gui.initOverlay(ims.getImagePlus());
		Library_Gui.setOverlayDG(ims.getImagePlus());
	}

}
