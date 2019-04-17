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
	protected ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws Exception {
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
			ImageSelection[] selection = new ImageSelection[1];
			selection[0] = new ImageSelection(imp.duplicate(), null, null);
			return selection;
		} else {
			throw new Exception("Ant/Post Image expected");
		}
		
		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(imp.duplicate(), null, null);
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus());
		Library_Gui.setOverlayDG(overlay, selectedImages[0].getImagePlus(), Color.yellow);
		
		FenApplication fen = new FenApplication(selectedImages[0].getImagePlus(), this.getExamType());
		this.setFenApplication(fen);
		selectedImages[0].getImagePlus().setOverlay(overlay);
		fen.setControleur(new Controleur_Dmsa(this, selectedImages, "dmsa"));
	}

}
