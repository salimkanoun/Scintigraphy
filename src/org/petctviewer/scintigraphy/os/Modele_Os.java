package org.petctviewer.scintigraphy.os;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.Prefs;


/**
 * DISCLAIMER :
 * Dans cette application, il a été fait comme choix d'initialiser le module par le biais du Contrôleur, qui va ensuite créer la vue et le modèle.
 * */
public class Modele_Os {
	
	boolean[][] selected;												// Tableau permettant de savoir quel DynamicImage sont selectionnées
	private ImagePlus[][] imps;											// Tableau à double dimension contenant les ImagePlus liées aux DynamicImage de(s) Scintigraphie(s) Osseuse(s)
	private ImagePlus imp;												// Tableau à double dimension contenant les ImagePlus liées aux DynamicImage de(s) Scintigraphie(s) Osseuse(s)
	DynamicImage[][] dynamicImps;										// Tableau à double dimension contenant les DynamicImage liées aux ImagePlus de(s) Scintigraphie(s) Osseuse(s)
	private int nbScinty;
	private boolean reversed;
	
	
	public Modele_Os(ImagePlus[][] imps) {
		nbScinty = imps.length;
		
		this.reversed = false;

		this.selected= new boolean[nbScinty][2];
		this.dynamicImps = new DynamicImage[nbScinty][2];
		this.imps = new ImagePlus[nbScinty][2];
		this.imps = imps;
		this.imp = this.imps[0][0];
		
		if (!Prefs.get("bone.defaultlut.preferred", true)) {					// Récupération dee la préférence d'application de la Lut (coloration des images). Si il faut appliquer une Lut particulière
			for(ImagePlus[] imgs : imps)
				for(ImagePlus img : imgs)										// Pour toutes les images
					Library_Gui.setCustomLut(img,"lut.preferredforbone");		// Appelle de la méthode permettant d'appliquer la Lut si on applique pas la Lut par défaut.
		}
		
		for(int i = 0; i<nbScinty ; i++){												// For every Scintigraphy
			for (int j=0 ; j<2 ; j++) {													// For ANT and POST of the Scintigraphy
				if (this.dynamicImps[i][j] == null) {									// If it is not already displayed.
					if(this.imps[i][j] != null){
						
						BufferedImage imgbuffered = this.imps[i][j].getBufferedImage();		// Getting Image from the list of ImagePlus
						this.dynamicImps[i][j] = new DynamicImage(imgbuffered);				// Creating the new Panel displaying the Image
						displayInformations(dynamicImps[i][j], i, j);						// Drawing informations in the image
						
					}
				}
			}
		}
		 
		

		
	}
	

	/**
	 * Permmet d'inverser la LUT de chaque image, et donc son contraste.
	 *@param arg0
	 *            
	 * @return
	 */
	public DynamicImage[][] inverser() {
		for(int i = 0; i<nbScinty ; i++){																		// Pour toutes les images
			for (int j=0 ; j<2 ; j++) {
				imps[i][j].setLut(imps[i][j].getLuts()[0].createInvertedLut());									// On inverse la LUT
				dynamicImps[i][j].setImage(imps[i][j].getBufferedImage());										// On recharge lea DynamicImage depuis la ImagePlus correspondante.
				dynamicImps[i][j].repaint();																	// On réaffiche
				displayInformations(dynamicImps[i][j],i,j);														// On affiche les informations (sinon elles disparaissent)
				if(reversed) {
					dynamicImps[i][j].setBackground(Color.black);
				}else {
					dynamicImps[i][j].setBackground(Color.white);
				}
			}
		}
		this.reversed = !this.reversed;
		return dynamicImps;
	}
	
	
	/**
	 * Affiche les information sur une DynamicImage.<br/>
	 * 1 - Récupère les information de l'image correspondante.<br/>
	 * 2 - Charge un Object Graphique, associé à l'ImageDynamic<br/>
	 * 3 - Ecrit un rectangle et la date sur l'Objet Graphique créé<br/>
	 *@param dyn
	 *            DynamicImage sur laquelle écrire.
	 *@param i
	 *            int représentant la position du patient
	 *@param j
	 *            int représentant la position de l'image du patient (0=ANT | 1=POST).
	 * @return
	 */
	public void displayInformations(DynamicImage dyn,int i,int j) {													// Affiche la date de la scintigraphie en bas
		ImagePlus impCurrent = imps[i][j];																			// On récupère l'ImagePlus
		HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(impCurrent);						// On récupère les informations liées à l'ImagePlus
		
		int fontLenght = impCurrent.getWidth()/infoPatient.get("date").length();									// On définit la taille de la police en fonction de la taille de l'image et du texte à écrire
		
		
		Graphics g = dyn.getImage().getGraphics();																	// On crée un objet graphique qui va être appliquer à la Image de notre Dynamic Image
		g.setColor(Color.BLACK);																					// Couleur pour le fond du rectangle
		g.fillRect(4, impCurrent.getHeight()*97/100-fontLenght, infoPatient.get("date").length()*fontLenght/2+3, fontLenght+3);
		g.setColor(Color.white);																					// Couleur pour le texte
		g.setFont(new Font("TimesRoman", Font.PLAIN, fontLenght));													// Font du texte
		g.drawString(infoPatient.get("date"), 5 , impCurrent.getHeight()*97/100);									// On dessine le texte sur l'image
		g.dispose();																								// On applique les dessins sur l'image
		
	}
	
	
	public DynamicImage getDynamicImage(int index) {
		return dynamicImps[index/2][index%2];
	}
	
