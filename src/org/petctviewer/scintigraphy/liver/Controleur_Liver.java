package org.petctviewer.scintigraphy.liver;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import org.petctviewer.scintigraphy.dynamic.Vue_Dynamic;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.gui.Roi;

public class Controleur_Liver extends ControleurScin {

	public static String[] ORGANES = { "Liver R", "Bkg R", "Liver L", "Bkg L", "Blood Pool" };

	private int maxIndexRoi = 0;

	protected Controleur_Liver(Vue_Dynamic vue) {
		super(vue);
		this.setOrganes(ORGANES);
		this.setModele(new Modele_Liver(vue.getFrameDurations()));
	}

	@Override
	public boolean isOver() {
		return maxIndexRoi == ORGANES.length - 1;
	}

	@Override
	public void fin() {
		System.out.println("fin du programme");
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		this.maxIndexRoi = Math.max(maxIndexRoi, indexRoi);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (this.indexRoi == 1) {
			return this.createBkgRoi(indexRoi, new int[] { -1, 1 });
		}

		if (this.indexRoi == 3) {
			return this.createBkgRoi(indexRoi, new int[] { 1, 1 });
		}

		return null;
	}

	private Roi createBkgRoi(int indexRoi, int[] direction) {
		ImagePlus imp = this.getVue().getImp();

		Roi liver = (Roi) this.roiManager.getRoi(indexRoi - 1).clone();
		Rectangle bounds = liver.getBounds();

		int[] size = { (bounds.width / 4) * direction[0], (bounds.height / 4) * direction[1] };

		Roi liverShifted = (Roi) liver.clone();
		liverShifted.setLocation(liver.getXBase() + size[0], liver.getYBase() + size[1]);
		this.roiManager.addRoi(liverShifted);

		this.roiManager.setSelectedIndexes(new int[] { indexRoi - 1, this.roiManager.getCount() - 1 });
		this.roiManager.runCommand(imp, "XOR");
		this.roiManager.runCommand(imp, "Split");

		int x = bounds.x + bounds.width / 2;
		int y = bounds.y + bounds.height / 2;
		int w = size[0] * imp.getWidth();
		int h = size[1] * imp.getHeight();

		Rectangle splitter;

		if (w > 0) {
			splitter = new Rectangle(x, y, w, h);
		} else {
			splitter = new Rectangle(x + w, y, -w, h);
		}

		Roi rect = new Roi(splitter);
		this.roiManager.addRoi(rect);

		this.roiManager.setSelectedIndexes(new int[] { this.roiManager.getCount() - 1, this.roiManager.getCount() - 2 });
		this.roiManager.runCommand(imp, "AND");

		Roi bkg = (Roi) this.getVue().getImp().getRoi().clone();
		int[] offset = new int[] {size[0]/4, size[1]/4};
		
		for(int i = 0; i < 2; i++) {
			if(offset[i] == 0)
				offset[i] = 1;
		}
		
		bkg.setLocation(bkg.getXBase() + offset[0], bkg.getYBase() + offset[1]);

		// on supprime les rois de construction
		while (this.roiManager.getCount() - 1 > this.maxIndexRoi) {
			this.roiManager.select(this.roiManager.getCount() - 1);
			this.roiManager.runCommand(imp, "Delete");
		}

		return bkg;
	}

	@Override
	public boolean isPost() {
		return true;
	}

}
