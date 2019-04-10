package org.petctviewer.scintigraphy.os;

import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
import ij.gui.TextRoi;
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
	
	Comparator<ImageOrientation> imagePlusDateComparator = new Comparator<ImageOrientation>() {
        @Override
        public int compare(ImageOrientation e1, ImageOrientation e2) {
        	HashMap<String, String> patient1 = Library_Capture_CSV.getPatientInfo(e1.getImagePlus());
        	HashMap<String, String> patient2 = Library_Capture_CSV.getPatientInfo(e2.getImagePlus());
        	/*
        	String date1 = String.join("",patient1.get("date").split("[/]"));
        	String date2 = String.join("",patient2.get("date").split("[/]"));
        	Integer.parseInt(date1)-Integer.parseInt(date2)
        	*/
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyy", Locale.ENGLISH);
        	LocalDate datebis1 = LocalDate.parse(patient1.get("date"), formatter);
        	LocalDate datebis2 = LocalDate.parse(patient2.get("date"), formatter);

            return (datebis1.compareTo(datebis2));
        }
    };
	

	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		
		
		// Arrays.sort(selectedImages);


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
			
			Overlay overlay = Library_Gui.initOverlay(impSorted, 12);
			impSorted.setOverlay(overlay);
			Library_Gui.setOverlayTitle("Post",overlay, impSorted, Color.yellow, 1);
			Library_Gui.setOverlayTitle("Ant",overlay, impSorted, Color.yellow, 2);
			OsScintigraphy.setOverlayBottom(Library_Capture_CSV.getPatientInfo(impSorted).get("date"),overlay, impSorted, Color.yellow, 1);
			OsScintigraphy.setOverlayBottom(Library_Capture_CSV.getPatientInfo(impSorted).get("date"),overlay, impSorted, Color.yellow, 2);
			// impSorted.show();
			for (int j=0 ; j<2; j++) {
				// System.out.println("Show "+j); // Appel bloquant.
				// buffer[i][1] = Library_Dicom.splitCameraMultiFrame(impSorted)[j];
				
				ImagePlus Ant = new ImagePlus("Ant", impSorted.getStack().getProcessor(1));
				Ant.setProperty("Info", impSorted.getStack().getSliceLabel(1));
				buffer[i][0] = Ant;
				
				
				
				ImagePlus Post = new ImagePlus("Ant", impSorted.getStack().getProcessor(2));
				Post.setProperty("Info", impSorted.getStack().getSliceLabel(2));
				buffer[i][1] = Post;
				
				
				Overlay overlay2 = Library_Gui.initOverlay(buffer[i][0], 12);
				buffer[i][0].setOverlay(overlay2);
				OsScintigraphy.setOverlayBottom(Library_Capture_CSV.getPatientInfo(impSorted).get("date"),overlay2, buffer[i][0], Color.yellow, 1);
				Library_Gui.setOverlayTitle("Post",overlay2, buffer[i][0], Color.yellow, 1);
				
				Overlay overlay3 = Library_Gui.initOverlay(buffer[i][1], 12);
				buffer[i][1].setOverlay(overlay3);
				OsScintigraphy.setOverlayBottom(Library_Capture_CSV.getPatientInfo(impSorted).get("date"),overlay3, buffer[i][1], Color.yellow, 1);
				Library_Gui.setOverlayTitle("Ant",overlay3, buffer[i][1], Color.yellow, 1);
				
				System.out.println("Buffer length : "+ buffer.length);
				System.out.println("Buffer["+i+"] length : "+ buffer[i].length);
			}
			// selectedImages[i].getImagePlus().close();
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
		
		FenApplication_Os fen = new FenApplication_Os(this, buffer);
		System.out.println("Fen_Os");
		// fen.setPreferredCanvasSize(950);
		fen.setVisible(true);
		this.setFenApplication_Os(fen);

		
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
	
	
	
	public static void setOverlayBottom(String title, Overlay overlay, ImagePlus imp, Color color, int slice) {
		int w = imp.getWidth();
		int h = imp.getHeight();
	
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
	
		Rectangle2D bounds = overlay.getLabelFont().getStringBounds(title, frc);
		double textHeight = bounds.getHeight();
		double textWidth = bounds.getWidth();
	
		double x = (w / 2) - (textWidth / 2);
		TextRoi bottom = new TextRoi(x, h*0.5, title);
		bottom.setPosition(slice);
		if (color != null) {
			bottom.setStrokeColor(color);
		}
	
		// Set la police des text ROI
		bottom.setCurrentFont(overlay.getLabelFont());
	
		overlay.add(bottom);
	}
	

}
