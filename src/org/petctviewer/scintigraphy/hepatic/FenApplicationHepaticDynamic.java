package org.petctviewer.scintigraphy.hepatic;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.awt.event.MouseListener;

public class FenApplicationHepaticDynamic extends FenApplication {

	private static final long serialVersionUID = 784076354714330098L;
	private final Button btn_finish;
	private final Button labelSlice1;
	private final Button labelSlice2;
	private final Button labelSlice3;
	private final Label buttonSlice1;
	private final Label buttonSlice2;
	private final Label buttonSlice3;

	public FenApplicationHepaticDynamic(ImagePlus imp, String nom) {
		super(imp, nom);

		this.imp.setSlice(1);
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;

		Panel instru = new Panel(gridbag);

		this.labelSlice1 = new Button("Hilium");
		labelSlice1.addMouseListener((ControllerHepaticDynamic) this.getControleur());
		labelSlice1.addActionListener(this);
		gridbag.setConstraints(labelSlice1, c);
		instru.add(labelSlice1);

		this.labelSlice2 = new Button("Duodenum");
		labelSlice2.addMouseListener((ControllerHepaticDynamic) this.getControleur());
		gridbag.setConstraints(labelSlice2, c);
		instru.add(labelSlice2);

		c.gridwidth = GridBagConstraints.REMAINDER; // end row
		this.labelSlice3 = new Button("Intestine");
		labelSlice3.addMouseListener((ControllerHepaticDynamic) this.getControleur());
		gridbag.setConstraints(labelSlice3, c);
		instru.add(labelSlice3);

		c.weightx = 1.0;
		c.gridwidth = 1;
		this.buttonSlice1 = new Label("");
		buttonSlice1.setBackground(new Color(233, 118, 118));
		gridbag.setConstraints(buttonSlice1, c);
		buttonSlice1.addMouseListener((ControllerHepaticDynamic) this.getControleur());
		instru.add(buttonSlice1);

		this.buttonSlice2 = new Label("");
		buttonSlice2.setBackground(new Color(233, 118, 118));
		gridbag.setConstraints(buttonSlice2, c);
		buttonSlice2.addMouseListener((ControllerHepaticDynamic) this.getControleur());
		instru.add(buttonSlice2);

		this.buttonSlice3 = new Label("");
		buttonSlice3.setBackground(new Color(233, 118, 118));
		buttonSlice3.addMouseListener((ControllerHepaticDynamic) this.getControleur());
		gridbag.setConstraints(buttonSlice3, c);
		instru.add(buttonSlice3);

		this.getTextfield_instructions().setEditable(true);
		this.btn_finish = new Button("Finish");
		btn_finish.setActionCommand(ControllerHepaticDynamic.COMMAND_END);

		this.getPanel_Instructions_btns_droite().removeAll();

		GridBagLayout gridbag2 = new GridBagLayout();
		GridBagConstraints c2 = new GridBagConstraints();
		c2.weighty = 2.0;
		c2.gridwidth = GridBagConstraints.REMAINDER; // end row

		gridbag2.setConstraints(instru, c2);

		this.getPanel_Instructions_btns_droite().setLayout(gridbag2);

		this.getPanel_Instructions_btns_droite().add(instru);

		Panel btns_instru = new Panel(new GridLayout(1, 1));
		btns_instru.add(this.btn_finish);
		btn_finish.setActionCommand(ControllerHepaticDynamic.COMMAND_END);
		// btns_instru.add(this.getBtn_precedent());
		// this.getBtn_suivant().setLabel("Select a slice");
		// btns_instru.add(this.getBtn_suivant());

		c2.weighty = 1.0;
		c2.insets = new Insets(10, 0, 0, 0); // top padding
		gridbag2.setConstraints(btns_instru, c2);
		this.getPanel_Instructions_btns_droite().add(btns_instru);

		this.setDefaultSize();

		IJ.setTool(Toolbar.RECTANGLE);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		this.pack();
	}

	public String getTextLabel1() {
		return this.buttonSlice1.getText();
	}

	public String getTextLabel2() {
		return this.buttonSlice2.getText();
	}

	public String getTextLabel3() {
		return this.buttonSlice3.getText();
	}

	public int getLabelNumber(Label label) {
		if (label == buttonSlice1)
			return 1;
		else if (label == buttonSlice2)
			return 2;
		else if (label == buttonSlice3)
			return 3;
		else
			return -1;
	}

	public int getButtonNumber(Button button) {
		if (button == labelSlice1)
			return 1;
		else if (button == labelSlice2)
			return 2;
		else if (button == labelSlice3)
			return 3;
		else
			return -1;
	}

	public void setLabelText(String s, int i) {
		switch (i) {
		case 1:
			buttonSlice1.setText(s);
			break;
		case 2:
			buttonSlice2.setText(s);
			break;
		case 3:
			buttonSlice3.setText(s);
			break;
		}
	}
	
	
	public void setLabelBackground(Color color, int i) {
		switch (i) {
		case 1:
			buttonSlice1.setBackground(color);
			break;
		case 2:
			buttonSlice2.setBackground(color);
			break;
		case 3:
			buttonSlice3.setBackground(color);
			break;
		}
	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		buttonSlice1.addMouseListener((MouseListener) ctrl);
		buttonSlice2.addMouseListener((MouseListener) ctrl);
		buttonSlice3.addMouseListener((MouseListener) ctrl);
		labelSlice1.addMouseListener((MouseListener) ctrl);
		labelSlice2.addMouseListener((MouseListener) ctrl);
		labelSlice3.addMouseListener((MouseListener) ctrl);
		this.btn_finish.addActionListener(ctrl);
	}

}