	public DynamicImage getDynamicImage(int i,int j) {
		return dynamicImps[i][j];
	}
	
	public ImagePlus getImagePlus(int index) {
		return imps[index/2][index%2];
	}
	
	public ImagePlus getImagePlus(int i,int j) {
		return imps[i][j];
	}
	
	 /**
	 * On change le contraste pour toutes les DynamicImage selectionnée,
	 * en parcourant toutes les ImagePlus, en changeant leur LUT,
	 * puis en ré affichant les DynamicImage correspondantes.<br/>
	 *@param sliderValue
	 *            valeur en int du contraste
	 * @return
	 */
	void setContrast(JSlider slider) {
		
		for(int i = 0; i<nbScinty ; i++){																			// Pour toutes les ImagePlus
			for (int j=0 ; j<2 ; j++) {
				if(isSelected(imps[i][j])) {																		// Si l'ImagePlus est selectionée
					imps[i][j].getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - slider.getValue())+1);	// On change son contraste.
				}
			}
		}
		

		SwingUtilities.invokeLater(new Runnable() {																	// Lancement en tache de fond, pour ne pas bloquer le thread principal

			@Override
			public void run() {
				
				for(int i = 0; i<nbScinty ; i++){																	// Pour toutes les DynamicImage
					for (int j=0 ; j<2 ; j++) {
						if(isSelected(dynamicImps[i][j])) {															// Si elle est selectionnée
							dynamicImps[i][j].setImage(imps[i][j].getBufferedImage());								// On récupère l'ImagePlus associée
							dynamicImps[i][j].repaint();															// On l'actualise
							displayInformations(dynamicImps[i][j],i,j);												// On affiche les informations (sinon elles disparaissent)
						}
					}
				}
			}
		});
	}
	
	
	/**
	 * Permet de renseigner la selection ou l'arrêt de sa selection.<br/>
	 * Pour savoir si un ImagePlus et son DynamicImage correspondante est selectionnée, <br/>
	 * un tableau de boolean enregistre les position de chaque ImagePlus et indique si elle est selectionnée ou non.
	 *
	 *@param i
	 *            int représentant la position du patient
	 *@param j
	 *            int représentant la position de l'image du patient (0=ANT | 1=POST).
	 * @return
	 */
	public void  perform(int i, int j) {
		if(selected[i][j]) {
			selected[i][j] = false;
		}else {
			selected[i][j] = true;
		}
	}
	
	/**
	 * Retourne si la imp est selectionnée ou non, grâce à sa position passée en paramètre.
	 * 
	 *@param i
	 *            int représentant la position du patient dans le tableau de ImagePlus
	 *@param j
	 *            int représentant la position de l'image du patient (0=ANT | 1=POST).
	 * @return boolean
	 */
	public boolean isSelected(int i, int j) {
		return this.selected[i][j];
	}
	
	/**
	 * Retourne si la DynamicImage passée en paramètre est selectionnée ou non.<br/>
	 * Récupère d'abord la position via position(DynamicImage)
	 *@param dyn
	 *            DynamicImage dont il faut retourner la position.
	 *@param i
	 *            int représentant la position du patient dans le tableau de DynamicImage
	 *@param j
	 *            int représentant la position de l'image du patient (0=ANT | 1=POST).
	 * @return boolean
	 */
	public boolean isSelected(DynamicImage dyn) {
		int[] position = position(dyn);
		return this.selected[position[0]][position[1]];
	}
	
	/**
	 * Retourne si la DynamicImage passée en paramètre est selectionnée ou non.
	 *@param dyn
	 *            DynamicImage dont il faut retourner la position.
	 * @return boolean
	 */
	public boolean isSelected(DynamicImage dyn,int i, int j) {
		return this.selected[i][j];
	}
	
	/**
	 * Retourne si la ImagePlus passée en paramètre est selectionnée ou non.<br/>
	 * Récupère d'abord la position via position(ImagePlus)
	 *@param imp
	 *            ImagePlus dont il faut retourner la position.
	 * @return boolean
	 */
	public boolean isSelected(ImagePlus imp) {
		return this.selected[position(imp)[0]][position(imp)[1]];
	}
	
	/**
	 * Retourne si la imp passée en paramètre est selectionnée ou non.
	 *@param imp
	 *            ImagePlus dont il faut retourner la position.
	 *@param i
	 *            int représentant la position du patient dans le tableau de ImagePlus
	 *@param j
	 *            int représentant la position de l'image du patient (0=ANT | 1=POST).
	 * @return boolean
	 */
	public boolean isSelected(ImagePlus imp,int i, int j) {
		return this.selected[i][j];
	}
	
	
	/**
	 * Retourne si l'index passé en paramètre identifie une DynamicImage selectionnée.
	 *
	 *@param i
	 *            int représentant la position du patient dans le tableau de ImagePlus
	 *
	 * @return boolean
	 */
	public List<Integer> getSelected() {
		List<Integer> selectionnes = new ArrayList<>();
		for(int i = 0 ; i<this.selected.length*this.selected[0].length ; i++) {
			if(selected[i/2][i%2]) {
				selectionnes.add(i);
			}
		}
		
		return selectionnes;
	}
	
	/**
	 * Parcours le tableau stockant les DynamixImage et retourne la position de la DynamicImage passée en paramètre.
	 *@param dyn
	 *            DynamicImage dont il faut retourner la position dans le tableau stockant les DynamicImage.
	 * @return int[] (Tableau de 2 entiers correspondant aux position dans le tableau à double entrée stockant les ImagePlus)
	 */
	public int[] position(DynamicImage dyn) {
		int[] location = new int[2];
		for (int i=0 ; i<nbScinty ; i++) {
 			for (int j=0 ; j<2 ; j++) {
 				if(dyn == dynamicImps[i][j]){
 					location[0] = i;
 					location[1] = j;
 				}
 			}	
 		}
		return location;
	}
	
	/**
	 * Parcours le tableau stockant les ImagePlus et retourne la position de la ImagePlus passée en paramètre.
	 *@param image
	 *            ImagePlus dont il faut retourner la position dans le tableau stockant les ImagePlus.
	 * @return int[] (Tableau de 2 entiers correspondant aux position dans le tableau à double entrée stockant les ImagePlus)
	 */
	public int[] position(ImagePlus image) {
		int[] location = new int[2];				
		for (int i =0 ;i<nbScinty;i++) {
 			for (int j = 0;j<2;j++) {
 				if(image == imps[i][j]){
 					location[0] = i;
 					location[1] = j;
 				}
 			}	
 		}
		return location;
	}
	
	public int getNbScinti() {
		return imps.length;
	}
	
	public ImagePlus getImp() {
		return this.imp;
	}

}
