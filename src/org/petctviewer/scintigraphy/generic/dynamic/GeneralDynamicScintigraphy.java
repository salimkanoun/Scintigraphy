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

	private ImageSelection impAnt, impPost, impProjetee, impProjeteeAnt;
	private int[] frameDurations;

	public GeneralDynamicScintigraphy() {
		super("Dynamic scintigraphy");
	}
	
	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		this.setFenApplication(new FenApplication_GeneralDyn(selectedImages[0].getImagePlus(), this.getStudyName(), this));
		this.getFenApplication().setControleur(new ControllerWorkflowScinDynamic(this, this.getFenApplication(),new Modele_GeneralDyn(selectedImages, "General Dynamic", this.getFrameDurations())));
		IJ.setTool(Toolbar.POLYGON);
	}
	
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		
		ImageSelection[] imps = new ImageSelection[2];
		
		for (int i=0 ; i<selectedImages.length; i++) {
			if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT ) {
				if(imps[0]!=null) throw new WrongInputException("Multiple dynamic Antorior Image");
				imps[0] = selectedImages[i].clone();
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_POST) {
				if(imps[1]!=null) throw new WrongInputException("Multiple dynamic Posterior Image");
				imps[1] = selectedImages[i].clone();
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT_POST) {
				if(imps[1]!=null || imps[0]!=null) throw new WrongInputException("Multiple dynamic Image");
				imps[0] = selectedImages[i].clone();
				imps[1] = selectedImages[i].clone();
				imps[0].setImagePlus(Library_Dicom.sortDynamicAntPost(selectedImages[i].getImagePlus())[0]);
				imps[1].setImagePlus(Library_Dicom.sortDynamicAntPost(selectedImages[i].getImagePlus())[1]);
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
			Library_Dicom.flipStackHorizontal(impPost.getImagePlus());
		}
		
		if( this.impAnt !=null ) {
			impProjeteeAnt = this.impAnt.clone();
			impProjeteeAnt.setImagePlus(Library_Dicom.projeter(this.impAnt.getImagePlus(),0,impAnt.getImagePlus().getStackSize(),"avg"));
			impProjetee=impProjeteeAnt;
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impAnt.getImagePlus());
		}
		if ( this.impPost !=null ) {
			impProjetee = this.impPost.clone();
			impProjetee.setImagePlus(Library_Dicom.projeter(this.impPost.getImagePlus(),0,impPost.getImagePlus().getStackSize(),"avg"));
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost.getImagePlus());
		}

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = impProjetee;
		System.out.println(selection[0].getImageOrientation());
		return selection;
	}


	public ImagePlus getImpAnt() {
		return impAnt.getImagePlus();
	}

	
	public ImagePlus getImpPost() {
		return impPost.getImagePlus();
	}

	
	public int[] getFrameDurations() {
		return frameDurations;
	}
	
	
}
