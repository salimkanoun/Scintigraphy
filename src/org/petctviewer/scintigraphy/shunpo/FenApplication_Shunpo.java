package org.petctviewer.scintigraphy.shunpo;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplication_Shunpo extends FenApplicationWorkflow {
	private static final long serialVersionUID = 1L;

	public FenApplication_Shunpo(Scintigraphy main, ImageSelection ims) {
		super(ims, main.getStudyName());

		IJ.setTool(Toolbar.POLYGON);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus(), Color.YELLOW);
	}
}
