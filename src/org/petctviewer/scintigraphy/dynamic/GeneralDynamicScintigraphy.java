package org.petctviewer.scintigraphy.dynamic;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.IJ;
import ij.ImagePlus;

public class GeneralDynamicScintigraphy extends DynamicScintigraphy{

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}
	
	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication_GeneralDyn(this.getImp(), this.getExamType(), this));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this));
	}
	
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 1) {
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

		return impProjeteeAnt.duplicate();
	}
	
}
