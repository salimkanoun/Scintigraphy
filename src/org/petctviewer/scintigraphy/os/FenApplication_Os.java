package org.petctviewer.scintigraphy.os;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.StackWindow;

public class FenApplication_Os extends JPanel implements ChangeListener{
	
	private JSlider slider;
	private Scintigraphy scin;
	private JLabel sliderLabel;
	protected Box boxSlider;
	
	private JPanel grid;
	
	private ImagePlus[][] imps;
	DynamicImage[][] dynamicImps;
	
	boolean[][] selected;
	
	
	private ImagePlus imp;
	private DynamicImage dynamicImp;
	
	protected SidePanel sidePanel;
	String additionalInfo, nomFen;
	
	private int nbScinty;
	
	public boolean reversed;
	
	
	 /**
		 * Constructeur de la fenêtre permettant de visualiser la scintigraphie osseuse.<br/>
		 * Prend en paramètre la fenêtre qui la lance, ainsi que les images du patient.<br/>
		 * Le buffer transmis est un tableau à double dimension possédant 2 colonnes et n ligne (n = nombre de patient).
		 * Chaque ligne est un patient. <br/>
		 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus POST du patient.<br/>
		 * 
		 * Crée un sidePanel, qui affiche quelques informations du patient.<br/>
		 * Appelle finishBuildingWindow() à la fin de l'execution.<br/>
		 * @param selectedImages
		 *            liste des images transmises depuis FenSelectionDicom
		 *@param img
		 *            Tableau à double dimension des images transmises
		 * @return
		 */
	public FenApplication_Os(OsScintigraphy scin, ImagePlus[][] img) {
		super(new BorderLayout());
		
		this.setScin(scin);
		
		this.nbScinty = img.length;
		
		this.reversed = false;
		
		this.additionalInfo = "Info";
		this.nomFen = "Fen";
		
		sidePanel = new SidePanel(null, "Bone scintigraphy", scin.getImp());
		JButton b = new JButton("Inverser");
		ActionListener ad = new inverser();
		b.addActionListener(ad);
		sidePanel.add(b);
		// sidePanel.addCaptureBtn(scin, "_other");
		this.add(sidePanel, BorderLayout.WEST);
		
		this.grid = new JPanel(new GridLayout(1, nbScinty*2));
		
		this.dynamicImps = new DynamicImage[nbScinty][2];
		this.imps = new ImagePlus[nbScinty][2];
		this.imps = img;
		this.imp = this.imps[0][0];
		
		this.selected= new boolean[nbScinty][2];
		
		this.add(grid, BorderLayout.CENTER);
		
		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		
		this.finishBuildingWindow();
	}

	
	
	
	
