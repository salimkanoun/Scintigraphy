package org.petctviewer.scintigraphy.renal;

import java.awt.Color;

import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class Vue_Renal extends VueScinDyn {

	public Vue_Renal() {
		super("Renal scintigraphy");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		super.ouvertureImage(titresFenetres);

		// on inverse l'image pour garder l'orientation gauche / droite
		for (int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}

		ImagePlus impProjetee = projeter(this.impPost);
		ImageStack s = impProjetee.getStack();

		Overlay ov = VueScin.initOverlay(impProjetee, 12);
		
		int fin = ModeleScinDyn.getSliceIndexByTime(2 * 60 * 1000, this.getFrameDurations());
		ImagePlus impPostFirstMin = projeter(this.impPost, 0, fin);
		s.addSlice(impPostFirstMin.getProcessor());
		
		// ajout de la prise ant si elle existe
		if (this.impAnt != null) {
			for (int i = 1; i <= this.impAnt.getStackSize(); i++) {
				this.impAnt.getStack().getProcessor(i).flipHorizontal();
			}

			ImagePlus impProjAnt = projeter(impAnt);
			s.addSlice(impProjAnt.getProcessor());
		}
		
		impProjetee.setStack(s);

		VueScin.setCustomLut(impProjetee);

		this.setImp(impProjetee);
		this.getImp().setOverlay(ov);
		
		this.setFenApplication(new FenApplication(this.getImp(), this.getExamType()));

		VueScin.setOverlayGD(ov, impProjetee, Color.yellow);
		VueScin.setOverlayTitle("2 first min of Post", ov, impProjetee, Color.YELLOW, 2);
		VueScin.setOverlayTitle("Post", ov, impProjetee, Color.yellow, 1);
		if (this.impAnt != null) {
			VueScin.setOverlayTitle("Ant", ov, impProjetee, Color.yellow, 3);
		}

		this.getImp().setOverlay(ov);
		this.getFenApplication().setControleur(new Controleur_Renal(this));

		IJ.setTool(Toolbar.POLYGON);
	}

}
