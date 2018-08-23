package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.ImagePlus;

public class FenApplication_Cardiac extends FenApplication {
	
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
		this.getTextfield_instructions().setText("Delimit the Bladder");
		
		this.setPreferredCanvasSize(400);
		
		this.setLocationRelativeTo(null);
	}
	
	/**
	 * Lance le mode decontamiation, c'est a dire modifier les boutons de la fenetre
	 */
	public void startContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);
		
		//mise en place des boutons
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 2));
		btns_instru.add(this.btn_newCont);
		btns_instru.add(this.btn_continue);
		this.getPanel_Instructions_btns_droite().add(btns_instru);
		this.modeCont = true;
		
		this.resizeCanvas();
	}
	
	/**
	 * Remplace les boutons permettant la decontamination par les boutons utilis�s pour d�limiter les rois
	 */
	public void stopContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);
		this.getPanel_Instructions_btns_droite().add(this.createPanelInstructionsBtns());
		
		String s = "Delimit the " + this.getControleur().getOrganes()[0];
		this.getTextfield_instructions().setText(s);
		
		ControleurScin ctrl = this.getControleur();
		ctrl.setSlice(ctrl.getSliceNumberByRoiIndex(ctrl.getIndexRoi()));
		this.modeCont = false;
		
		this.resizeCanvas();
	}
	
	@Override
	public void setControleur(ControleurScin ctrl) {
		super.setControleur(ctrl);
		this.btn_continue.addActionListener(ctrl);
		this.btn_newCont.addActionListener(ctrl);
		this.getTextfield_instructions().setText("Delimit a new contamination");
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
