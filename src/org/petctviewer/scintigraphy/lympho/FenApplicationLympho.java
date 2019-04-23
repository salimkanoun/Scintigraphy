package org.petctviewer.scintigraphy.lympho;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class FenApplicationLympho extends FenApplication{

	public FenApplicationLympho(ImagePlus imp, String nom) {
		super(imp, nom);


		IJ.setTool(Toolbar.RECTANGLE);
	}

}
