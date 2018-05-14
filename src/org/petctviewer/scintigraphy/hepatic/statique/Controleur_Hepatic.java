package org.petctviewer.scintigraphy.hepatic.statique;

import java.awt.image.BufferedImage;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import ij.gui.Roi;

public class Controleur_Hepatic extends ControleurScin {

	public static String[] organes = { "Liver", "Intestine" };

	protected Controleur_Hepatic(VueScin vue) {
		super(vue);
		this.setOrganes(organes);
		this.setModele(new Modele_Hepatic(vue.getImp()));
		this.setSlice(1);
	}

	@Override
	public boolean isOver() {
		return this.roiManager.getCount() >= 2;
	}

	@Override
	public void fin() {
		this.setSlice(2);
		VueScin vue = this.getVue();
		// Copie des rois sur la deuxieme slice
		for (int i = 0; i < 2; i++) {
			this.indexRoi++;
			vue.getImp().setRoi(getOrganRoi(indexRoi));
			this.getModele().enregistrerMesure(this.addTag(this.getNomOrgane(indexRoi)), vue.getImp());
		}
		
		this.getModele().calculerResultats();
		
		this.setSlice(1);
		
		Thread captureThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
				    Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				BufferedImage capture = ModeleScin.captureImage(getVue().getImp(), 400, 400).getBufferedImage();
				new FenResultat_Hepatic(getVue(), capture);
				getVue().getFen_application().dispose();
			}
		});
		captureThread.start();
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
