package org.petctviewer.scintigraphy.esophageus.application;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;

import org.petctviewer.scintigraphy.esophageus.EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationDyn;

import ij.ImagePlus;

public class FenApplication_EsophagealTransit extends FenApplication{
 
	private static final long serialVersionUID = 1L;

	private EsophagealTransit esoScinPlugin;
	
	private Button  btn_start;

	
	public FenApplication_EsophagealTransit(ImagePlus imp, EsophagealTransit esoScinPlugin) {
		super(imp,"Eso");
		this.esoScinPlugin = esoScinPlugin;
		
		
		this.getBtn_drawROI().setEnabled(false);

		this.setDefaultSize();

	}
	
	



	

}
