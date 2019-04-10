package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.gui.Roi;

public class ControleurShunpo_KidneyPulmon extends ControleurScin {

	private Modele_Shunpo modele;

	protected ControleurShunpo_KidneyPulmon(Scintigraphy scin) {
		super(scin);
		
		String[] organes = { "Right lung", "Left lung", "Right kidney", "Left kidney", "Background" };
		this.setOrganes(organes);

		this.modele = new Modele_Shunpo();
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= this.getOrganes().length * 2 - 1 && this.scin.getImp().getSlice() >= 2;
	}

	@Override
	public void fin() {
		// Brain
		this.scin.getFenApplication().getBtn_suivant().setEnabled(true);
		this.scin.getFenApplication().getBtn_precedent().setEnabled(false);
		this.scin.getFenApplication().setControleur(new ControleurShunpo_Brain(this.scin));
	}

	@Override
	public boolean isPost() {
		return false;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return roiIndex < this.getOrganes().length ? 1 : 2;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		Roi roi = this.roiManager.getRoi(this.indexRoi % this.getOrganes().length);
		return roi;
	}

}
