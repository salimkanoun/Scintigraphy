package org.petctviewer.scintigraphy.renal;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.plugin.ZProjector;

public class RenalScintigraphy extends Scintigraphy {

	private ImageSelection impAnt, impPost, impProjetee;
	private int[] frameDurations;

	public RenalScintigraphy() {
		super("Renal scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		// Prepare the final ImagePlus array, position 0 for anterior dynamic and
		// position 1 for posterior dynamic.
		ImageSelection[] imps = new ImageSelection[2];

		for (int i = 0; i < selectedImages.length; i++) {
			if (selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_ANT) {
				if (imps[0] != null)
					throw new WrongInputException("Multiple dynamic Antorior Image");
				imps[0] = selectedImages[i].clone();
			} else if (selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_POST) {
				if (imps[1] != null)
					throw new WrongInputException("Multiple dynamic Posterior Image");
				imps[1] = selectedImages[i].clone();
			} else if (selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_ANT_POST) {
				if (imps[1] != null || imps[0] != null)
					throw new WrongInputException("Multiple dynamic Image");
				imps = Library_Dicom.splitDynamicAntPost(selectedImages[i]);
			} else {
				throw new WrongInputException("Unexpected Image type");
			}

			selectedImages[i].getImagePlus().close();
		}

		if (imps[0] != null) {
			this.impAnt = imps[0];
			for (int i = 1; i <= this.impAnt.getImagePlus().getStackSize(); i++) {
				this.impAnt.getImagePlus().getStack().getProcessor(i).flipHorizontal();
			}
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impAnt.getImagePlus());
		}

		if (imps[1] != null) {
			this.impPost = imps[1];
			this.frameDurations = Library_Dicom.buildFrameDurations(this.impPost.getImagePlus());
		}
		
		ImageSelection impPostCountPerSec = this.impPost.clone();
		Library_Dicom.normalizeToCountPerSecond(impPostCountPerSec);
		
		

		impProjetee = Library_Dicom.project(impPostCountPerSec,0,impPostCountPerSec.getImagePlus().getStackSize(),"avg");
		ImageStack stack = impProjetee.getImagePlus().getStack();

		// deux premieres minutes
		int fin = ModeleScinDyn.getSliceIndexByTime(2 * 60 * 1000, frameDurations);
		ImageSelection impPostFirstMin = Library_Dicom.project(impPostCountPerSec, 0, fin,"avg");
		stack.addSlice(impPostFirstMin.getImagePlus().getProcessor());

		// MIP
		ImagePlus pj = ZProjector.run(impPostCountPerSec.getImagePlus(), "max", 0, impPostCountPerSec.getImagePlus().getNSlices());
		stack.addSlice(pj.getProcessor());

		// ajout de la prise ant si elle existe
		if (imps[0] != null) {
			
			ImageSelection impAntCountPerSec = this.impAnt.clone();
			Library_Dicom.normalizeToCountPerSecond(impAntCountPerSec);
			
			ImageSelection impProjAnt = Library_Dicom.project(impAntCountPerSec,0,impAntCountPerSec.getImagePlus().getStackSize(),"avg");
			impProjAnt.getImagePlus().getProcessor().flipHorizontal();
			impAnt = impProjAnt;
			stack.addSlice(impProjAnt.getImagePlus().getProcessor());
		}

		// ajout du stack a l'imp
		impProjetee.getImagePlus().setStack(stack);
		int nbImage = 0;
		if (impPost != null)
			nbImage++;
		if (impProjetee != null)
			nbImage++;
		if (impAnt != null)
			nbImage++;

		ImageSelection[] selection = new ImageSelection[nbImage];

		nbImage = 0;

		if (impProjetee != null) {
			selection[nbImage] = impProjetee;
			nbImage++;
		}
		if (impPost != null) {
			selection[nbImage] = impPost;
			nbImage++;
		}
		if (impAnt != null) {
			selection[nbImage] = impAnt;
		}
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(impProjetee.getImagePlus(), 12);
		Library_Gui.setOverlayTitle("Post", impProjetee.getImagePlus(), Color.yellow, 1);
		Library_Gui.setOverlayTitle("2 first min posterior", impProjetee.getImagePlus(), Color.YELLOW, 2);
		Library_Gui.setOverlayTitle("MIP", impProjetee.getImagePlus(), Color.YELLOW, 3);
		if (this.impAnt != null) {
			Library_Gui.setOverlayTitle("Ant", impProjetee.getImagePlus(), Color.yellow, 4);
		}

		this.setFenApplication(new FenApplication_Renal(selectedImages[0], this.getStudyName(), this));
		selectedImages[0].getImagePlus().setOverlay(overlay);
//		this.getFenApplication().setControleur(new Controleur_Renal(this, selectedImages, "Renal scintigraphy"));
		((FenApplicationWorkflow) this.getFenApplication())
				.setControleur(new ControllerWorkflowRenal(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Modele_Renal(this.frameDurations, selectedImages, "Renal scintigraphy")));
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

	public ImageSelection getImpAnt() {
		return impAnt;
	}

	public ImageSelection getImpPost() {
		return impPost;
	}

}
