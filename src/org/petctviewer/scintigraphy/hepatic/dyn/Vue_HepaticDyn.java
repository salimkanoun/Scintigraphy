package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class Vue_HepaticDyn extends VueScinDyn {

	public Vue_HepaticDyn() {
		super("Biliary scintigraphy");
	}
	
	@Override
	public void ouvertureImage(String[] titresFenetres) {
		super.ouvertureImage(titresFenetres);
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		
		Overlay overlay = VueScin.initOverlay(this.getImp(), 12);
		VueScin.setOverlayDG(overlay, this.getImp(), Color.YELLOW);
		this.getImp().setOverlay(overlay);
		
		this.getFenApplication().setControleur(new Controleur_HepaticDyn(this));
		IJ.setTool(Toolbar.RECT_ROI);
	}

}
