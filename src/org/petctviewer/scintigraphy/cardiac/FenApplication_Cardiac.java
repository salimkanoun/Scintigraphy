package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.FenApplication;

import ij.ImagePlus;

public class FenApplication_Cardiac extends FenApplication {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8986173550839545500L;

	//boutons mode decontamination 
	private Button btn_newCont;
	private Button btn_continue;
	private boolean modeCont;

	public FenApplication_Cardiac(ImagePlus imp, String nom) {
		super(imp, nom);
		this.modeCont = false;

		this.btn_continue = new Button("End");
		this.btn_newCont = new Button("Next");
		this.setInstructions("Delimit the Bladder");
	}
	
	/**
	 * Lance le mode decontamiation, c'est a dire modifier les boutons de la fenetre
	 */
	public void startContaminationMode() {
		this.getInstru().remove(1);
		
		//mise en place des boutons
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 2));
		btns_instru.add(this.btn_newCont);
		btns_instru.add(this.btn_continue);
		this.getInstru().add(btns_instru);
		this.modeCont = true;
		
		this.adaptWindow(256);

		this.setInstructions("Delimit a new contamination");
	}
	
	/**
	 * Remplace les boutons permettant la decontamination par les boutons utilis�s pour d�limiter les rois
	 */
	public void stopContaminationMode() {
		this.getInstru().remove(1);
		this.getInstru().add(this.createBtnsInstru());
		this.adaptWindow(256);
		
		String s = "Delimit the " + this.getControleur().getOrganes()[0];
		this.getField_instructions().setText(s);
		
		ControleurScin ctrl = this.getControleur();
		ctrl.setSlice(ctrl.getSliceNumberByRoiIndex(ctrl.getIndexRoi()));
		this.modeCont = false;
	}
	
	@Override
	public void setControleur(ControleurScin ctrl) {
		super.setControleur(ctrl);
		this.btn_continue.addActionListener(ctrl);
		this.btn_newCont.addActionListener(ctrl);
		this.setInstructions("Delimit a new contamination");
	}
	
	public boolean isModeCont() {
		return this.modeCont;
	}
	
	public Button getBtn_newCont() {
		return this.btn_newCont;
	}

	public Button getBtn_continue() {
		return this.btn_continue;
	}

}
