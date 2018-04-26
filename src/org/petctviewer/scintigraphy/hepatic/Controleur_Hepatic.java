package org.petctviewer.scintigraphy.hepatic;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.cardiac.FenResultat_Cardiac;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.MontageMaker;

public class Controleur_Hepatic extends ControleurScin {

	public static String[] organes = { "Liver", "Intestine" };

	protected Controleur_Hepatic(VueScin vue) {
		super(vue);
		this.setOrganes(organes);
		this.setModele(new Modele_Hepatic(vue.getImp()));
	}

	@Override
	public boolean isOver() {
		return this.roiManager.getCount() >= 2;
	}

	@Override
	public void fin() {
		// Copie des rois sur la deuxieme slice
		for (int i = 0; i < 2; i++) {
			this.indexRoi++;
			this.preparerRoi(indexRoi-1);
			this.saveCurrentRoi(this.getNomOrgane(indexRoi), indexRoi);
		}
		
		this.getModele().calculerResultats();		
		ImagePlus imp = this.getVue().getImp();
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {			
				@Override
				public void run() {
					Controleur_Hepatic.this.setSlice(1);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
			
		BufferedImage capture = ModeleScin.captureImage(imp, 400, 400).getBufferedImage();
		
		new FenResultat_Hepatic(this.getVue(), capture);
		//this.getVue().getFen_application().dispose();
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		if (this.getIndexRoi() > 1) {
			return 2;
		} else {
			return 1;
		}
	}

	@Override
	public boolean isPost() {
		return this.getIndexRoi() > 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (this.isPost()) {
			return this.roiManager.getRoi(this.getIndexRoi() - 2);
		}
		return null;
	}

}
