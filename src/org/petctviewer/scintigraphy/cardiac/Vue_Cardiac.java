package org.petctviewer.scintigraphy.cardiac;
import java.util.ArrayList;

import org.petctviewer.scintigraphy.scin.view.FenetreApplication;
import org.petctviewer.scintigraphy.scin.view.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class Vue_Cardiac extends VueScin{
	
	public Vue_Cardiac() {
		super("Cardiac");
	}
	
	private ImagePlus creerMontage(ImagePlus imp) {
		return imp;		
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {

		ArrayList<ImagePlus> mountedImages = new ArrayList<ImagePlus>();

		int[] frameDuration = new int[2];

		for (int i = 0; i < titresFenetres.length; i++) {
			ImagePlus imp = WindowManager.getImage(titresFenetres[i]);
			if (imp.getStackSize() == 2) {
				String info = imp.getInfoProperty();
				ImagePlus impReversed = VueScin.sortImageAntPost(imp);
				MontageMaker mm = new MontageMaker();
				ImagePlus montageImage = mm.makeMontage2(impReversed, 2, 1, 1.0, 1, 2, 1, 0, false);
				montageImage.setProperty("Info", info);
				frameDuration[i] = Integer.parseInt(DicomTools.getTag(imp, "0018,1242").trim());
				mountedImages.add(montageImage);
			} else {
				IJ.log("wrong input, need ant/post image");
			}
			imp.close();
		}

		// si il y a plus de 3 minutes de différence entre les deux prises
		if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
			IJ.log("Warning, frame duration differ by 3 minutes");
		}

		ImagePlus[] mountedSorted = VueScin.orderImagesByAcquisitionTime(mountedImages);
		Concatenator enchainer = new Concatenator();
		ImagePlus impStacked = enchainer.concatenate(mountedSorted, false);
		this.setImp(impStacked);
		
		// Charge la LUT
		VueScin.setCustomLut(this.getImp());

		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		this.fen_application = new FenetreApplication(this.getImp(), this.getExamType());
		Controleur_Cardiac ctrl = new Controleur_Cardiac(this, null);
		this.fen_application.setControleur(ctrl);
		
		IJ.setTool(Toolbar.POLYGON);
	}

}
