package org.petctviewer.scintigraphy.generic.statics;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;

public class FenApplication_ScinStatic extends FenApplicationWorkflow {
	private static final long serialVersionUID = 1L;

	public static final String BTN_TEXT_NEW_ROI = "Validate/New Roi", BTN_TEXT_NEXT = "Next";

	private Button btn_finish;

	public FenApplication_ScinStatic(ImageSelection ims, String nom) {
		super(ims, nom);
		this.getTextfield_instructions().setEditable(true);
		this.btn_finish = new Button("Finish");
		this.btn_finish.setActionCommand(ControllerWorkflow.COMMAND_END);

		this.getPanel_Instructions_btns_droite().removeAll();
		Panel instru = new Panel(new GridLayout(1, 2));
		instru.add(new Label("Roi name :"));
		instru.add(this.getTextfield_instructions());
		this.getPanel_Instructions_btns_droite().add(instru);

		Panel btns_instru = new Panel(new GridLayout(1, 3));
		btns_instru.add(this.btn_finish);
		btns_instru.add(this.getBtn_precedent());
		this.getBtn_suivant().setLabel(BTN_TEXT_NEW_ROI);
		btns_instru.add(this.getBtn_suivant());
		this.getPanel_Instructions_btns_droite().add(btns_instru);

		this.setDefaultSize();
	}

	public Button getBtn_finish() {
		return this.btn_finish;
	}

	@Override
	public void setControleur(ControllerScin ctrl) {
		super.setControleur(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}
}
