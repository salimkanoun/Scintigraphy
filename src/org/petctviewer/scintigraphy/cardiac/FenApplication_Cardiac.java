package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionListener;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.FenetreApplication;

import ij.ImagePlus;

public class FenApplication_Cardiac extends FenetreApplication {
	
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
		this.btn_newCont = new Button("Save");
		this.setInstructions("Delimit the Bladder");
	}
	
	/**
	 * Lance le mode decontamiation, c'est a dire modifier les boutons de la fenetre
	 */
	public void startContaminationMode() {
		this.instru.remove(1);
		
		//mise en place des boutons
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 2));
		btns_instru.add(this.btn_newCont);
		btns_instru.add(this.btn_continue);
		this.instru.add(btns_instru);
		this.modeCont = true;
		
		this.adaptWindow();

		this.setInstructions("Delimit a new contamination");
	}
	
	/**
	 * Remplace les boutons permettant la decontamination par les boutons utilisés pour délimiter les rois
	 */
	public void stopContaminationMode() {
		this.instru.remove(1);
		this.instru.add(this.createBtnsInstru());
		this.adaptWindow();
		this.setInstructions(0);
		this.getControleur().showSliceWithOverlay(this.getImagePlus().getCurrentSlice());
		this.modeCont = false;
	}

	
	@Override
	public void setControleur(ControleurScin ctrl) {
		super.setControleur(ctrl);
		this.btn_continue.addActionListener(ctrl);
		this.btn_newCont.addActionListener(ctrl);
	}
	
	public boolean isModeCont() {
		return this.modeCont;
	}
	
	public Button getBtn_newCont() {
		return btn_newCont;
	}

	public Button getBtn_continue() {
		return btn_continue;
	}

}
