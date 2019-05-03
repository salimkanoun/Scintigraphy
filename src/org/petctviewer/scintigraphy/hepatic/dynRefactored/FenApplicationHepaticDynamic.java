package org.petctviewer.scintigraphy.hepatic.dynRefactored;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Toolbar;

public class FenApplicationHepaticDynamic extends FenApplication {
	
	private Button btn_finish;

	public FenApplicationHepaticDynamic(ImagePlus imp, String nom, ImageCanvas canvas) {
		super(imp, nom, canvas);
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

}
