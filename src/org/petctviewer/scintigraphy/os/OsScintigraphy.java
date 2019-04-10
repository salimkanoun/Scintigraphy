package org.petctviewer.scintigraphy.os;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.petctviewer.scintigraphy.renal.postMictional.Controleur_PostMictional;
import org.petctviewer.scintigraphy.renal.postMictional.Modele_PostMictional;
import org.petctviewer.scintigraphy.scin.ImageOrientation;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.PlugIn;

public class OsScintigraphy extends Scintigraphy implements PlugIn  {
	
	private String examType;
	
	private ImagePlus[] impAnt, impPost = new ImagePlus[2];
	ImagePlus[][] buffer;
	
	private FenApplication_Os fen_application_os;
	
	private ImagePlus imp;
	
	protected int nombreAcquisitions;
	
	private ModeleScin modele;

	public OsScintigraphy() {
		super("Scinti Os");
	}
	
	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}
	
	public void startExam(ImageOrientation[] selectedImages) throws Exception {
		//Send selected image to specific app to retrieve the ImagePlus to show in the app (will be stored in this object)
		this.imp = preparerImp(selectedImages);
		//Start the program, this overided method should construct the FenApplication, the controller and the model
		this.lancerProgramme();

	}
	
	
	@Override
	public void run(String arg) {
		//SK FAIRE DANS UN AUTRE THREAD ?
		FenSelectionDicom fen = new FenSelectionDicom(this.getExamType(), this);
		fen.setVisible(true);
		fen.pack();
	}
	

	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {

		ImagePlus[] imps = new ImagePlus[2];
		ImagePlus impSorted = null;
		buffer = new ImagePlus[selectedImages.length][2];
		
		for (int i=0 ; i<selectedImages.length; i++) {
			
			ImagePlus imp = selectedImages[i].getImagePlus();
			if(selectedImages[i].getImageOrientation()==ImageOrientation.ANT_POST) {
				impSorted = Library_Dicom.sortImageAntPost(imp);
				
			}else if(selectedImages[i].getImageOrientation()==ImageOrientation.POST_ANT){
				impSorted = Library_Dicom.sortImageAntPost(imp);
				
			}
			else if(selectedImages[i].getImageOrientation()==ImageOrientation.POST) {
				impSorted=imp.duplicate();
			}
			
			for (int j=0 ; j<2; j++) {
				// System.out.println("Show "+j); // Appel bloquant.
				buffer[i][j] = Library_Dicom.splitCameraMultiFrame(impSorted)[j];
				System.out.println("Buffer length : "+ buffer.length);
				System.out.println("Buffer["+i+"] length : "+ buffer[i].length);
			}
			selectedImages[i].getImagePlus().close();
		}
		if(imps[0] != null) {
			this.impPost[0] = imps[0];
		}
		if(imps[1] != null) {
			this.impPost[1] = imps[1];
		}
		
		this.setImp(impSorted);
		
		return impSorted;
	}

	public void lancerProgramme() {
		Overlay ov = Library_Gui.initOverlay(this.getImp());
		Library_Gui.setOverlayGD(ov, this.getImp(), Color.YELLOW);
		
		
		
		FenApplication_Os fen = new FenApplication_Os(this, buffer);
		System.out.println("Fen_Os");
		// fen.setPreferredCanvasSize(950);
		fen.setVisible(true);
		this.setFenApplication_Os(fen);
		this.getImp().setOverlay(ov);
		
		JFrame frame = new JFrame("Results Renal Exam");
		frame.add(fen);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		
		System.out.println("test");
	}
	
	
	
	public void setExamType(String examType) {
		this.examType = examType;
	}
	
	public void setFenApplication_Os(FenApplication_Os fen_application) {
		this.fen_application_os = fen_application;
	}
	
	
	/********************** Getter **************************/
	public ImagePlus getImp() {
		return this.imp;
	}

	public String getExamType() {
		return this.examType;
	}


	public FenApplication_Os getFenApplication_Os() {
		return this.fen_application_os;
	}
	
	public void setModele(ModeleScin modele) {
		this.modele=modele;
	}
	
	public ModeleScin getModele() {
		return modele;
	}

}
