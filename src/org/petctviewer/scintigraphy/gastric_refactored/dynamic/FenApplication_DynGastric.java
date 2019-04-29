package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class FenApplication_DynGastric extends FenApplication {
	private static final long serialVersionUID = 1L;

	public FenApplication_DynGastric(ImagePlus imp, String nom) {
		super(imp, nom);
		
		IJ.setTool(Toolbar.POLYGON);
		Library_Gui.initOverlay(getImagePlus());
		Library_Gui.setOverlayDG(getImagePlus(), Color.YELLOW);
	}

}
