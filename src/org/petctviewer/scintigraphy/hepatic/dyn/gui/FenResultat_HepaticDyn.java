package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.hepatic.dyn.HepaticDynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

public class FenResultat_HepaticDyn extends FenResults {
	private static final long serialVersionUID = 1L;
	
	private final int width = 1000, height = 800;

	public FenResultat_HepaticDyn(HepaticDynamicScintigraphy vue, BufferedImage capture, ControleurScin controller) {
		super(controller);
		
		this.addTab(new TabPrincipal(vue, capture, width, height, this));
		this.addTab(new TabTAC(vue, width, height, this));
		this.addTab(new TabVasculaire(vue, width, height, this));

		this.setPreferredSize(new Dimension(width, height));
	}

}
