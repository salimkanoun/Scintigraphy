package org.petctviewer.scintigraphy.renal;

import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import ij.IJ;
import ij.gui.Toolbar;

public class Vue_Renal extends VueScinDyn {

	public Vue_Renal() {
		super("Renal Scintigraphy");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		super.ouvertureImage(titresFenetres);
		
		//on inverse l'image pur garder l'orientation gauche / droite
		for(int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}
		
		this.impProjetee = this.projeter(this.impPost);
		this.impAnt = null;
		
		VueScin.setCustomLut(impProjetee);
		
		this.setImp(impProjetee);
		
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_Renal(this));
		
		IJ.setTool(Toolbar.POLYGON);
	}

}
