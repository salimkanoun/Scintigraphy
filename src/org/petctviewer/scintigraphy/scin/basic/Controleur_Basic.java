package org.petctviewer.scintigraphy.scin.basic;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Controleur_Basic extends ControleurScin{

	private CustomControleur ctrl;

	protected Controleur_Basic(Scintigraphy vue, String[] organes) {
		super(vue);
		this.setOrganes(organes);
		this.setModele(new Modele_Basic());
		this.setRoiManager(new RoiManager(false));
	}

	@Override
	public boolean isOver() {
		return this.indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void fin() {
		ctrl.fin();
		this.getScin().getFenApplication().dispose();
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		Roi roi = this.roiManager.getRoi(indexRoi - 1);
		return ctrl.getOrganRoi(roi);
	}

	@Override
	public boolean isPost() {
		return true;
	}
	
	@Override
	public void notifyClic(ActionEvent arg0) {
		ctrl.notifyClic(arg0);
	}
	
	public void setCustomControleur(CustomControleur ctrl) {
		this.ctrl = ctrl;
	}

}
