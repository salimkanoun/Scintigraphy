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
	
	
	/**
	 * Permet de préparer les images reçu depuis la FenSelectionDicom et ensuite de lancer le programme.
	 * 
	 * @param selectedImages
	 *            liste des images transmises depuis FenSelectionDicom
	 * @return
	 */
	public void startExam(ImageOrientation[] selectedImages) throws Exception {
		//Send selected image to specific app to retrieve the ImagePlus to show in the app (will be stored in this object)
		this.imp = preparerImp(selectedImages);
		//Start the program, this overided method should construct the FenApplication, the controller and the model
		this.lancerProgramme();

	}
	
	/**
	 * Lance la FenSelectionDicom qui permet de selectionner les images qui seront traité par ce plug-in.
	 * 
	 * @param selectedImages
	 *            liste des images transmises depuis FenSelectionDicom
	 * @return
	 */
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
	
    /**
	 * Prépare les images à traiter.<br/>
	 * Dans ce plug-in, les images qui seront envoyées doivent être POST/ANT, au nombre maximum de 3<br/>
	 * Dans un premier temps, trie les images par date.<br/>
	 * Met ensuite les Image ANT/POST dans le bonne ordre au niveau des slice, à savoir Ant puis POST.<br/>
	 * Sépare ensuite les deux slice en deux ImagePlus, en transmettant les informations de l'image originale.<br/>
	 * Enregistre les images dans un buffer, qui sera transmis à la fenêtre traitant les images de Scinty Osseuse.<br/>
	 * Le buffer enregistré est un tableau à double dimension possédant 2 colonnes et n ligne (n = nombre de patient).<br/>
	 * Chaque ligne est un patient. <br/>
	 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus POST du patient.<br/>
	 * @param selectedImages
	 *            liste des images transmises depuis FenSelectionDicom
	 * @return
	 */
	protected ImagePlus preparerImp(ImageOrientation[] selectedImages) throws Exception {
		
		if(selectedImages.length>3)
			throw new Exception("Vous avez rentré trop d'images. Seulement 3 images peuvent être traitées.");
		
		
		ImagePlus[] bufferForSort = new ImagePlus[selectedImages.length];
		for(int IO=0 ; IO<selectedImages.length; IO++) {
			bufferForSort[IO] = selectedImages[IO].getImagePlus();
		}
		ArrayList<ImagePlus> arrayBuffer = new ArrayList<ImagePlus>(Arrays.asList(bufferForSort));
		ImagePlus[] impsSorted = Library_Dicom.orderImagesByAcquisitionTime(arrayBuffer);
		
		
		
		// Arrays.sort(selectedImages);


		ImagePlus[] imps = new ImagePlus[2];
		ImagePlus impSorted = null;
		buffer = new ImagePlus[selectedImages.length][2];
		
		for (int i=0 ; i<selectedImages.length; i++) {
			
			ImagePlus imp = impsSorted[i];
			if(selectedImages[i].getImageOrientation()==ImageOrientation.ANT_POST) {
				impSorted = Library_Dicom.sortImageAntPost(imp);
				
			}else if(selectedImages[i].getImageOrientation()==ImageOrientation.POST_ANT){
				impSorted = Library_Dicom.sortImageAntPost(imp);
				
			}
			else if(selectedImages[i].getImageOrientation()==ImageOrientation.POST) {
				impSorted=imp.duplicate();
			}

			for (int j=0 ; j<2; j++) {
				
				ImagePlus Ant = new ImagePlus("Ant", impSorted.getStack().getProcessor(1));
				Ant.setProperty("Info", impSorted.getStack().getSliceLabel(1));
				buffer[i][0] = Ant;
				
				
				ImagePlus Post = new ImagePlus("Ant", impSorted.getStack().getProcessor(2));
				Post.setProperty("Info", impSorted.getStack().getSliceLabel(2));
				buffer[i][1] = Post;
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

	 /**
		 * Créé un JFrame et la fenêtre qui traitera les images de scinty osseuse<br/>
		 * Cette fenêtre prend en paramètre la classe actuelle (dérivée de Scinty), et le buffer d'images.<br/>
		 * Le buffer enregistré est un tableau à double dimension possédant 2 colonnes et n ligne (n = nombre de patient)<br/>
		 * Chaque ligne est un patient. <br/>
		 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus POST du patient.<br/>
		 * @return
		 */
	public void lancerProgramme() {
		
		FenApplication_Os fen = new FenApplication_Os(this, buffer);
		fen.setVisible(true);
		this.setFenApplication_Os(fen);

		
		JFrame frame = new JFrame("Results Renal Exam");
		frame.add(fen);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
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
