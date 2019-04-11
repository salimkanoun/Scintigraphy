package org.petctviewer.scintigraphy.os;


import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import org.petctviewer.scintigraphy.scin.ImageOrientation;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;
import ij.plugin.PlugIn;

public class OsScintigraphy extends Scintigraphy implements PlugIn  {
	
	private String examType;
	

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
	
    /**
	 * Prépare les images à traiter.<br/>
	 * Dans ce plug-in, les images qui seront envoyées doivent être POST/ANT ou ANT/POST, au nombre maximum de 3<br/>
	 * Dans un premier temps, travaille les images pour mettre le ANT en slice 1 et le POST en slice 2.<br/>
	 * Tri ensuite les ImagePlus par date, avec la plus récente en première.<br/>
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
		buffer = new ImagePlus[selectedImages.length][2];

		ImagePlus impSorted = null;
		ImagePlus[] impsSortedAntPost = new ImagePlus[selectedImages.length];
		
		for (int i=0 ; i<selectedImages.length; i++) {																// Modifie l'ImagePlus pour mettre ANT en slice 1 et POST en slice 2
			impSorted = null;
			ImagePlus imp = selectedImages[i].getImagePlus();
			if(selectedImages[i].getImageOrientation()==ImageOrientation.ANT_POST) {
				impSorted = Library_Dicom.sortImageAntPost(imp);
			}else if(selectedImages[i].getImageOrientation()==ImageOrientation.POST_ANT){
				impSorted = Library_Dicom.sortImageAntPost(imp);
			}
			else if(selectedImages[i].getImageOrientation()==ImageOrientation.POST) {
				impSorted=imp.duplicate();
			}else {
				throw new Exception("Mauvais type d'image choisie.\n Types acceptés : ANT/POST | POST/ANT | POST");
			}
			
			impsSortedAntPost[i]=impSorted;
			selectedImages[i].getImagePlus().close();
		}
		
		
		ArrayList<ImagePlus> arrayBufferForSortByTime = new ArrayList<ImagePlus>(Arrays.asList(impsSortedAntPost));
		ImagePlus[] impsSortedByTime = Library_Dicom.orderImagesByAcquisitionTime(arrayBufferForSortByTime);
		
		int reverseIndex = 0;
		int nbImpsSortedByTime = impsSortedByTime.length;
		ImagePlus tempImp;
        for (reverseIndex = 0 ; reverseIndex < nbImpsSortedByTime / 2 ; reverseIndex++){
        	tempImp = impsSortedByTime[reverseIndex];
                impsSortedByTime[reverseIndex] = impsSortedByTime[nbImpsSortedByTime - reverseIndex - 1];
                impsSortedByTime[nbImpsSortedByTime - reverseIndex - 1] = tempImp;
        }
		
		for (int i=0 ; i<impsSortedByTime.length; i++) {
			for (int j=0 ; j<2; j++) {
				
				ImagePlus Ant = new ImagePlus("Ant", impsSortedByTime[i].getStack().getProcessor(1));
				Ant.setProperty("Info", impsSortedByTime[i].getStack().getSliceLabel(1));
				buffer[i][0] = Ant;
				
				ImagePlus Post = new ImagePlus("Post", impsSortedByTime[i].getStack().getProcessor(2));
				Post.setProperty("Info", impsSortedByTime[i].getStack().getSliceLabel(2));
				buffer[i][1] = Post;
			}
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
