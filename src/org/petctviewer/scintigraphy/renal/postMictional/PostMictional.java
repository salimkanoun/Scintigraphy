package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.petctviewer.scintigraphy.renal.gui.TabPostMict;
import org.petctviewer.scintigraphy.scin.ImageOrientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;

public class PostMictional extends Scintigraphy {

	private String[] organes;
	private TabPostMict resultFrame;

	public PostMictional(String[] organes, TabPostMict resultFrame) {
		super("Post-mictional");
		this.organes = organes;
		this.resultFrame=resultFrame;
	}

	@Override
	protected ImagePlus preparerImp(ImageOrientation[] images) {
		if (images.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}
		ImagePlus impSorted = null;
		ImagePlus imp = images[0].getImagePlus();
		if(images[0].getImageOrientation()==ImageOrientation.ANT_POST) {
			impSorted = Library_Dicom.sortImageAntPost(imp);
			
		}else if(images[0].getImageOrientation()==ImageOrientation.POST_ANT){
			impSorted = Library_Dicom.sortImageAntPost(imp);
			
		}
		else if(images[0].getImageOrientation()==ImageOrientation.POST) {
			impSorted=imp.duplicate();
		}
			
		

		
		return impSorted;
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
	
	public TabPostMict getResultFrame() {
		return resultFrame;
		
	}
	
	
}
