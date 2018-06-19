package org.petctviewer.scintigraphy.statics;

import java.awt.Color;

import org.petctviewer.scintigraphy.cardiac.Controleur_Cardiac;
import org.petctviewer.scintigraphy.cardiac.FenApplication_Cardiac;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

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
		imp.getStack().getProcessor(2).flipHorizontal();
		return imp;
	}

	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication_ScinStatic(this.getImp(), this.getExamType()));
		ControleurScinStatic ctrl = new ControleurScinStatic(this);
		this.getFenApplication().setControleur(ctrl);
	}

}
