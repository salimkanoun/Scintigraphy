package org.petctviewer.scintigraphy.shunpo;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.gui.Roi;

public class ControleurShunpo_Brain extends ControleurScin {

	private Modele_Shunpo modele;

	protected ControleurShunpo_Brain(Scintigraphy scin) {
		super(scin);
		
		String[] organes = { "Brain" };
		this.setOrganes(organes);

		this.modele = new Modele_Shunpo();
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= this.getOrganes().length * 2 - 1 && this.scin.getImp().getSlice() >= 2;
	}

	@Override
	public void fin() {
		JOptionPane.showMessageDialog(this.scin.getFenApplication(), "Fin !", "", JOptionPane.INFORMATION_MESSAGE);
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
