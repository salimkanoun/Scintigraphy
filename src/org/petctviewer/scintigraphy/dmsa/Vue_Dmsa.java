package org.petctviewer.scintigraphy.dmsa;

import java.awt.Color;

import org.petctviewer.scintigraphy.hepatic.statique.Controleur_Hepatic;
import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class Vue_Dmsa extends VueScin {

	public Vue_Dmsa() {
		super("dmsa");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		ImagePlus imp = WindowManager.getImage(titresFenetres[0]);
		
		this.setImp(imp.duplicate());
		
		VueScin.setCustomLut(this.getImp());
		FenApplication fen = new FenApplication(this.getImp(), this.getExamType());
		this.setFenApplication(fen);
		
		Overlay overlay = VueScin.initOverlay(imp);
		VueScin.setOverlayDG(overlay, imp, Color.yellow);
		this.getImp().setOverlay(overlay);
		
		fen.setControleur(new Controleur_Dmsa(this));		
		IJ.setTool(Toolbar.POLYGON);
		
		imp.close();	
		
	}

}