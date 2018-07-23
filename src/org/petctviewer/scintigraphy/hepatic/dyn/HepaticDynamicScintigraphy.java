package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.gui.Overlay;

public class HepaticDynamicScintigraphy extends DynamicScintigraphy {

	public HepaticDynamicScintigraphy() {
		super("Biliary scintigraphy");
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(), 12);
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.YELLOW);
		
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		this.getImp().setOverlay(overlay);
		this.getFenApplication().setControleur(new Controleur_HepaticDyn(this));
	}

}
