package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.ImageOrientation;
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
		this.setModele(new Modele_GeneralDyn(getFrameDurations()));
		IJ.setTool(Toolbar.POLYGON);
	}
	
	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		
		ImagePlus[] imps = new ImagePlus[2];
		
		for (int i=0 ; i<selectedImages.length; i++) {
			if(selectedImages[i].getImageOrientation()==ImageOrientation.DYNAMIC_ANT ) {
				if(imps[0]!=null) throw new Exception("Multiple dynamic Antorior Image");
				imps[0] = selectedImages[i].getImagePlus().duplicate();
			}else if(selectedImages[i].getImageOrientation()==ImageOrientation.DYNAMIC_POST) {
				if(imps[1]!=null) throw new Exception("Multiple dynamic Posterior Image");
				imps[1] = selectedImages[i].getImagePlus().duplicate();
			}else if(selectedImages[i].getImageOrientation()==ImageOrientation.DYNAMIC_ANT_POST) {
				if(imps[1]!=null || imps[0]!=null) throw new Exception("Multiple dynamic Image");
				imps=Library_Dicom.sortDynamicAntPost(selectedImages[i].getImagePlus());
			}else{
				throw new Exception("Unexpected Image type");
			}
			
			selectedImages[i].getImagePlus().close();
		}
		
		if(imps[0] != null) {
			this.impAnt = imps[0];
		}
		
		if(imps[1] != null) {
			this.impPost = imps[1];
			Library_Dicom.flipStackHorizontal(impPost);
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
