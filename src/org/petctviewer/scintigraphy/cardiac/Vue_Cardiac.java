package org.petctviewer.scintigraphy.cardiac;
import java.util.ArrayList;

import org.petctviewer.scintigraphy.shunpo.Vue_Shunpo;
import org.petctviewer.scintigraphy.view.FenetreApplication;
import org.petctviewer.scintigraphy.view.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class Vue_Cardiac extends VueScin{

	public Vue_Cardiac() {
		super("Cardiac");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {

		ArrayList<ImagePlus> mountedImage = new ArrayList<ImagePlus>();

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
				mountedImage.add(montageImage);
			} else {
				IJ.log("wrong input, need ant/post image");
			}
			imp.close();
		}

		// si il y a plus de 3 minutes de différence entre les deux prises
		if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
			IJ.log("Warning, frame duration differ by 3 minutes");
		}

		ImagePlus[] mountedSorted = VueScin.orderImagesByAcquisitionTime(mountedImage);
		this.setImp(mountedSorted[0]);
		// Charge la LUT
		VueScin.setCustomLut(this.getImp());

		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		this.fen_application = new FenetreApplication(this.getImp(), this);

		// on affiche la première instruction
		//this.setInstructions(this.leControleur.getListeInstructions()[0]);
		this.fen_application.setInstructions("Samourai shunpo");
		
		IJ.setTool(Toolbar.POLYGON);
	}

}
