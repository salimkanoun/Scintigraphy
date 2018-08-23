package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.StaticMethod;

import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_Dmsa extends ControleurScin {

	public static String[] ORGANES = { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg" };
	private int maxIndexRoi = 0;

	private boolean antPost;
	private boolean over;
	
	protected Controleur_Dmsa(Scintigraphy vue) {
		super(vue);
		
		this.antPost = vue.getImp().getNSlices() == 2;
		if (antPost) {
			this.setSlice(2);
			String[] organes = new String[ORGANES.length];
			for (int i = 0; i < organes.length; i++) {
				organes[i] = ORGANES[i%ORGANES.length];
			}
			this.setOrganes(organes);
		}
		
		Modele_Dmsa modele = new Modele_Dmsa();
		this.setModele(modele);
	}

	@Override
	public boolean isOver() {
		return indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void fin() {
		this.over = true;
		
		BufferedImage capture = StaticMethod.captureImage(getScin().getImp(), 400, 400).getBufferedImage();
		
		if(this.antPost) {
			this.setSlice(1);
			for(Roi roi : this.roiManager.getRoisAsArray()) {
				this.indexRoi++;
				this.getScin().getImp().setRoi(roi);
				String nom = this.getNomOrgane(indexRoi);
				this.getModele().enregistrerMesure(this.addTag(nom), this.getScin().getImp());
			}
		}
		
		this.getModele().calculerResultats();
		
		new FenResultats_Dmsa(this.getScin(), capture);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 2;
	}
	
	@Override
	public void setSlice(int indexSlice) {
		super.setSlice(indexSlice);
		this.hideAndColorLabel("R. bkg", Color.GRAY);
		this.hideAndColorLabel("L. bkg", Color.GRAY);
	}

	private void hideAndColorLabel(String name, Color c) {
		Overlay ov = this.getScin().getImp().getOverlay();
		Roi roi = ov.get(ov.getIndex(name));
		if (roi != null) {
			roi.setName("");
			roi.setStrokeColor(c);
		}
	}
	
	@Override
	public void notifyClic(ActionEvent arg0) {
		this.maxIndexRoi = Math.max(this.maxIndexRoi, this.indexRoi);
	}
	
	@Override
	public Roi getOrganRoi(int lastRoi) {
		if(this.indexRoi == 1 | this.indexRoi == 3) {
			return Scintigraphy.createBkgRoi(this.roiManager.getRoi(indexRoi - 1), this.getScin().getImp(), Scintigraphy.KIDNEY);
		}
		return null;
	}

	@Override
	public boolean isPost() {
		if(this.over) {
			return this.getScin().getImp().getCurrentSlice() == 2;
		}
		return true;
	}

}
