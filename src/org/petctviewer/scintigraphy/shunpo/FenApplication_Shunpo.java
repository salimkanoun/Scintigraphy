package org.petctviewer.scintigraphy.shunpo;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class FenApplication_Shunpo extends FenApplication {
	private static final long serialVersionUID = 1L;

	public FenApplication_Shunpo(Scintigraphy main, ImagePlus imp) {
		super(imp, main.getExamType());
		
		IJ.setTool(Toolbar.POLYGON);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus(), Color.YELLOW);
	}

}
