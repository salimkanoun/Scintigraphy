package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

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
	protected ImagePlus preparerImp(String[] titresFenetres) {
		ImagePlus imp = WindowManager.getImage(titresFenetres[0]);

		if(imp.getStackSize() == 2) {
			imp.getStack().getProcessor(1).flipHorizontal();
		}

		//TODO test bon format dicom
		
		return imp.duplicate();
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = VueScin.initOverlay(this.getImp());
		VueScin.setOverlayDG(overlay, this.getImp(), Color.yellow);
		
		FenApplication fen = new FenApplication(this.getImp(), this.getExamType());
		this.setFenApplication(fen);
		this.getImp().setOverlay(overlay);
		fen.setControleur(new Controleur_Dmsa(this));
	}

}
