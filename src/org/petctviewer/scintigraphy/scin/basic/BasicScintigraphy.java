package org.petctviewer.scintigraphy.scin.basic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;

public class BasicScintigraphy extends Scintigraphy {

	private String[] organes;
	private CustomControleur cusCtrl;

	public BasicScintigraphy(String[] organes, CustomControleur cusCtrl) {
		super("Scintigraphy");
		this.organes = organes;
		this.cusCtrl = cusCtrl;
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}

		ImagePlus imp = images[0];
		String info = imp.getInfoProperty();
		
		ImagePlus impSorted = Scintigraphy.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);
		
		return impSorted.duplicate();
	}
	
	@Override
	public void lancerProgramme() {
		Overlay ov = Scintigraphy.initOverlay(this.getImp());
		Scintigraphy.setOverlayGD(ov, this.getImp(), Color.YELLOW);
		
		FenApplication fen = new FenApplication(this.getImp(), this.getExamType());
		fen.setVisible(true);
		this.setFenApplication(fen);
		this.getImp().setOverlay(ov);
		Controleur_Basic ctrl = new Controleur_Basic(this, this.organes);
		ctrl.setCustomControleur(this.cusCtrl);
		this.getFenApplication().setControleur(ctrl);
	}

	
	public HashMap<String, Double> getData() {
		return ((Modele_Basic) this.getFenApplication().getControleur().getModele()).getData();
	}
	
	public BufferedImage getCapture() {
		return null;
	}
}
