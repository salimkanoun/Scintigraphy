package org.petctviewer.scintigraphy.dynamic;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.petctviewer.scintigraphy.hepatic.dyn.Controleur_HepaticDyn;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.FenApplication;

import ij.ImagePlus;

public class FenApplication_GeneralDyn extends FenApplication{

	private static final long serialVersionUID = 2588688323462231144L;
	
	private Button btn_finish;

	public FenApplication_GeneralDyn(ImagePlus imp, String nom) {
		super(imp, nom);
		this.getField_instructions().setEditable(true);
		
		this.btn_finish = new Button("Finish");
		
		this.getInstru().removeAll();
		Panel instru = new Panel(new GridLayout(1,2));
		instru.add(new Label("Roi name :"));
		instru.add(this.getField_instructions());
		this.getInstru().add(instru);
		
		Panel btns_instru = new Panel(new GridLayout(1, 3));
		btns_instru.add(this.btn_finish);
		btns_instru.add(this.getBtn_precedent());
		btns_instru.add(this.getBtn_suivant());
		this.getInstru().add(btns_instru);
		
		this.adaptWindow();
	}

	public Button getBtn_finish() {
		return btn_finish;
	}
	
	@Override
	public void setControleur(ControleurScin ctrl) {
		super.setControleur(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}
}
