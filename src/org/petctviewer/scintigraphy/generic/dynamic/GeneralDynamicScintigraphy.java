package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class GeneralDynamicScintigraphy extends Scintigraphy{

	private ImagePlus impAnt, impPost, impProjetee, impProjeteeAnt;
	private int[] frameDurations;

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}
	
	@Override
	public void lancerProgramme() {
		this.setFenApplication(new FenApplication_GeneralDyn(this.getImp(), this.getExamType(), this));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this));
		IJ.setTool(Toolbar.POLYGON);
	}
	
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 1) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}
		
		ImagePlus[] imps = Library_Dicom.sortDynamicAntPost(images[0]);
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

		return impProjetee.duplicate();
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
