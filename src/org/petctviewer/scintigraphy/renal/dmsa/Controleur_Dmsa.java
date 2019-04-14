package org.petctviewer.scintigraphy.renal.dmsa;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_Dmsa extends Controleur_OrganeFixe {

	public static String[] ORGANES = { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg" };
	private int maxIndexRoi = 0;

	private boolean antPost;
	private boolean over;
	
	protected Controleur_Dmsa(Scintigraphy scin) {
		super(scin);
		
		this.antPost = scin.getImp().getNSlices() == 2;
		if (antPost) {
			this.setSlice(2);
			String[] organes = new String[ORGANES.length];
			for (int i = 0; i < organes.length; i++) {
				organes[i] = ORGANES[i%ORGANES.length];
			}
			this.setOrganes(organes);
		}
		
	}

	@Override
	public boolean isOver() {
		return indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void end() {
		this.over = true;
		//Clear the result hashmap in case of a second validation
		((Modele_Dmsa)this.getScin().getModele()).data.clear();
		this.nomRois.clear();
		
		indexRoi=0;
		BufferedImage capture = Library_Capture_CSV.captureImage(getScin().getImp(), 400, 400).getBufferedImage();
		
		for(Roi roi : this.roiManager.getRoisAsArray()) {
			this.indexRoi++;
			this.getScin().getImp().setRoi(roi);
			String nom = getNomOrgane(indexRoi);
			this.setSlice(1);
			((Modele_Dmsa)this.getScin().getModele()).enregistrerMesure(this.addTag(nom), this.getScin().getImp());
			if(this.antPost) {
				this.setSlice(2);
				((Modele_Dmsa)this.getScin().getModele()).enregistrerMesure(this.addTag(nom), this.getScin().getImp());
				
			}
			
		}
		
		
		this.getScin().getModele().calculerResultats();
		
		new FenResultats_Dmsa(this.getScin(), capture);
		
		this.over=false;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
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
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		maxIndexRoi = Math.max(this.maxIndexRoi, this.indexRoi);
	}
	
	@Override
	public Roi getOrganRoi(int lastRoi) {
		if(this.indexRoi == 1 | this.indexRoi == 3) {
			return Library_Roi.createBkgRoi(this.roiManager.getRoi(indexRoi - 1), this.getScin().getImp(), Library_Roi.KIDNEY);
		}
		return null;
	}

	@Override
	public boolean isPost() {
		if(this.over && antPost) {
			return this.getScin().getImp().getCurrentSlice() != 2;
		}
		return true;
	}

}
