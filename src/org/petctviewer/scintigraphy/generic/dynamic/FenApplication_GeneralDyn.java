package org.petctviewer.scintigraphy.generic.dynamic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FenApplication_GeneralDyn extends FenApplicationWorkflow {
	public static final String BTN_TEXT_NEW_ROI = "Validate & Add ROI", BTN_TEXT_NEXT = "Next";

	private static final long serialVersionUID = 2588688323462231144L;

	private final Button btn_finish;

	public FenApplication_GeneralDyn(ImageSelection ims, String nom) {
		super(ims, nom);
		
		this.setVisualizationEnable(false);

		this.btn_finish = new Button("Validate & Finish");
		btn_finish.setActionCommand(ControllerWorkflow.COMMAND_END);

		this.getPanel_Instructions_btns_droite().removeAll();
		Panel instru = new Panel(new GridLayout(1, 2));
		instru.add(new Label("Input new ROI name:"));
		JTextField tf = this.getTextfield_instructions();
		tf.setBackground(UIManager.getColor("JTextField.background"));
		tf.setEditable(true);
		instru.add(tf);
		this.getPanel_Instructions_btns_droite().add(instru);

		Panel btns_instru = new Panel(new GridBagLayout());
		btns_instru.add(this.getBtn_precedent());
		this.getBtn_suivant().setLabel(BTN_TEXT_NEW_ROI);
		btns_instru.add(this.getBtn_suivant());
		btns_instru.add(this.btn_finish);
		this.getPanel_Instructions_btns_droite().add(btns_instru);
		this.btn_finish.setPreferredSize(this.getBtn_precedent().getSize());
		this.getBtn_suivant().setPreferredSize(new Dimension(115, this.getBtn_precedent().getHeight()));

		this.setDefaultSize();

		// Set default button (when pressing enter)
		this.textfield_instructions.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
					ActionEvent click = new ActionEvent(btn_suivant, ActionEvent.ACTION_PERFORMED, "");
					btn_suivant.dispatchEvent(click);
				}
			}
		});
		// Set default button (when pressing Shift + Enter)
		this.textfield_instructions.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
					ActionEvent click = new ActionEvent(btn_finish, ActionEvent.ACTION_PERFORMED, "");
					btn_finish.dispatchEvent(click);
				}
			}
		});

		this.pack();	
	}

	public Button getBtn_finish() {
		return this.btn_finish;
	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}
}
