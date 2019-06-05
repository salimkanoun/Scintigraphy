package org.petctviewer.scintigraphy.hepatic.SecondExam;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplicationSecondHepaticDyn extends FenApplicationWorkflow {
	private static final long serialVersionUID = -910237891674972798L;

	public FenApplicationSecondHepaticDyn(ImageSelection ims, String nom) {
		super(ims, nom);
		
		IJ.setTool(Toolbar.RECTANGLE);

	}
}