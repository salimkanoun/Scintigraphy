package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;

public class PostMictional extends Scintigraphy {

	private String[] organes;

	public PostMictional(String[] organes) {
		super("Scintigraphy");
		this.organes = organes;
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}

		ImagePlus imp = images[0];
		String info = imp.getInfoProperty();
		
		ImagePlus impSorted = Library_Dicom.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);
		
		return impSorted.duplicate();
	}
	
	@Override
	public void lancerProgramme() {
		Overlay ov = Library_Gui.initOverlay(this.getImp());
		Library_Gui.setOverlayGD(ov, this.getImp(), Color.YELLOW);
		
		FenApplication fen = new FenApplication(this.getImp(), this.getExamType());
		fen.setVisible(true);
		this.setFenApplication(fen);
		this.getImp().setOverlay(ov);
		Controleur_PostMictional ctrl = new Controleur_PostMictional(this, this.organes);
		this.getFenApplication().setControleur(ctrl);
		this.setModele(new Modele_PostMictional());
	}

	
	public HashMap<String, Double> getData() {
		return ((Modele_PostMictional) this.getModele()).getData();
	}
	
	public BufferedImage getCapture() {
		return null;
	}
}
