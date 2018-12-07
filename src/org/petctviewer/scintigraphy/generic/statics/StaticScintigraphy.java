package org.petctviewer.scintigraphy.generic.statics;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageOrientation;
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
	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		
		if(selectedImages.length != 1) {
			throw new Exception("Software Accept only one A/P serie");
		}
		ImagePlus imp = null;
		//SK ETENDRE A SEULEMENT UNE INCIDENCE ??
		//SK PAS SUR QUE POST ANT SOIT BIEN PRIS EN COMPTE DANS LE FLIP / Ordre du stack
		if(selectedImages[0].getImageOrientation()==ImageOrientation.ANT_POST || selectedImages[0].getImageOrientation()==ImageOrientation.POST_ANT) {
			imp = selectedImages[0].getImagePlus();
			imp = Library_Dicom.sortImageAntPost(imp);//inverse la 2 eme slice
		}else {
			throw new Exception("Not A/P view");
		}
		
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
