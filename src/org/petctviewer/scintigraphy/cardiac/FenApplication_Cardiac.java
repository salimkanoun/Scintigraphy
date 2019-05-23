package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;

import javax.swing.JButton;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.FenApplicationSecondHepaticDyn;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplication_Cardiac extends FenApplicationWorkflow {

	private static final long serialVersionUID = -8986173550839545500L;

	// boutons mode decontamination
	private Button btn_newCont;
	private Button btn_continue;
	private boolean modeCont;
	JButton buttonTest;

	public FenApplication_Cardiac(ImageSelection ims, String nom) {
		super(ims, nom);
		this.modeCont = false;

		this.btn_continue = new Button("End");
		this.btn_newCont = new Button("Next");

		this.setPreferredCanvasSize(600);
		this.setLocationRelativeTo(null);
		IJ.setTool(Toolbar.POLYGON);

		this.pack();
	}

	/**
	 * Lance le mode decontamiation, c'est a dire modifier les boutons de la fenetre
	 */
	public void startContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);

		// mise en place des boutons
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 2));
		btns_instru.add(this.btn_newCont);
		btns_instru.add(this.btn_continue);
		this.getPanel_Instructions_btns_droite().add(btns_instru);
		
		btns_instru.setLayout(new GridLayout(1, 1));
		buttonTest = new JButton("Load Roi");
		buttonTest.addActionListener(this);
		btns_instru.add(buttonTest);
		this.modeCont = true;

		this.pack();

	}

	/**
	 * Remplace les boutons permettant la decontamination par les boutons utilis�s
	 * pour d�limiter les rois
	 */
	public void stopContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);
		this.getPanel_Instructions_btns_droite().add(this.createPanelInstructionsBtns());
		this.modeCont = false;
		IJ.setTool(Toolbar.POLYGON);
		this.pack();
	}

	@Override
	public void setControleur(ControllerScin ctrl) {
		super.setControleur(ctrl);
		this.btn_continue.addActionListener(ctrl);
		this.btn_newCont.addActionListener(ctrl);
		this.setText_instructions("Delimit a new contamination");
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if((JButton)e.getSource() == this.buttonTest) {
			FenApplicationSecondHepaticDyn.importRoiList(this, this.getControleur().getModel(), this.getControleur());
//			((ControllerWorkflowCardiac)this.getControleur()).end();
		}
	}

}
