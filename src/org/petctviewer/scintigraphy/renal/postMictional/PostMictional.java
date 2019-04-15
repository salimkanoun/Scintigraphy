package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.petctviewer.scintigraphy.renal.gui.TabPostMict;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

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
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) {
		
		ImagePlus impSorted = null;
		if(selectedImages[0].getImageOrientation()==Orientation.ANT_POST) {
			impSorted = Library_Dicom.sortImageAntPost(selectedImages[0].getImagePlus());
			
		}else if(selectedImages[0].getImageOrientation()==Orientation.POST_ANT){
			impSorted = Library_Dicom.sortImageAntPost(selectedImages[0].getImagePlus());
			
		}
		else if(selectedImages[0].getImageOrientation()==Orientation.POST) {
			impSorted=selectedImages[0].getImagePlus().duplicate();
		}
		

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(impSorted, null, null);
		return selection;
	}
	
	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay ov = Library_Gui.initOverlay(selectedImages[0].getImagePlus());
		Library_Gui.setOverlayGD(ov, selectedImages[0].getImagePlus(), Color.YELLOW);
		
		FenApplication fen = new FenApplication(selectedImages[0].getImagePlus(), this.getExamType());
		fen.setVisible(true);
		this.setFenApplication(fen);
		selectedImages[0].getImagePlus().setOverlay(ov);
		Controleur_PostMictional ctrl = new Controleur_PostMictional(this, this.organes);
		this.getFenApplication().setControleur(ctrl);
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
