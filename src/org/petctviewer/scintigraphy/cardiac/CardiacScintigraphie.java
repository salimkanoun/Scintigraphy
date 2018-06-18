package org.petctviewer.scintigraphy.cardiac;

import java.awt.Color;
import java.util.ArrayList;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class CardiacScintigraphie extends Scintigraphy {

	public CardiacScintigraphie() {
		super("Cardiac");
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {

		ArrayList<ImagePlus> mountedImages = new ArrayList<>();

		int[] frameDuration = new int[2];

		for (int i = 0; i < images.length; i++) {
			ImagePlus imp = images[i];
			if (imp.getStackSize() == 2) {
				String info = imp.getInfoProperty();
				ImagePlus impReversed = Scintigraphy.sortImageAntPost(imp);
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

		ImagePlus[] mountedSorted = Scintigraphy.orderImagesByAcquisitionTime(mountedImages);
		Concatenator enchainer = new Concatenator();

		ImagePlus impStacked;
		// si la prise est early/late
		if (images.length == 2) {
			impStacked = enchainer.concatenate(mountedSorted, false);
			// si il y a plus de 3 minutes de diff�rence entre les deux prises
			if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
				IJ.log("Warning, frame duration differ by "
						+ Math.abs(frameDuration[0] - frameDuration[1]) / (1000 * 60) + " minutes");
			}
		} else {
			impStacked = mountedSorted[0];
		}

		return impStacked.duplicate();
	}

	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(), 7);
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.YELLOW);
		
		// fenetre de l'application
		this.setFenApplication(new FenApplication_Cardiac(this.getImp(), this.getExamType()));
		this.getImp().setOverlay(overlay);
		Controleur_Cardiac ctrl = new Controleur_Cardiac(this);
		this.getFenApplication().setControleur(ctrl);
	}

}