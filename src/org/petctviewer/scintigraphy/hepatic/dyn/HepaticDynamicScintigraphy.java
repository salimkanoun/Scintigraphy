package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;

public class HepaticDynamicScintigraphy extends DynamicScintigraphy {

	public HepaticDynamicScintigraphy() {
		super("Biliary scintigraphy");
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(), 12);
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.YELLOW);
		
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));
		this.getImp().setOverlay(overlay);
		this.getFenApplication().setControleur(new Controleur_HepaticDyn(this));
	}

	

	  @Override
	  protected ImagePlus preparerImp(ImagePlus[] images) {
	    if (images.length > 2) {
	      IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
	    }
	    
	    ImagePlus[] imps = Scintigraphy.sortAntPost(images[0]);
	    if(imps[0] != null) {
	      this.impAnt = imps[0].duplicate();
	    }
	    
	    if(imps[1] != null) {
	      this.impPost = imps[1].duplicate();
	      for(int i = 1; i <= this.impPost.getStackSize(); i++) {
	        this.impPost.getStack().getProcessor(i).flipHorizontal();
	      }
	    }
	    
	    if( this.impAnt !=null ) {
	      impProjeteeAnt = projeter(this.impAnt,0,impAnt.getStackSize(),"avg");
	      impProjetee=impProjeteeAnt;
	      this.frameDurations = buildFrameDurations(this.impAnt);
	    }
	    if ( this.impPost !=null ) {
	      impProjetee = projeter(this.impPost,0,impPost.getStackSize(),"avg");
	      this.frameDurations = buildFrameDurations(this.impPost);
	    }
	 
	    return impProjetee.duplicate();
	  }

}
