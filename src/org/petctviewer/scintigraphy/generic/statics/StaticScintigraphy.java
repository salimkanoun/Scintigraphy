package org.petctviewer.scintigraphy.generic.statics;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

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
		imp = Library_Dicom.sortImageAntPost(imp);//inverse la 2 eme slice
		return imp;
	}

	@Override
	public void lancerProgramme() {
		
		Overlay overlay = Library_Gui.initOverlay(this.getImp(),12);
		Library_Gui.setOverlayDG(overlay, this.getImp(),Color.white);
		
		this.setFenApplication(new FenApplication_ScinStatic(this.getImp(), this.getExamType()));
		this.getImp().setOverlay(overlay);
		
		ModeleScinStatic modele=new ModeleScinStatic();
		this.setModele(modele);

		ControleurScinStatic ctrl = new ControleurScinStatic(this);
		this.getFenApplication().setControleur(ctrl);
		IJ.setTool(Toolbar.POLYGON);
	}

}
