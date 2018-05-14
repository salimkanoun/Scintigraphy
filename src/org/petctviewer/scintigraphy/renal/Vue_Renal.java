package org.petctviewer.scintigraphy.renal;

import org.petctviewer.scintigraphy.scin.FenApplication;
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
		this.impProjetee = this.projeter(this.impPost);
		
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_Renal(this));
		
		IJ.setTool(Toolbar.POLYGON);
	}

}
