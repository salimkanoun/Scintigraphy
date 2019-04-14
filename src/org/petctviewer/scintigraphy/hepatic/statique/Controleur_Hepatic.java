package org.petctviewer.scintigraphy.hepatic.statique;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.gui.Roi;

public class Controleur_Hepatic extends Controleur_OrganeFixe {

	public static String[] organes = { "Liver", "Intestine" };

	protected Controleur_Hepatic(Scintigraphy scin) {
		super(scin);
		this.setOrganes(organes);
		this.setSlice(1);
	}

	@Override
	public boolean isOver() {
		return this.roiManager.getCount() >= 2;
	}

	@Override
	public void fin() {
		//SK A REVOIR MANQUE LES ROI DE LA PREMIERE SLICE
		this.setSlice(1);
		
		this.setSlice(2);
		// Copie des rois sur la deuxieme slice
		HashMap<String, Double> data =new HashMap<String, Double>();
		for (int i = 0; i < 2; i++) {
			this.indexRoi++;
			scin.getImp().setRoi(getOrganRoi(this.indexRoi));
			
			Double counts = Library_Quantif.getCounts(scin.getImp());
			data.put(this.addTag(this.getNomOrgane(this.indexRoi)), counts);
		}
		((Modele_Hepatic) this.getScin().getModele()).setData(data);
		this.getScin().getModele().calculerResultats();
		
		this.setSlice(1);
		
		Thread captureThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
				    Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				BufferedImage capture = Library_Capture_CSV.captureImage(getScin().getImp(), 400, 400).getBufferedImage();
				new FenResultat_Hepatic(getScin(), capture);
				getScin().getFenApplication().dispose();
			}
		});
		captureThread.start();
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		if (this.getIndexRoi() > 1) {
			return 2;
		}
		return 1;
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
