package org.petctviewer.scintigraphy.renal;

import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;

public class Vue_Renal extends VueScinDyn {

	public Vue_Renal() {
		super("Renal Scintigraphy");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		super.ouvertureImage(titresFenetres);
		
		//on inverse l'image pour garder l'orientation gauche / droite
		for(int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}
		
		ImagePlus impProjetee = this.projeter(this.impPost);
		
		if(this.impAnt != null) {
			System.out.println("Heya");
			
			for(int i = 1; i <= this.impAnt.getStackSize(); i++) {
				this.impAnt.getStack().getProcessor(i).flipHorizontal();
			}
			
			ImagePlus impProjAnt = this.projeter(impAnt);
			ImageStack s = impProjetee.getStack();
			s.addSlice(impProjAnt.getProcessor());
			impProjetee.setStack(s);
			impProjetee.show();
		}
		
		VueScin.setCustomLut(impProjetee);
		
		this.setImp(impProjetee);
		
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_Renal(this));
		
		IJ.setTool(Toolbar.POLYGON);
	}

}
