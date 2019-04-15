package org.petctviewer.scintigraphy.renal;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.plugin.ZProjector;

public class RenalScintigraphy extends Scintigraphy {

	private ImagePlus impAnt, impPost, impProjetee;
	private int[] frameDurations;


	JValueSetter nephrogramChart, patlakChart;

	public RenalScintigraphy() {
		super("Renal scintigraphy");
	}

	@Override
	protected ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws Exception {
		
		//Prepare the final ImagePlus array, position 0 for anterior dynamic and position 1 for posterior dynamic.
		ImagePlus[] imps =new ImagePlus[2];
		
		for (int i=0 ; i<selectedImages.length; i++) {
			if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT ) {
				if(imps[0]!=null) throw new Exception("Multiple dynamic Antorior Image");
				imps[0] = selectedImages[i].getImagePlus().duplicate();
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_POST) {
				if(imps[1]!=null) throw new Exception("Multiple dynamic Posterior Image");
				imps[1] = selectedImages[i].getImagePlus().duplicate();
			}else if(selectedImages[i].getImageOrientation()==Orientation.DYNAMIC_ANT_POST) {
				if(imps[1]!=null || imps[0]!=null) throw new Exception("Multiple dynamic Image");
				imps=Library_Dicom.sortDynamicAntPost(selectedImages[i].getImagePlus());
			}else{
				throw new Exception("Unexpected Image type");
			}
			
			selectedImages[i].getImagePlus().close();
		}
		
		
		
		if(imps[1] != null) {
			this.impPost = imps[1];
			for(int i = 1; i <= this.impPost.getStackSize(); i++) {
				this.impPost.getStack().getProcessor(i).flipHorizontal();	
			}
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost);
		}

		impProjetee = Library_Dicom.projeter(this.impPost,0,impPost.getStackSize(),"avg");
		ImageStack stack = impProjetee.getStack();
		
		//deux premieres minutes
		int fin = ModeleScinDyn.getSliceIndexByTime(2 * 60 * 1000, frameDurations);
		ImagePlus impPostFirstMin = Library_Dicom.projeter(this.impPost, 0, fin,"avg");
		stack.addSlice(impPostFirstMin.getProcessor());
		
		// MIP
		ImagePlus pj = ZProjector.run(this.impPost, "max", 0, this.impPost.getNSlices());
		stack.addSlice(pj.getProcessor());

		// ajout de la prise ant si elle existe
		if (imps[0] != null) {
			ImagePlus impProjAnt = Library_Dicom.projeter(impAnt,0,impAnt.getStackSize(),"avg");
			impProjAnt.getProcessor().flipHorizontal();
			impAnt=impProjAnt;
			stack.addSlice(impProjAnt.getProcessor());
		}

		//ajout du stack a l'imp
		impProjetee.setStack(stack);
		

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(impProjetee, null, null);
		return selection;
	}
	

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(impProjetee, 12);
		Library_Gui.setOverlayGD(overlay, impProjetee, Color.yellow);
		Library_Gui.setOverlayTitle("Post",overlay, impProjetee, Color.yellow, 1);
		Library_Gui.setOverlayTitle("2 first min posterior", overlay, impProjetee, Color.YELLOW, 2);
		Library_Gui.setOverlayTitle("MIP", overlay, impProjetee, Color.YELLOW, 3);
		if (this.impAnt != null) {
			Library_Gui.setOverlayTitle("Ant", overlay, impProjetee, Color.yellow, 4);
		}
		
	System.out.println(selectedImages[0].getImagePlus().getStackSize());

		this.setFenApplication(new FenApplication_Renal(selectedImages[0].getImagePlus(), this.getExamType(), this));
		selectedImages[0].getImagePlus().setOverlay(overlay);
		this.getFenApplication().setControleur(new Controleur_Renal(this));
	}

	public JValueSetter getNephrogramChart() {
		return nephrogramChart;
	}

	public void setNephrogramChart(JValueSetter nephrogramChart) {
		this.nephrogramChart = nephrogramChart;
	}

	public JValueSetter getPatlakChart() {
		return patlakChart;
	}

	public void setPatlakChart(JValueSetter patlakChart) {
		this.patlakChart = patlakChart;
	}


	public int[] getFrameDurations() {
		return frameDurations;
	}
	
	public ImagePlus getImpAnt() {
		return impAnt;
	}

	public ImagePlus getImpPost() {
		return impPost;
	}


}
