package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageOrientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.Overlay;

public class DmsaScintigraphy extends Scintigraphy {

	public DmsaScintigraphy() {
		super("dmsa");
	}

	@Override
	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		if(selectedImages.length>1) throw new Exception ("Only one serie Expected");
		
		ImagePlus imp = selectedImages[0].getImagePlus();

		if(selectedImages[0].getImageOrientation()==ImageOrientation.ANT_POST) {
			imp.getStack().getProcessor(1).flipHorizontal();
		}else if(selectedImages[0].getImageOrientation()==ImageOrientation.POST_ANT){
			imp.getStack().getProcessor(0).flipHorizontal();
		}else {
			throw new Exception("Ant/Post Image expected");
		}

		return imp.duplicate();
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = Library_Gui.initOverlay(this.getImp());
		Library_Gui.setOverlayDG(overlay, this.getImp(), Color.yellow);
		
		FenApplication fen = new FenApplication(this.getImp(), this.getExamType());
		this.setFenApplication(fen);
		this.getImp().setOverlay(overlay);
		fen.setControleur(new Controleur_Dmsa(this));
		Modele_Dmsa modele = new Modele_Dmsa();
		this.setModele(modele);
	}

}
