package org.petctviewer.scintigraphy.cardiac;

import java.awt.Color;
import java.util.ArrayList;
import org.petctviewer.scintigraphy.scin.VueScin;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class Vue_Cardiac extends VueScin{
	
	public Vue_Cardiac() {
		super("Cardiac");
	}
	
	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		
		ArrayList<ImagePlus> mountedImages = new ArrayList<>();

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

		ImagePlus[] mountedSorted = VueScin.orderImagesByAcquisitionTime(mountedImages);
		Concatenator enchainer = new Concatenator();
		
		ImagePlus impStacked;
		//si la prise est early/late
		if(titresFenetres.length == 2) {
			impStacked = enchainer.concatenate(mountedSorted, false);
			// si il y a plus de 3 minutes de diff�rence entre les deux prises
			if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
				IJ.log("Warning, frame duration differ by " + Math.abs(frameDuration[0] - frameDuration[1]) / (1000 * 60) + " minutes");
			}
		}else {
			impStacked = mountedSorted[0];
		}
		
		Overlay ov = VueScin.initOverlay(impStacked, 7);
		VueScin.setOverlayDG(ov, impStacked, Color.YELLOW);
		
		this.setImp(impStacked.duplicate());
		
		// Charge la LUT
		VueScin.setCustomLut(this.getImp());
		
		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		this.setFenApplication(new FenApplication_Cardiac(this.getImp(), this.getExamType()));
		
		this.getImp().setOverlay(ov);
		
		Controleur_Cardiac ctrl = new Controleur_Cardiac(this);
		this.getFenApplication().setControleur(ctrl);
		
		IJ.setTool(Toolbar.POLYGON);
		
	}

}
