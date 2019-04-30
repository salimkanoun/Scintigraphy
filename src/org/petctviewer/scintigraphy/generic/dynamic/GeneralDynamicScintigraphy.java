package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
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
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication_GeneralDyn(selectedImages[0].getImagePlus(), this.getStudyName(), this));
		this.getFenApplication().setControleur(new Controleur_GeneralDyn(this, "Dynamic scintigraphy",selectedImages));
		IJ.setTool(Toolbar.POLYGON);
	}
	
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		
		ImagePlus[] imps = new ImagePlus[2];
		
		for (int i=0 ; i<selectedImages.length; i++) {
			if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT ) {
				if(imps[0]!=null) throw new WrongInputException("Multiple dynamic Antorior Image");
				imps[0] = selectedImages[i].getImagePlus().duplicate();
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_POST) {
				if(imps[1]!=null) throw new WrongInputException("Multiple dynamic Posterior Image");
				imps[1] = selectedImages[i].getImagePlus().duplicate();
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT_POST) {
				if(imps[1]!=null || imps[0]!=null) throw new WrongInputException("Multiple dynamic Image");
				imps=Library_Dicom.splitDynamicAntPost(selectedImages[i].getImagePlus());
			}else{
				throw new WrongInputException("Unexpected Image orientation");
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
