package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FenApplication_ScinStatic extends FenApplicationWorkflow {
	public static final String BTN_TEXT_NEW_ROI = "Validate/New Roi", BTN_TEXT_NEXT = "Next";
	private static final long serialVersionUID = 1L;
	private final Button btn_finish;

	public FenApplication_ScinStatic(ImageSelection ims, String nom) {
		super(ims, nom);

		// Keep default visualisation
		this.setVisualizationEnable(false);

		this.getTextfield_instructions().setEditable(true);
		this.getTextfield_instructions().setBackground(new Color(225,225,225));
		this.getTextfield_instructions().selectAll();
		this.btn_finish = new Button("Finish");
		this.btn_finish.setActionCommand(ControllerWorkflow.COMMAND_END);

		this.getPanel_Instructions_btns_droite().removeAll();
		Panel instru = new Panel(new GridLayout(1, 2));
		instru.add(new Label("Roi name :"));
		instru.add(this.getTextfield_instructions());
		this.getPanel_Instructions_btns_droite().add(instru);

		Panel btns_instru = new Panel(new GridBagLayout());
		btns_instru.add(this.btn_finish);
		btns_instru.add(this.getBtn_precedent());
		this.getBtn_suivant().setLabel(BTN_TEXT_NEW_ROI);
		btns_instru.add(this.getBtn_suivant());
		this.getPanel_Instructions_btns_droite().add(btns_instru);
		this.btn_finish.setPreferredSize(this.getBtn_precedent().getSize());
		this.getBtn_suivant().setPreferredSize(new Dimension(115, this.getBtn_precedent().getHeight()));

		// Set default button (when pressing enter)
		this.textfield_instructions.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
					ActionEvent click = new ActionEvent(btn_suivant, ActionEvent.ACTION_PERFORMED, "");
					btn_suivant.dispatchEvent(click);
				}
			}
		});
		// Set default button (when pressing Shift + Enter)
		this.textfield_instructions.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
					ActionEvent click = new ActionEvent(btn_finish, ActionEvent.ACTION_PERFORMED, "");
					btn_finish.dispatchEvent(click);
				}
			}
		});

//		this.setDefaultSize();
		this.pack();
	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		this.btn_finish.addActionListener(ctrl);
	}
}
