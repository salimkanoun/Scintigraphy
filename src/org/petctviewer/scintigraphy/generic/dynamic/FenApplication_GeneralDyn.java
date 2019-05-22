package org.petctviewer.scintigraphy.generic.dynamic;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Toolbar;

public class FenApplication_GeneralDyn extends FenApplicationWorkflow {

	private static final long serialVersionUID = 2588688323462231144L;

	private Button btn_finish;

	public FenApplication_GeneralDyn(ImageSelection ims, String nom, Scintigraphy vue) {
		super(ims, nom);
		this.getTextfield_instructions().setEditable(true);
		this.btn_finish = new Button("Finish");
		btn_finish.setActionCommand(ControllerWorkflow.COMMAND_END);

		this.getPanel_Instructions_btns_droite().removeAll();
		Panel instru = new Panel(new GridLayout(1, 2));
		instru.add(new Label("Roi name :"));
		instru.add(this.getTextfield_instructions());
		this.getPanel_Instructions_btns_droite().add(instru);

		Panel btns_instru = new Panel(new GridLayout(1, 3));
		btns_instru.add(this.btn_finish);
		btns_instru.add(this.getBtn_precedent());
		this.getBtn_suivant().setLabel("Create a new Roi");
		btns_instru.add(this.getBtn_suivant());
		this.getPanel_Instructions_btns_droite().add(btns_instru);

		this.setDefaultSize();
		
		IJ.setTool(Toolbar.RECTANGLE);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		this.pack();	
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
