package org.petctviewer.scintigraphy.lympho;

import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;

import javax.swing.JButton;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.FenApplicationSecondHepaticDyn;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

public class FenApplicationLympho extends FenApplication {
	private static final long serialVersionUID = 1L;
	
	JButton buttonTest;

	public FenApplicationLympho(ImagePlus imp, String nom) {
		super(imp, nom);

		IJ.setTool(Toolbar.RECTANGLE);
		this.imp.setOverlay(Library_Gui.initOverlay(getImagePlus()));
		Library_Gui.setOverlayDG(getImagePlus());
		
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 1));
		buttonTest = new JButton("Load Roi");
		buttonTest.addActionListener(this);
		btns_instru.add(buttonTest);
		this.getPanel_Instructions_btns_droite().add(btns_instru);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if((JButton)e.getSource() == this.buttonTest)
			FenApplicationSecondHepaticDyn.importRoiList(this, this.getControleur().getModel(), this.getControleur());
	}

}
