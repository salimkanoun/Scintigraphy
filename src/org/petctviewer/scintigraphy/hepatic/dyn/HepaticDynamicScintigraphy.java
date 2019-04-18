package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;

public class HepaticDynamicScintigraphy extends Scintigraphy {

	private ImagePlus impAnt, impPost, impProjetee, impProjeteeAnt;
	private int[] frameDurations;

	
	public HepaticDynamicScintigraphy() {
		super("Biliary scintigraphy");
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);
		
		this.setFenApplication(new FenApplication(selectedImages[0].getImagePlus(), this.getExamType()));
		selectedImages[0].getImagePlus().setOverlay(overlay);
		this.getFenApplication().setControleur(new Controleur_HepaticDyn(this, selectedImages, "Biliary scintigraphy"));
	}

	

	  @Override
	  public ImageSelection[] preparerImp(ImageSelection[] images) {
	    if (images.length > 2) {
	      IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
	    }
	    
	    ImagePlus[] imps = Library_Dicom.sortDynamicAntPost(images[0].getImagePlus());
	    if(imps[0] != null) {
	      this.impAnt = imps[0];
	    }
	    
	    if(imps[1] != null) {
	      this.impPost = imps[1];
	      for(int i = 1; i <= this.impPost.getStackSize(); i++) {
	        this.impPost.getStack().getProcessor(i).flipHorizontal();
	      }
	    }
	    
	    if( this.impAnt !=null ) {
	      impProjeteeAnt = Library_Dicom.projeter(this.impAnt,0,impAnt.getStackSize(),"avg");
	      impProjetee=impProjeteeAnt;
	      this.frameDurations = Library_Dicom.buildFrameDurations(this.impAnt);
	    }
	    if ( this.impPost !=null ) {
	      impProjetee = Library_Dicom.projeter(this.impPost,0,impPost.getStackSize(),"avg");
	      this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost);
	    }

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(impProjetee.duplicate(), null, null);
		return selection;
	  }

	public ImagePlus getImpAnt() {
		return impAnt;
	}

	public ImagePlus getImpPost() {
		return impPost;
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

	  
}
