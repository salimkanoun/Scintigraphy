package org.petctviewer.scintigraphy.statics;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImagePlus;
import ij.gui.Overlay;

public class StaticScintigraphy extends Scintigraphy {

	public StaticScintigraphy() {
		super("General static scintigraphy");
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		
		if(images.length > 1 || images.length <= 0) {
			throw new IllegalArgumentException("Exam needs exactly one image");
		}
		
		ImagePlus imp = images[0];
		imp = Scintigraphy.sortImageAntPost(imp);//inverse la 2 eme slice
		return imp;
	}

	@Override
	public void lancerProgramme() {
		
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(),12);
		Scintigraphy.setOverlayDG(overlay, this.getImp(),Color.white);
		
		this.setFenApplication(new FenApplication_ScinStatic(this.getImp(), this.getExamType()));
		this.getImp().setOverlay(overlay);

		ControleurScinStatic ctrl = new ControleurScinStatic(this);
		this.getFenApplication().setControleur(ctrl);
	}

}
