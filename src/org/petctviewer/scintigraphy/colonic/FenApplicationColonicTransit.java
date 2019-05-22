package org.petctviewer.scintigraphy.colonic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplicationColonicTransit extends FenApplicationWorkflow {
	
	private static final long serialVersionUID = -3302015218823308415L;

	public FenApplicationColonicTransit(ImageSelection ims, String nom) {
		super(ims, nom);
		// TODO Auto-generated constructor stub
		
		IJ.setTool(Toolbar.RECTANGLE);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());
		
		this.pack();
	}

}
