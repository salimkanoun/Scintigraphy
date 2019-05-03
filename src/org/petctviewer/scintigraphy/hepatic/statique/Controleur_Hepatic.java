package org.petctviewer.scintigraphy.hepatic.statique;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.gui.Roi;

public class Controleur_Hepatic extends Controleur_OrganeFixe {

	public static String[] organes = { "Liver", "Intestine" };

	protected Controleur_Hepatic(Scintigraphy scin, ImageSelection[] selectedImages, String studyName) {
		super(scin, new Modele_Hepatic(selectedImages, studyName));
		this.setOrganes(organes);
		this.setSlice(1);
	}

	@Override
	public boolean isOver() {
		return this.model.getRoiManager().getCount() >= 2;
	}

	@Override
	public void end() {
		// SK A REVOIR MANQUE LES ROI DE LA PREMIERE SLICE
		this.setSlice(1);

		this.setSlice(2);
		// Copie des rois sur la deuxieme slice
		HashMap<String, Double> data = new HashMap<String, Double>();
		for (int i = 0; i < 2; i++) {
			this.indexRoi++;
			this.model.getImagePlus().setRoi(getOrganRoi(this.indexRoi));

			Double counts = Library_Quantif.getCounts(this.model.getImagePlus());
			data.put(this.addTag(this.getNomOrgane(this.indexRoi)), counts);
		}
		((Modele_Hepatic) this.model).setData(data);
		this.model.calculerResultats();

		this.setSlice(1);

		Thread captureThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				BufferedImage capture = Library_Capture_CSV
						.captureImage(Controleur_Hepatic.this.model.getImagePlus(), 400, 400).getBufferedImage();
				new FenResultat_Hepatic(getScin(), capture, Controleur_Hepatic.this);
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
			return this.model.getRoiManager().getRoi(this.getIndexRoi() - 2);
		}
		return null;
	}

}
