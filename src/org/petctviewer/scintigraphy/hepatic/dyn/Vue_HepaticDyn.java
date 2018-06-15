package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class Vue_HepaticDyn extends VueScinDyn {

	public Vue_HepaticDyn() {
		super("Biliary scintigraphy");
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = VueScin.initOverlay(this.getImp(), 12);
		VueScin.setOverlayDG(overlay, this.getImp(), Color.YELLOW);
		
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		this.getImp().setOverlay(overlay);
		this.getFenApplication().setControleur(new Controleur_HepaticDyn(this));
	}

}
