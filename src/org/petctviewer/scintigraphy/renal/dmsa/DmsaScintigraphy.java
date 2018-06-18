package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class DmsaScintigraphy extends Scintigraphy {

	public DmsaScintigraphy() {
		super("dmsa");
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		ImagePlus imp = images[0];

		if(imp.getStackSize() == 2) {
			imp.getStack().getProcessor(1).flipHorizontal();
		}

		//TODO test bon format dicom
		
		return imp.duplicate();
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(this.getImp());
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.yellow);
		
		FenApplication fen = new FenApplication(this.getImp(), this.getExamType());
		this.setFenApplication(fen);
		this.getImp().setOverlay(overlay);
		fen.setControleur(new Controleur_Dmsa(this));
	}

}