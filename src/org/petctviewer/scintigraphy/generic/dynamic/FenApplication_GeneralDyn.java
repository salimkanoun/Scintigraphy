package org.petctviewer.scintigraphy.generic.dynamic;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.ImagePlus;

public class FenApplication_GeneralDyn extends FenApplication {

	private static final long serialVersionUID = 2588688323462231144L;

	private Button btn_finish;

	public FenApplication_GeneralDyn(ImagePlus imp, String nom, Scintigraphy vue) {
		super(imp, nom);
		this.getTextfield_instructions().setEditable(true);
		this.btn_finish = new Button("Finish");

		this.getPanel_Instructions_btns_droite().removeAll();
		Panel instru = new Panel(new GridLayout(1, 2));
		instru.add(new Label("Roi name :"));
		instru.add(this.getTextfield_instructions());
		this.getPanel_Instructions_btns_droite().add(instru);

		Panel btns_instru = new Panel(new GridLayout(1, 3));
		btns_instru.add(this.btn_finish);
		btns_instru.add(this.getBtn_precedent());
		this.getBtn_suivant().setLabel("Validate/New Roi");
		btns_instru.add(this.getBtn_suivant());
		this.getPanel_Instructions_btns_droite().add(btns_instru);

		this.setDefaultSize();
	}

	public Button getBtn_finish() {
		return this.btn_finish;
	}

	@Override
	public void setControleur(Controleur_OrganeFixe ctrl) {
		super.setControleur(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}
}
