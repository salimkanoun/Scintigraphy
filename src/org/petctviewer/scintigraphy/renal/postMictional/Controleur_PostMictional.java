package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Controleur_PostMictional extends ControleurScin{


	protected Controleur_PostMictional(Scintigraphy scin, String[] organes) {
		super(scin);
		this.setOrganes(organes);
		this.setRoiManager(new RoiManager(false));
	}

	@Override
	public boolean isOver() {
		return this.indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void fin() {
		HashMap<String, Double> hm =new HashMap<String, Double>();
		for (int j = 0; j < roiManager.getCount(); j++) {
			scin.getImp().setRoi(getOrganRoi(this.indexRoi));
			String name = this.getNomOrgane(this.indexRoi);
			
			hm.put(name, Library_Quantif.getCounts(scin.getImp()));
			this.indexRoi++;
		}
		( (Modele_PostMictional) this.getScin().getModele()).setData(hm);
		this.getScin().getFenApplication().dispose();
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
	public void notifyClic(ActionEvent arg0) {
	}

}
