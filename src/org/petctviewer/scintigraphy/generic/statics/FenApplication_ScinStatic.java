package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;

import java.awt.*;

public class FenApplication_ScinStatic extends FenApplicationWorkflow {
	private static final long serialVersionUID = 1L;

	public static final String BTN_TEXT_NEW_ROI = "Validate/New Roi", BTN_TEXT_NEXT = "Next";

	private final Button btn_finish;

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
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}

	@Override
	protected DocumentationDialog createDocumentation() {
		DocumentationDialog doc = super.createDocumentation();

		doc.setDesigner("Esteban BAICHOO");
		doc.setDeveloper("Titouan QUEMA");
		doc.setReference("Salim KANOUN");
		doc.setYoutube("https://www.youtube.com/watch?v=GzteARkk6as&feature=youtu.be");

		return doc;
	}
}
