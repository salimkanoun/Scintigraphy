package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Controleur_PostMictional extends ControleurScin{


	protected Controleur_PostMictional(Scintigraphy scin, String[] organes) {
		
		super(scin);
		for (int i=0; i<organes.length; i++) {
			System.out.println(organes[i]);
		}
		this.setOrganes(organes);
		this.setRoiManager(new RoiManager(false));
	}

	@Override
	public boolean isOver() {
		return this.indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void fin() {
		indexRoi=0;
		HashMap<String, Double> hm =new HashMap<String, Double>();
		for (int j = 0; j < roiManager.getCount(); j++) {
			scin.getImp().setRoi(getOrganRoi(this.indexRoi));
			String name = this.getNomOrgane(this.indexRoi);
			System.out.println(name);
			System.out.println(Library_Quantif.getCounts(scin.getImp()));
			hm.put(name, Library_Quantif.getCounts(scin.getImp()));
			this.indexRoi++;
		}
		( (Modele_PostMictional) this.getScin().getModele()).setData(hm);
		this.getScin().getFenApplication().dispose();
		((PostMictional) this.getScin()).getResultFrame().updateResultFrame();
		
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		Roi roi = this.roiManager.getRoi(indexRoi - 1);
		return roi;
	}

	@Override
	public boolean isPost() {
		return true;
	}

	@Override
	public void preparerRoi(int lastRoi) {
		int index =  getIndexRoi();
		if (index == 1 || index == 3) {
			Roi organRoi= Library_Roi.createBkgRoi(this.getOrganRoi(lastRoi), this.getScin().getImp(),
					Library_Roi.KIDNEY);
			
			this.scin.getImp().setRoi((Roi) organRoi.clone());
			this.scin.getImp().getRoi().setStrokeColor(this.STROKECOLOR);
			int nOrgane = this.indexRoi % this.getOrganes().length;
			this.setInstructionsAdjust(nOrgane);
		}
		

	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Overlay ov = this.getScin().getImp().getOverlay();

		if (ov.getIndex("L. bkg") != -1) {
			Library_Gui.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);
		}

		if (ov.getIndex("R. bkg") != -1) {
			Library_Gui.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
		}
	}
}
