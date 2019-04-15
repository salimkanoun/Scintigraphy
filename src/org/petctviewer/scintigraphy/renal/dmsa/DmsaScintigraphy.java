package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.StackReverser;

public class DmsaScintigraphy extends Scintigraphy {

	public DmsaScintigraphy() {
		super("dmsa");
	}

	@Override
	protected ImagePlus preparerImp(ImageSelection[] selectedImages) throws Exception {
		if(selectedImages.length>1) throw new Exception ("Only one serie Expected");
		
		ImagePlus imp = selectedImages[0].getImagePlus();

		if(selectedImages[0].getImageOrientation()==Orientation.ANT_POST) {
			imp.getStack().getProcessor(1).flipHorizontal();
			//SK REVERSE DES METADATA A VERIFIER !!!!
			StackReverser reverser=new StackReverser();
			reverser.flipStack(imp);
		}else if(selectedImages[0].getImageOrientation()==Orientation.POST_ANT){
			imp.getStack().getProcessor(2).flipHorizontal();
		}else if (selectedImages[0].getImageOrientation()==Orientation.POST) {
			return imp.duplicate();
		} else {
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