	 /**
	 * Fini la contruction de la fenêtre.<br/>
	 * Crée un slider et le place. Ce slider servira à faire varier le contraste des images.<br/>
	 * Crée le tableau des DynamicImage à partir des ImagePlus reçues.<br/>
	 * Affete à chaque DynamicImage un Listener, permettant de le selectionner.<br/>
	 * Enregistre la valeure macimale du slider, qui permet de gérer le contraste.<br/>
	 * Crée un gridLayout et ajoute dedans les DynamicImage créés.<br/>
	 *@param img
	 *            Tableau à double dimension des images transmises
	 * @return
	 */
	public void finishBuildingWindow() {

		this.slider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) imp.getStatistics().max, 4);
		slider.addChangeListener(this);

		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);
		
		for(int i = 0; i<nbScinty ; i++){												// For every Scintigraphy
			for (int j=0 ; j<2 ; j++) {													// For ANT and POST of the Scintigraphy
				if (this.dynamicImps[i][j] == null) {									// If it is not already displayed.
					BufferedImage imgbuffered = this.imps[i][j].getBufferedImage();		// Getting Image from the list of ImagePlus
					this.dynamicImps[i][j] = new DynamicImage(imgbuffered);				// Creating the new Panel displaying the Image
					displayInformations(dynamicImps[i][j], i, j);						// Drawing informations in the image
					this.dynamicImps[i][j].addMouseListener(new MouseAdapter() {		// Adding a MouseListener
						@Override
				         public void mousePressed(MouseEvent e) {						// For every click on the object
				        	JPanel di = (JPanel)e.getSource();
				     		for (int i =0 ;i<nbScinty;i++) {							// This loop look for the DynamicImage clicked on the DynamicImage list (dynamicImps)
				     			for (int j = 0;j<2;j++) {
				     				if(di == dynamicImps[i][j]){						// When we found it
				     					imp = imps[i][j];								// We change the current ImagePlus
				     					dynamicImp = dynamicImps[i][j];					// We change the current DynamicImage
				     					perform(dynamicImps[i][j],i,j);					// We perform this DynamicImage
				     				}
				     			}
				     		}
				     		slider.setValue((int) ((slider.getModel().getMaximum() - imp.getLuts()[0].max)+1));		// We put the slider value to the current ImagePlus contrast value.
				     	}
				      });																							// End of the listener
					this.setContrast(this.slider.getValue());														
					grid.add(dynamicImps[i][j]);																	// Adding the DynamicImage to the gridLayout to diplay it.
				}
				
			}
		}
		
		this.dynamicImp = this.dynamicImps[0][0];																	// The basic current DynamicImage is the most recent ANT
		this.setContrast(slider.getValue());
		sidePanel.add(boxSlider);
		// sidePanel.addCaptureBtn(getScin(), this.additionalInfo, new Component[] { this.slider });
		this.add(sidePanel, BorderLayout.WEST);
	}
	
	public ImagePlus getImagePlus() {
		return this.imp;
	}

	public Box getBoxSlider() {
		return this.boxSlider;
	}

	public JSlider getSlider() {
		return this.slider;
	}

	public Scintigraphy getScin() {
		return scin;
	}

	public void setImp(ImagePlus imp) {
		this.imp = imp;
		this.finishBuildingWindow();
	}


	 /**
		 * A chaque modification du slider, on change la valeur du contrast par setContrast(int).<br/>
		 *@param e
		 *            Origine de l'évènement
		 * @return
		 */
	@Override
	public void stateChanged(ChangeEvent e) {	
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider.getValue());
	}
	
	public void setScin(Scintigraphy scin) {
		this.scin = scin;
	}
	
	
	 /**
	 * On change le contraste pour toutes les DynamicImage selectionnée,
	 * en parcourant toutes les ImagePlus, en changeant leur LUT,
	 * puis en ré affichant les DynamicImage correspondantes.<br/>
	 *@param sliderValue
	 *            valeur en int du contraste
	 * @return
	 */
	private void setContrast(int sliderValue) {
		
		for(int i = 0; i<nbScinty ; i++){																			// Pour toutes les ImagePlus
			for (int j=0 ; j<2 ; j++) {
				if(isSelected(imps[i][j])) {																		// Si l'ImagePlus est selectionée
					imps[i][j].getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - sliderValue)+1);	// On change son contraste.
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

	
	
	
	
	 
	public class inverser implements ActionListener{																// Boutton pour inverser le contrast
		/**
		 * Listener permmettant d'inverser la LUT de chaque image, et donc son contraste.
		 *@param arg0
		 *            
		 * @return
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {																// Lors du click sur le boutton
			for(int i = 0; i<nbScinty ; i++){																		// Pour toutes les images
				for (int j=0 ; j<2 ; j++) {
					imps[i][j].setLut(imps[i][j].getLuts()[0].createInvertedLut());									// On inverse la LUT
					dynamicImps[i][j].setImage(imps[i][j].getBufferedImage());										// On recharge lea DynamicImage depuis la ImagePlus correspondante.
					dynamicImps[i][j].repaint();																	// On réaffiche
					displayInformations(dynamicImps[i][j],i,j);														// On affiche les informations (sinon elles disparaissent)
				}
			}
		}
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
	public void displayInformations(DynamicImage dyn,int i,int j) {													// Affiche la date de la scintigraphie en bas.
		ImagePlus impCurrent = imps[i][j];																			// On récupère l'ImagePlus
		HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(impCurrent);						// On récupère les informations liées à l'ImagePlus
		
		int fontLenght = impCurrent.getWidth()/infoPatient.get("date").length();									// On définit la taille de la police
		
		
		Graphics g = dyn.getImage().getGraphics();																	// On crée un objet graphique qui va être appliquer à la Image de notre Dynamic Image
		g.setColor(Color.BLACK);																					// Couleur pour le fond du rectangle
		g.fillRect(4, impCurrent.getHeight()*97/100-impCurrent.getWidth()/infoPatient.get("date").length(), infoPatient.get("date").length()*fontLenght/2+3, impCurrent.getWidth()/infoPatient.get("date").length()+3);
		g.setColor(Color.white);																					// Couleur pour le texte
		g.setFont(new Font("TimesRoman", Font.PLAIN, fontLenght));													// Font du texte
		g.drawString(infoPatient.get("date"), 5 , impCurrent.getHeight()*97/100);									// On dessine le texte sur l'image
		g.dispose();																								// On applique les dessins sur l'image
		
	}
	
	
	public void displayInformations(DynamicImage dyn) {
		displayInformations(dyn,position(dyn)[0],position(dyn)[1]);
	}
	
	
	/**
	 * Permet de renseigner le clique sur une DynamicImage, changeant son cadre et notifiant sa selection ou l'arrêt de sa selection.<br/>
	 * Pour savoir si un ImagePlus et son DynamicImage correspondante est selectionnée, <br/>
	 * un tableau de boolean enregistre les position de chaque ImagePlus et indique si elle est selectionnée ou non.
	 *@param dyn
	 *            DynamicImage dont il faut changer la selection.
	 * @return
	 */
	public void  perform(DynamicImage dyn) {
		perform(dyn,position(dyn)[0], position(dyn)[1]);
	}
	
	/**
	 * Permet de renseigner le clique sur une DynamicImage, changeant son cadre et notifiant sa selection ou l'arrêt de sa selection.<br/>
	 * Pour savoir si un ImagePlus et son DynamicImage correspondante est selectionnée, <br/>
	 * un tableau de boolean enregistre les position de chaque ImagePlus et indique si elle est selectionnée ou non.
	 *@param dyn
	 *            DynamicImage dont il faut changer la selection.
	 *@param i
	 *            int représentant la position du patient
	 *@param j
	 *            int représentant la position de l'image du patient (0=ANT | 1=POST).
	 * @return
	 */
	public void  perform(DynamicImage dyn,int i, int j) {
		if(selected[i][j]) {
			dyn.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 0, 0, Color.black));
			selected[i][j] = false;
			
		}else {
			dyn.setBorder(BorderFactory.createMatteBorder(
                    3, 3, 3, 3, Color.red));
			selected[i][j] = true;
		}
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
}
