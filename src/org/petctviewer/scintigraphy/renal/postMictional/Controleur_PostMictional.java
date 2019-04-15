package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_PostMictional extends Controleur_OrganeFixe{


	protected Controleur_PostMictional(Scintigraphy scin, String[] organes) {
		
		super(scin, new Modele_PostMictional());
		for (int i=0; i<organes.length; i++) {
			System.out.println(organes[i]);
		}
		this.setOrganes(organes);
	}

	@Override
	public boolean isOver() {
		return this.indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void end() {
		indexRoi=0;
		HashMap<String, Double> hm =new HashMap<String, Double>();
		for (int j = 0; j < this.model.getRoiManager().getCount(); j++) {
			this.model.getImagePlus().setRoi(getOrganRoi(this.indexRoi));
			String name = this.getNomOrgane(this.indexRoi);
			System.out.println(name);
			System.out.println(Library_Quantif.getCounts(this.model.getImagePlus()));
			hm.put(name, Library_Quantif.getCounts(this.model.getImagePlus()));
			this.indexRoi++;
		}
		( (Modele_PostMictional) this.model).setData(hm);
		this.getScin().getFenApplication().dispose();
		((PostMictional) this.getScin()).getResultFrame().updateResultFrame();
		
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		Roi roi = this.model.getRoiManager().getRoi(indexRoi - 1);
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
			Roi organRoi= Library_Roi.createBkgRoi(this.getOrganRoi(lastRoi), this.model.getImagePlus(),
					Library_Roi.KIDNEY);
			
			this.model.getImagePlus().setRoi((Roi) organRoi.clone());
			this.model.getImagePlus().getRoi().setStrokeColor(this.STROKECOLOR);
			int nOrgane = this.indexRoi % this.getOrganes().length;
			this.setInstructionsAdjust(nOrgane);
		}
		

	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		Overlay ov = this.model.getImagePlus().getOverlay();

		if (ov.getIndex("L. bkg") != -1) {
			Library_Gui.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);
		}

		if (ov.getIndex("R. bkg") != -1) {
			Library_Gui.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
		}
		
	}
}
