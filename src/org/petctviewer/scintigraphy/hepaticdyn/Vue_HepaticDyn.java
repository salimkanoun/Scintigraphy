package org.petctviewer.scintigraphy.hepaticdyn;

import org.petctviewer.scintigraphy.hepatic.Controleur_Hepatic;
import org.petctviewer.scintigraphy.scin.FenApplication;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;
import ij.plugin.ZProjector;

public class Vue_HepaticDyn extends VueScin {

	private ImagePlus impProjetee, impAnt, impPost;
	
	public Vue_HepaticDyn() {
		super("Hepatic Dyn");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		if (titresFenetres.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		if (titresFenetres.length == 1) { // si il y a qu'un fenetre d'ouverte
			ImagePlus imp = WindowManager.getImage(titresFenetres[0]);
			if (VueScin.isMultiFrame(imp)) { // si l'image est multiframe
				ImagePlus[] imps = VueScin.splitCameraMultiFrame(imp);
				this.impAnt = imps[0];
				this.impPost = imps[1];;
			} else if (VueScin.isAnterieur(imp)) {
				this.impAnt = imp;
			} else {
				IJ.log("Please open the Ant view");
			}
			
		} else { // si il y a deux fenetres d'ouvertes
			for (String s : titresFenetres) { //pour chaque fenetre
				ImagePlus imp = WindowManager.getImage(s);
				if (VueScin.isAnterieur(imp)) { //si la vue est ant, on choisi cette image
					this.impAnt = imp;
				}else {
					this.impPost = imp;
				}
			}
		}
		
		this.impProjetee = getZProjection(this.impAnt);
		this.impProjetee.setProperty("Info", this.impAnt.getInfoProperty());
		this.setImp(this.impProjetee);
		VueScin.setCustomLut(this.getImp());
		this.fen_application = new FenApplication(this.getImp(), this.getExamType());
		this.fen_application.setControleur(new Controleur_HepaticDyn(this));
		IJ.setTool(Toolbar.RECT_ROI);
	}

	private ImagePlus getZProjection(ImagePlus imp) {
		return ZProjector.run(imp, "max");
	}

	public ImagePlus getImpAnt() {
		return impAnt;
	}

	public ImagePlus getImpPost() {
		return impPost;
	}

}
