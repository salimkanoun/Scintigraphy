package org.petctviewer.scintigraphy.renal;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
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
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 1) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}
		
		ImagePlus[] imps = Library_Dicom.sortDynamicAntPost(images[0]);
		
		images[0].close();
		
		if(imps[0] != null) {
			this.impAnt = imps[0];
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
		if (this.impAnt != null) {
			ImagePlus impProjAnt = Library_Dicom.projeter(impAnt,0,impAnt.getStackSize(),"avg");
			impProjAnt.getProcessor().flipHorizontal();
			stack.addSlice(impProjAnt.getProcessor());
		}

		//ajout du stack a l'imp
		impProjetee.setStack(stack);
		
		this.setImp(impProjetee);
		
		return impProjetee;
	}
	

	@Override
	public void lancerProgramme() {
		Overlay overlay = Library_Gui.initOverlay(impProjetee, 12);
		Library_Gui.setOverlayGD(overlay, impProjetee, Color.yellow);
		Library_Gui.setOverlayTitle("Post",overlay, impProjetee, Color.yellow, 1);
		Library_Gui.setOverlayTitle("2 first min posterior", overlay, impProjetee, Color.YELLOW, 2);
		Library_Gui.setOverlayTitle("MIP", overlay, impProjetee, Color.YELLOW, 3);
		if (this.impAnt != null) {
			Library_Gui.setOverlayTitle("Ant", overlay, impProjetee, Color.yellow, 4);
		}
		
	System.out.println(this.getImp().getStackSize());

		this.setFenApplication(new FenApplication_Renal(this.getImp(), this.getExamType(), this));
		this.getImp().setOverlay(overlay);
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
