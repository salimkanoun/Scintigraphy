package org.petctviewer.scintigraphy.statics;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.ImagePlus;

public class FenApplication_ScinStatic extends FenApplication{

	private Button btn_finish;
	
	public FenApplication_ScinStatic(ImagePlus imp, String nom) {
		// Scintigraphy.sortImageAntPost(imp)
		super(imp, nom);//pour inverser la post
		this.getField_instructions().setEditable(true);
		this.btn_finish = new Button("Finish");
		
		this.getInstru().removeAll();
		Panel instru = new Panel(new GridLayout(1, 2));
		instru.add(new Label("Roi name :"));
		instru.add(this.getField_instructions());
		this.getInstru().add(instru);

		Panel btns_instru = new Panel(new GridLayout(1, 3));
		btns_instru.add(this.btn_finish);
		btns_instru.add(this.getBtn_precedent());
		btns_instru.add(this.getBtn_suivant());
		this.getInstru().add(btns_instru);

		this.setDefaultSize();
	}
 
	public Button getBtn_finish() {
		return this.btn_finish;
	}

	@Override
	public void setControleur(ControleurScin ctrl) {
		super.setControleur(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}
}

