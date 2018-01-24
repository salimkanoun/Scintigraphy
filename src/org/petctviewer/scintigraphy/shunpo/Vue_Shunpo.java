/**
Copyright (C) 2017 MOHAND Mathis and KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy.shunpo;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.plugin.Concatenator;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.LUT;
import ij.util.DicomTools;

public class Vue_Shunpo implements PlugIn {

	protected HashMap<String, Button> lesBoutons;

	private boolean imageOuverte = false;

	private Label img_inst;

	private Controleur_Shunpo leControleur = new Controleur_Shunpo(this, new Modele_Shunpo());
	
	protected Overlay overlay ;
	
	private Label[] labRes ;
	
	protected Label Csv = new Label();
	
	protected RoiManager leRoi;
	
	protected Label instructions;

	protected CustomWindow res;
	
	protected CustomStackWindow win;
	
	protected ImagePlus imp;

	protected static boolean image2Ouverte;
	
	private Frame f;
	 
	
	@Override
	public void run(String arg) {
		// Initialisation des differents attributs
		RoiManager rm=new RoiManager(false);
		leRoi=rm;
		instructions = new Label("");
		instructions.setBackground(Color.LIGHT_GRAY);
		img_inst = new Label();
		labRes = new Label[10] ;
		for (int i = 0 ; i < 10 ; i++)
			labRes[i] = new Label("") ;
		initBoutons();
		addEcouteurs();
		lesBoutons.get("Valider").addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Button b = (Button) arg0.getSource();
				if (WindowManager.getCurrentImage() != null && WindowManager.getCurrentImage() != imp && imageOuverte == true) {
					ImagePlus imp=WindowManager.getCurrentImage();
					ouvertureImageBrain(imp) ;
					((Frame) b.getParent().getParent()).dispose();
				}
				if (WindowManager.getCurrentImage() != null && imageOuverte == false) {
					imageOuverte = true;
					ImagePlus imp=WindowManager.getCurrentImage();
					ouvertureImage(imp) ;
					((Frame) b.getParent().getParent()).dispose();
				}
				
				
			}

		});
		//Methode de dèŒ…marrage automatique mais pas trçŒ«s intèŒ…rèŒ…ssante
		//if (WindowManager.getCurrentImage() != null) {
		//	ImagePlus imp=WindowManager.getCurrentImage();
		//	imageOuverte = true ;
		//	ouvertureImage(imp); 
		//}
		if (!imageOuverte)
			ouvrirImage("Lungs - Kidneys");
	}
		   
		

	private void initBoutons() {

		lesBoutons = new HashMap<>();
		lesBoutons.put("Show Log", new Button(" Show Log "));
		lesBoutons.put("Draw ROI", new Button(" Draw ROI "));
		lesBoutons.put("Quitter", new Button(" Quit "));
		lesBoutons.put("Contrast", new Button(" Contrast "));
		lesBoutons.put("Precedent", new Button(" Previous "));
		lesBoutons.put("Suivant", new Button(" Next "));
		lesBoutons.put("Contraste", new Button(" Contrast "));
		lesBoutons.put("Valider", new Button(" Confirm "));
		lesBoutons.put("Capture", new Button(" Capture "));
		
		
	}
	
	private void addEcouteurs() {
		for (Entry<String, Button> entry : lesBoutons.entrySet())
			entry.getValue().addActionListener(leControleur);
	}

	protected void setInstructions(String inst) {
		instructions.setText(inst);
	}
	
	// Extraite du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	class CustomCanvas extends ImageCanvas {

		private static final long serialVersionUID = -5710708795558320699L;

		CustomCanvas(ImagePlus imp) {
			super(imp);
			} 
		} // Fin CustomCanvas
	

	// Extraite puis modifièŒ…e du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	// Cette classe permet d'avoir une image et des èŒ…lèŒ…ments graphiques sur une
	// fené”štre
	// On l'utilise pour la fené”štre de rèŒ…sultats
	
	class CustomWindow extends ImageWindow{

		private static final long serialVersionUID = -9097595151860174657L;
		

		CustomWindow(ImagePlus imp) {
			super(imp, new CustomCanvas(imp));
			addPanel();
		} 
		// Fin constructeur CustomWindow

		// Permet d'ajouter les boutons è„¿ la fené”štre
		
		private void addPanel() {
			// Agencement des composants
			Panel panel = new Panel();
			panel.setLayout(new FlowLayout());

			Panel resultats = new Panel();
			resultats.setLayout(new GridLayout(6, 2));
			for (int i = 0 ; i < 10 ; i++) {
				resultats.add(labRes[i]) ;
				resultats.setSize(resultats.getMaximumSize());
			}
			Panel csv = new Panel();
			csv.setLayout(new GridLayout(1, 1));
			
			//On test la prèŒ…sence d'un repertoire CSV defini
			String path = Prefs.get("dir.preferred", null);
			if (path==null){
				Csv.setText("No CSV output");
				Csv.setForeground(Color.RED);
			}
			else{
				Csv.setText("CSV Save OK");
			}
			csv.add(Csv);
			Panel Capture = new Panel();
			Capture.setLayout(new GridLayout(1, 1));
			Button capture=lesBoutons.get("Capture");
			Capture.add(capture);
			resultats.add(csv);
			resultats.add(Capture);
			panel.add(resultats) ;
			add(panel);
			pack();
			
			// Permet d'avoir la fenetre ouverte au meme endroit que l'image
			// selectionee par l'utilisateur
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Point loc = getLocation();
			Dimension size = getSize();
			if (loc.y + size.height > screen.height)
				getCanvas().zoomOut(0, 0);

		} // Fin addPanel

	} // Fin CustomWindow

	// Extraite puis modifièŒ…e du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	// Cette classe permet d'avoir une pile d'images et des èŒ…lèŒ…ments graphiques
	// sur une fené”štre
	
	class CustomStackWindow extends StackWindow {

		private static final long serialVersionUID = -6280620624574294247L;

		CustomStackWindow(ImagePlus imp) {
			super(imp, new CustomCanvas(imp));
			addPanel();
		} // Fin constructeur CustomStackWindow

		// Construction de l'interface
		void addPanel() {
			// Agencement des composants
			Panel panel = new Panel();
			panel.setLayout(new FlowLayout());
			//addEcouteurs();

			// Partie gauche
			Panel gauche = new Panel();
			gauche.setLayout(new FlowLayout());

			// Premiers boutons
			Panel btns_glob = new Panel();
			btns_glob.setLayout(new GridLayout(1, 3));
			btns_glob.add(lesBoutons.get("Quitter"));
			btns_glob.add(lesBoutons.get("Draw ROI"));
			btns_glob.add(lesBoutons.get("Contrast"));
			gauche.add(btns_glob);

			// Instructions
			Panel instru = new Panel();
			instru.setLayout(new GridLayout(2, 1));
			instru.add(instructions);
			Panel btns_instru = new Panel();
			btns_instru.setLayout(new GridLayout(1, 3));
			btns_instru.add(lesBoutons.get("Show Log"));
			btns_instru.add(lesBoutons.get("Precedent"));
			lesBoutons.get("Precedent").setEnabled(false);
			btns_instru.add(lesBoutons.get("Suivant"));
			instru.add(btns_instru);
			gauche.add(instru);
			panel.add(gauche);
			add(panel);
			pack();

			// Permet d'avoir la fené”štre ouverte au meme endroit que l'image
			// selectionnee par l'utilisateur
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); 
			Point loc = getLocation();
			Dimension size = getSize();
			if (loc.y + size.height > screen.height)
				getCanvas().zoomOut(0, 0);
		} // Fin addPanel
		
		 @Override
         public void windowClosing(WindowEvent we) {
			 //On ferme le ROI manager en plus de la fenetre
			 leRoi.close();
			 win.close();
			 System.gc();
         }
		
	} // Fin CustomStackWindow

	//Ouvre le dialog pour charger une image
	protected void ouvrirImage(String image) {
		f = new Frame();
		Panel pan = new Panel();
		pan.setLayout(new GridLayout(2, 1));
		img_inst.setText("Please open the " + image + " image then confirm.");
		pan.add(img_inst);
		pan.add(lesBoutons.get("Valider"));
		f.add(pan);
		f.setLocationRelativeTo(null);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		f.setSize(200, 300);
		f.pack();
		f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);
		f.setVisible(true);
		f.setResizable(false);
		//tj au dessus Pour eviter un click qui ferait perdre la fenetre
		f.setAlwaysOnTop(true);
		f.toFront();
		
		f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we) {
				end("dialog");
				f.dispose();
			}
		});
		
		if (imageOuverte==true){
			lesBoutons.get("Suivant").setEnabled(false);
		}
		
	}
	
	private void ouvertureImage(ImagePlus imp) {
		int nombreImage=imp.getStackSize();
		if (nombreImage!=2) IJ.showMessage("Wrong Input, number of image should be 2, please restart");
		//Tri des image dans une nouvelle imageplus
		ImagePlus imp2=sortImageAntPost(imp);
		imp2.show();
		imp.close();
		//Suite du programme sur la nouvelle imageplus
		this.imp=imp2;
		//Charge la LUT
		setCustomLut(this.imp);
		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une fenetre pour la pile d'images;
		CustomStackWindow win = new CustomStackWindow(this.imp);
		this.win=win;
		win.setTitle(setTitre(win.getImagePlus()));
		this.imp.setTitle(setTitre(this.imp));
		//  On affiche l'image en 512*512 en forcant le zoom adhoc	
		win.getCanvas().setSize(new Dimension(512,512));
		// Adaptation automatique de l'image au resize
		win.getCanvas().setScaleToFit(true);
		//On Pack la fenetre pour la mettre a la preferred Size
		win.pack();
		win.setSize(win.getPreferredSize());
		//On met au premier plan au centre de l'ecran
		win.setLocationRelativeTo(null);
		win.toFront();
		//On initialise l'overlay
		this.overlay=initOverlay();
		//On ajouter l'overlay Droite/Gauche
		Vue_Shunpo.setOverlayDG(overlay, win.getImagePlus());
		//On met sur l'image
		win.getImagePlus().setOverlay(overlay);
		if (instructions.getText().equals("")) 
			instructions.setText("Delimit the right lung.");
		IJ.setTool(Toolbar.POLYGON);
		win.showSlice(2);
	}
	
	// Definit le titre de la forme : ShunPo - XXXXX Xxxxx - organe et renvoit la string titre
	private String setTitre(ImagePlus imp) {
		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = "ShunPo - " ;
		titre = titre + tagNom +  " - " + tagSerie ;
		return titre ;
	}
	
	// Interface graphique pour resultats
	protected void UIResultats(ImagePlus screen) {
		//On cree la fenetre resultat avec le panel resultat
		res = new CustomWindow(screen);
		//On resize la window pour laisser la place a l'image et au pannel
		//Ici on ajoute que 70 pixel en hauteur car il n'y a pas l'ascenseur horizontal du stack
		res.setLocationRelativeTo(null);
		res.getCanvas().setMagnification(1.0);
		res.getCanvas().setScaleToFit(true);
		res.getCanvas().hideZoomIndicator(true);
		res.pack();
		res.setSize(res.getPreferredSize());
		//On prend le focus
		res.toFront();
		//On implemente le titre de la fenetre
		res.setTitle("Pulmonary Shunt - Results");
		//On quitte l'outil ROI pour Hand (evite d'avoir le curseur qui fait des ROI)
		IJ.setTool("hand");
	}
	
	// routine de fermeture du programme
	protected void end(String dialog) {
		if (dialog=="dialog" && imageOuverte==false){
			leRoi.close();
			f.dispose();
			System.gc();
		}
		
		if (dialog=="dialog" && imageOuverte==true){
			int optionChoisie = JOptionPane.showConfirmDialog(null, "The program will now shut down", "", JOptionPane.OK_CANCEL_OPTION) ;
			if (optionChoisie == JOptionPane.OK_OPTION) {
				f.dispose();
				win.close();
				leRoi.close();
				System.gc();
			}
			
		}
		if (dialog==null){
			int optionChoisie = JOptionPane.showConfirmDialog(null, "The program will now shut down", "", JOptionPane.OK_CANCEL_OPTION) ;
			if (optionChoisie == JOptionPane.OK_OPTION) {
				leRoi.close();
				win.close();
				System.gc();
			}
		}
		
	}
	
	
	/**
	 * Applique la LUT definie dans les preference à l'ImagePlus demandee
	 * @param imp : L'ImagePlus sur laquelle on va appliquer la LUT des preferences
	 */
	public static void setCustomLut(ImagePlus imp){
		String lalut = Prefs.get("lut.preferred", null) ;
		if (lalut != null) {
		LUT lut = ij.plugin.LutLoader.openLut(lalut);
		imp.setLut(lut);
		}
	}
	
	//Injecte une nouvelle imageplus dans la fenetre existante
	private void ouvertureImageBrain(ImagePlus imp) {
			//On reactive le boutton suivant
			lesBoutons.get("Suivant").setEnabled(true);
			//On ordonne les images dans une nouvelle imageplus
			ImagePlus cerveau2=sortImageAntPost(imp);
			//On injecte l'image cerveau dans la fenetre
			win.setImage(cerveau2);
			imp.getWindow().close();
			//On applique la LUT des preference si presente
			setCustomLut(cerveau2);
			//On set le Titre
			cerveau2.setTitle(setTitre(cerveau2));
			win.setTitle(setTitre(cerveau2));
			//on set le canvas et on repaint
			win.getCanvas().fitToWindow();
			win.repaint();
			win.getImagePlus().killRoi();
			//On ajouter l'overlay Droite/Gauche
			this.overlay=initOverlay();
			Vue_Shunpo.setOverlayDG(overlay, win.getImagePlus());
			win.getImagePlus().setOverlay(overlay);
			//Variable pour notifier que l'image 2 est ouverte
			image2Ouverte=true ;
			//On envoi un event au controleur pour passer è„¿ l'èŒ…tat suivant
			ActionEvent e = new ActionEvent(lesBoutons.get("Suivant"), ActionEvent.ACTION_PERFORMED, "Suivant");
			leControleur.actionPerformed(e);
			}
	
	// Remplis les labels des resultats
	protected void labelsResultats(String[] resultats) {
		for (int i = 0 ; i < resultats.length ; i++) {
			labRes[i].setText(resultats[i]);}
		if (Modele_Shunpo.shunt<2) {
			//Si shunt inférieur à 2% (examen normal) Affiche en vert
			labRes[7].setForeground(new Color(0,89,0));
		}
		if (Modele_Shunpo.shunt<5 && Modele_Shunpo.shunt>2) {
			//Affiche en Orange
			labRes[7].setForeground(new Color(229,148,0));
		}
		if (Modele_Shunpo.shunt>5) {
			//Affiche en Rouge
			labRes[7].setForeground(new Color(230,0,0));
		}
		//Affiche le nom en gras
		labRes[9].setFont(new Font ("Arial", Font.BOLD, 12));
	}
	
	/**
	 * Cree overlay et set la police
	 * SK : A Optimiser pour tenir compte de la taille initiale de l'Image
	 * @return Overlay
	 */
	public static Overlay initOverlay() {
		//On initialise l'overlay il ne peut y avoir qu'un Overlay
		// pour tout le programme sur lequel on va ajouter/enlever les ROI au fur et a mesure
		Overlay overlay = new Overlay();
		Font font = new Font("Arial",Font.PLAIN, 19) ;
		overlay.setLabelFont(font);
		overlay.drawLabels(true);
		overlay.drawNames(true);
		return overlay;
	}
	
	/**
	 * Affiche D et G en overlay sur l'image
	 * @param overlay : Overlay sur lequel ajouter D/G
	 * @param imp : ImagePlus sur laquelle est appliquée l'overlay
	 */
	public static void setOverlayDG(Overlay overlay, ImagePlus imp) {
		//Position au mileu dans l'axe Y
		double y=((imp.getHeight())/2);
		// Cree police
		Font font = new Font("Arial",Font.PLAIN, 10) ;
		
		//Cote droit
		TextRoi right = new TextRoi(0, y, "R");
		right.setCurrentFont(font);
		
		//Cote gauche
		String labelLeft="L";
		double xl = imp.getWidth()-(font.getSize()*labelLeft.length()); // sinon on sort de l'image
		TextRoi left = new TextRoi(xl, y, labelLeft);
		left.setCurrentFont(font);
		
		// Set de la couleur et de la police des text ROI
		TextRoi.setColor(Color.WHITE);
		
		// Ajout de l'indication de la droite du patient
		overlay.add(right);
		overlay.add(left);
	}
		
	/**
	 * Permet de savoir si l'ImagePlus vient d'une Image MultiFrame (teste l'Image 1)
	 * @param imp : L'ImagePlus a tester
	 * @return : vrai si multiframe
	 */
	public static boolean isMultiFrame(ImagePlus imp) {
		//On regarde la coupe 1
		imp.setSlice(1);
		
		//Regarde si frame unique ou multiple
		String numFrames = DicomTools.getTag(imp, "0028,0008");
		if (numFrames!=null && !numFrames.isEmpty()) numFrames=numFrames.trim();
		
		//On passe le texte en Int
		int slices=Integer.parseInt(numFrames);
		
		if (slices==1) return false;
		else return true;
		
	}
	
	/**
	 * Permet de trier les image Anterieure et posterieure et retourne les images posterieures pour garder la meme lateralisation (la droite est à gauche de l'image comme une image de face)
	 * @param imp : ImagePlus a trier 
	 * @return Retourne l'ImagePlus avec les images posterieures inversees
	 */
	public static ImagePlus sortImageAntPost(ImagePlus imp) {
		ImagePlus imp2=null;
		if (isMultiFrame(imp)) {
			imp2=sortAntPostMultiFrame(imp);
		}
		if (!isMultiFrame(imp)) {
			imp2=sortAntPostUniqueFrame(imp);
		}
		return imp2;
	}
	
	/**
	 * Permet de tirer et inverser les images posterieure pour les images multiframe
	 * A Eviter d'utiliser, préférer la methode sortImageAntPost(ImagePlus imp) qui est générique pour tout type d'image
	 * @param imp0 : ImagePlus a trier
	 * @return Retourne l'ImagePlus triee
	 */
	@Deprecated
	public static ImagePlus sortAntPostMultiFrame(ImagePlus imp0) {
			//On duplique pour faire les modifs dans l'image dupliqué”Ÿçµœ
			ImagePlus imp=imp0.duplicate();
			
			//On prend le Header
			String metadata=imp.getInfoProperty();
			
			//On recupere la chaine de vue
			String tag = DicomTools.getTag(imp, "0011,1012");
			if (DicomTools.getTag(imp, "0011,1030")!=null)		tag+=DicomTools.getTag(imp, "0011,1030");
			
			// TAG 0011, 1012 semble absent de SIEMENS, TROUVER D AUTRE EXAMPLE POUR STATUER
			//Si pas de tag
			if (tag==null) tag="no tag";
			// On recupere la chaine de detecteur
			String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
			if (tagDetecteur!=null && !tagDetecteur.isEmpty()) tagDetecteur=tagDetecteur.trim();
			String delims = "[ ]+";
			String[] sequenceDeteceur = tagDetecteur.split(delims);
			
			///On recupere le 1er separateur de chaque vue dans le champ des orientation
			int separateur=tag.indexOf("\\");
			//Si on ne trouve pas le separateur, on met la position du separateur ï¿½ la fin de la string pour tout traiter
			if (separateur==-1) separateur=(tag.length());
			
			// Si la 1ere image est labelisee anterieure
			if (tag.substring(0, separateur).contains("ANT") || tag.substring(0, separateur).contains("_E")) {
				//On recupere le numé”Ÿçµ©o du detecteur
				int detecteurAnterieur=Integer.parseInt(sequenceDeteceur[0]);
				// On parcours la sequence de detecteur et on flip é”Ÿï¿½ chaque fois que ce n'est pas le numé”Ÿçµ©o de ce deteceur
				for (int j=0; j<sequenceDeteceur.length; j++) {
					int detecteur=Integer.parseInt(sequenceDeteceur[j]);
						if (detecteur!=detecteurAnterieur) {
						imp.getStack().getProcessor(j+1).flipHorizontal();
						}	
					}
			}
			
			//Si la 1ere image est labelisee posterieurs
			if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
				//on ré”Ÿçµšupere le numé”Ÿçµ©o du detecteur posterieur
				int detecteurPosterieur=Integer.parseInt(sequenceDeteceur[0]);
				// On parcours la sequence de detecteur et on flip é”Ÿï¿½ chaque fois que ca correspond é”Ÿï¿½ ce deteceur
				for (int j=0; j<sequenceDeteceur.length; j++) {
					int detecteur=Integer.parseInt(sequenceDeteceur[j]);
						if (detecteur==detecteurPosterieur) {
						imp.getStack().getProcessor(j+1).flipHorizontal();
						}	
					}
			}
			
			//Si on ne trouve pas de tag on flip toute detecteur 2 et on notifie l'utilisateur
			if (!tag.substring(0, separateur).contains("POS") && !tag.substring(0, separateur).contains("_F") &&!tag.substring(0, separateur).contains("ANT") &&!tag.substring(0, separateur).contains("_E")) {
				IJ.log("No Orientation tag found, assuming detector 2 is posterior. Please Notify Salim.Kanoun@gmail.com");
				for (int j=0; j<sequenceDeteceur.length; j++) {
					int detecteur=Integer.parseInt(sequenceDeteceur[j]);
						if (detecteur==2) {
						imp.getStack().getProcessor(j+1).flipHorizontal();
						}	
				}				
			}
			
			ImagePlus[] pileImage=new ImagePlus[imp.getStackSize()];
			
			for (int j=0; j<imp.getStackSize();j++) {
				pileImage[j]= new ImagePlus();
				pileImage[j].setProcessor(imp.getStack().getProcessor(j+1));
				pileImage[j].setProperty("Info", metadata);
				pileImage[j].setTitle("Image"+j);
			}
			
			Concatenator enchainer = new Concatenator();
			ImagePlus imp2=enchainer.concatenate(pileImage, false);
			//ImagePlus imp2 = enchainer.concatenate(impAnt,impPost, false);
			//On retourne le resultat
			return imp2;
			
		}

	/**
	 * Permet de trier les image unique frame et inverser l'image posterieure
	 * A Eviter d'utiliser, préférer la methode sortImageAntPost(ImagePlus imp) qui est générique pour tout type d'image
	 * @param imp0 : ImagePlus a trier
	 * @return retourne l'ImagePlus trier
	 */
	@Deprecated
	public static ImagePlus sortAntPostUniqueFrame(ImagePlus imp0) {
			//On copie dans une nouvelle image qu'on va renvoyer
			ImagePlus imp=imp0.duplicate();
			
			//Si unique frame on inverse toute image qui contient une image posté”Ÿçµ©ieure
				for (int i = 1; i <= imp.getImageStackSize(); i++) {
				imp.setSlice(i);
				String tag = DicomTools.getTag(imp, "0011,1012");
				if (tag!=null && !tag.isEmpty()) tag=tag.trim();
				
				String tagVector=DicomTools.getTag(imp, "0054,0020");
				if (tagVector!=null && !tagVector.isEmpty()) tagVector=tagVector.trim();

					if (tag!=null) {
						if (tag.contains("POS") || tag.contains("_F")) {
							imp.getProcessor().flipHorizontal();
						}
						if (imp.getStackSize()==2 && !tag.contains("POS") && !tag.contains("_F") && !tag.contains("ANT") && !tag.contains("_F") ) {
							IJ.log("2 image detected with No Orientation label found, assuming image 2 is posterior. Please notify Salim.kanoun@gmail.com");
						}
					}
					else {
						IJ.log("No Orientation found Assuming detector 1 is anterior, please send image sample to Salim.kanoun@gmail.com if wrong");
						if (imp.getStackSize()==2 && tagVector.equals("2")) {
							imp.getProcessor().flipHorizontal();
							
						}
					}
					
				}	
			return imp;
		}
	
	/**
	 * Permet de tester si l'image est anterieure pour une unique frame, ne teste que la première Image (peut etre generalisee plus tard si besoin)
	 * A Eviter d'utiliser car la methode isAnterieur(ImagePlus imp) est generique pour tout type d'image
	 * @param imp : ImagePlus a tester
	 * @return boolean vrai si anterieur
	 */
	@Deprecated
	public static Boolean isAnterieurUniqueFrame(ImagePlus imp){
			imp.setSlice(1);
			
			//Recupere le private tag qui peut contenir des informations de localisation (rangueil)
			String tag = DicomTools.getTag(imp, "0011,1012");
			
			//On repere le num de camera
			String tagVector=DicomTools.getTag(imp, "0054,0020");
			if (tagVector!=null && !tagVector.isEmpty()) tagVector=tagVector.trim();
			
			//On ajoute un deuxieme tag de localisation a voir dans la pratique ou se situe l'info
			if (DicomTools.getTag(imp, "0011,1030")!=null)		tag+=DicomTools.getTag(imp, "0011,1030");
			Boolean anterieur=null;
			
			if (tag!=null || tagVector!=null) {
				
				// Si on a le private tag on le traite
				if (tag!=null) {
					
					if (tag.contains("ANT") || tag.contains("_E")) {
						anterieur=true;
					}
					else if (tag.contains("POS") || tag.contains("_F")) {
						anterieur=false;
					}
					else {
						IJ.log("Orientation not reckognized");
					}
				}
				
				//Si pas de private tag on fait avec le numero de la camera
				else if (tag==null && tagVector!=null) {
					if(imp.getStackSize()==2) {
						// SK FAUDRA RECONNAITRE LES IMAGE D/G ET LES DIFFERENCIER
						if (tagVector.equals("1")) anterieur=true;
						if (tagVector.equals("2")) anterieur=false;
						IJ.log("Orientation Not reckgnized, assuming vector 1 is anterior");
					}
					// le Boolean reste null et on informe l'user
					else {
						IJ.log("Orientation not reckognized");
					}
				}
				
			}
			
			//Si aucun des deux echec du reperage
			else {
				IJ.log("Orientation not reckognized");
			}
				
				
				return anterieur;
	}
		
	/**
	 * Permet de tester si l'image est anterieure pour une MultiFrame, ne teste que la première Image (peut etre generalisee plus tard si besoin)
	 * A Eviter d'utiliser car la methode isAnterieur(ImagePlus imp) est generique pour tout type d'image
	 * 
	 * @param imp : ImagePlus a tester
	 * @return boolean vrai si anterieur
	 */
	@Deprecated
	public static Boolean isAnterieurMultiframe(ImagePlus imp) {
		//On ne traite que l'image 1
		imp.setSlice(1);
		String tag= DicomTools.getTag(imp, "0011,1012");
		//On ajoute un deuxieme tag de localisation a voir dans la pratique ou se situe l'info
		if (DicomTools.getTag(imp, "0011,1030")!=null)		tag+=DicomTools.getTag(imp, "0011,1030");
		
		//On set le Boolean a null
		Boolean anterieur = null;
		if (tag!=null) {
			///On recupere le 1er separateur de chaque vue dans le champ des orientation
			int separateur=tag.indexOf("\\");
			
			//Si on ne trouve pas le separateur, on met la position du separateur ï¿½ la fin de la string pour tout traiter
			if (separateur==-1) separateur=(tag.length());
			
				// Si la 1ere image est labelisee anterieure
			if (tag.substring(0, separateur).contains("ANT") || tag.substring(0, separateur).contains("_E")) {
				anterieur=true;
			}
			//Si la 1ere image est labellisee posterieure
			else if (tag.substring(0, separateur).contains("POS") || tag.substring(0, separateur).contains("_F")) {
				anterieur=false;
			}
			
			//Si on ne trouve pas de tag le booelan reste null et on notifie l'utilisateur
			else if (!tag.substring(0, separateur).contains("POS") && !tag.substring(0, separateur).contains("_F")&& !tag.substring(0, separateur).contains("ANT") && !tag.substring(0, separateur).contains("_E")) {
				// le Boolean reste ï¿½ null et on informe l'user
				IJ.log("Information not reckognized");	
			}
		}
		else {
			IJ.log("No localization information");	
		}
		
		return anterieur;	
	}
		
	/**
	 * Permet de tester si la 1ere image de l'ImagePlus est une image anterieure
	 * @param imp : ImagePlus a tester
	 * @return booleen vrai si image anterieure
	 */
	public static Boolean isAnterieur(ImagePlus imp) {
		Boolean anterieur=null;
			if (isMultiFrame(imp)) {
				anterieur=isAnterieurMultiframe(imp);
			}
			if (!isMultiFrame(imp)) {
				anterieur=isAnterieurUniqueFrame(imp);
			}
			return anterieur;
	}
		
	/**
	 * Premet de trier un tableau d'ImagePlus par leur acquisition date et time de la plus ancienne à la plus recente
	 * @param serie : Tableau d'ImagePlus a trier
	 * @return Tableau d'ImagePlus ordonne par acquisition time
	 */
	public static ImagePlus[] orderImagesByAcquisitionTime(ImagePlus[] serie) {
		
		ImagePlus[] retour = serie.clone();
		
		Arrays.sort(retour, new Comparator<ImagePlus>() {

			@Override
			public int compare(ImagePlus arg0, ImagePlus arg1) {
				DateFormat dateHeure=new SimpleDateFormat("yyyyMMddHHmmss.SS");
				String dateImage0 = DicomTools.getTag(arg0, "0008,0022");
				String dateImage1 = DicomTools.getTag(arg1, "0008,0022");
				
				String heureImage0 = DicomTools.getTag(arg0, "0008,0032");
				String heureImage1 = DicomTools.getTag(arg1, "0008,0032");
				
				String dateInputImage0=dateImage0.trim()+heureImage0.trim();
				String dateInputImage1=dateImage1.trim()+heureImage1.trim();
				
				Date timeImage0 = null;
				Date timeImage1 = null;
				try {
					timeImage0= dateHeure.parse(dateInputImage0);
					timeImage1 = dateHeure.parse(dateInputImage1);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				return (int) ((timeImage0.getTime()-timeImage1.getTime())/1000);
			}
		});
		
		return retour;
	}
		
	/**
	 * Permet de spliter les images d'un multiFrame contenant 2 camera, image 0 camera Ant et Image1 Camera Post
	 * @param imp : ImagePlus a traiter
	 * @return Tableau d'imagePlus avec 2 ImagePlus (camera 1 et 2 )
	 */
	public static ImagePlus[] splitCameraMultiFrame(ImagePlus imp) {
		//On prend le Header
		String metadata=imp.getInfoProperty();
		
		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (tagDetecteur!=null && !tagDetecteur.isEmpty()) tagDetecteur = tagDetecteur.trim();
		String delims = "[ ]+";
		String[] sequenceDetecteur = tagDetecteur.split(delims);
		
		//On cree les ImageStack qui vont recevoir les image de chaque tête
		ImageStack camera0=new ImageStack(imp.getWidth(),imp.getHeight());
		ImageStack camera1=new ImageStack(imp.getWidth(),imp.getHeight());
		
		// Determination de l'orientation des camera en regardant la 1ere image
		String detecteurPremiereImage=sequenceDetecteur[0];
		Boolean anterieurPremiereImage=Vue_Shunpo.isAnterieurMultiframe(imp);

		
		//On ajoute les images dans les camera adhoc
		
			if(anterieurPremiereImage!= null && anterieurPremiereImage) {
				for (int i=0; i<sequenceDetecteur.length ; i++) {
					if (sequenceDetecteur[i]==detecteurPremiereImage) {
						camera0.addSlice(imp.getImageStack().getProcessor((i+1)));
						}
					else {
						camera1.addSlice(imp.getImageStack().getProcessor((i+1)));
						camera1.getProcessor(i+1).flipHorizontal();
						}
					}
				}
			else if(anterieurPremiereImage!= null && !anterieurPremiereImage) {
				for (int i=0; i<sequenceDetecteur.length ; i++) {
					if (sequenceDetecteur[i]==detecteurPremiereImage) {
						camera1.addSlice(imp.getImageStack().getProcessor((i+1)));
						camera1.getProcessor(i+1).flipHorizontal();
						}
					else {
						camera0.addSlice(imp.getImageStack().getProcessor((i+1)));
						}			
					}
				}
			else  {
				IJ.log("assuming image 2 is posterior. Please notify Salim.kanoun@gmail.com");
					for (int i=0; i<sequenceDetecteur.length ; i++) {
						if (sequenceDetecteur[i].equals("1")) {
							camera0.addSlice(imp.getImageStack().getProcessor((i+1)));
						}
						else if (sequenceDetecteur[i].equals("2")) {
							camera1.addSlice(imp.getImageStack().getProcessor((i+1)));
						}
					}
			}
		
		ImagePlus cameraAnt=new ImagePlus();
		ImagePlus cameraPost=new ImagePlus();
		cameraAnt.setStack(camera0);
		cameraPost.setStack(camera1);
		
		ImagePlus[] cameras=new ImagePlus[2];
		cameras[0]=cameraAnt;
		cameras[1]=cameraPost;
		
		//On ajoute une copie des headers
		for (int i=0 ; i<cameras.length ; i++) {
			cameras[i].setProperty("Info", metadata);
		}
		return cameras;
	}
		
	/**
	 * Test si les images du MutiFrame viennent toutes de la meme camera
	 * @param imp : ImagePlus à traiter
	 * @return Boolean
	 */
	public static boolean isSameCameraMultiFrame(ImagePlus imp) {
		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (tagDetecteur!=null && !tagDetecteur.isEmpty()) tagDetecteur=tagDetecteur.trim();
		String delims = "[ ]+";
		String[] sequenceDetecteur = tagDetecteur.split(delims);
		boolean sameCamera=true ;
		
		String premiereImage=sequenceDetecteur[0];
		for (int i=1 ; i<sequenceDetecteur.length;i++) {
			if (!premiereImage.equals(sequenceDetecteur[i])) sameCamera=false;
			premiereImage=sequenceDetecteur[i];
		}
		return sameCamera;
	}
	
	/** 
	 * Test si la premiere image du stack est du detecteur 1
	 * @param imp : ImagePus A traiter
	 * @return boolean
	 */
	public static boolean isPremiereImageDetecteur1(ImagePlus imp) {
		// On recupere la chaine de detecteur
		String tagDetecteur = DicomTools.getTag(imp, "0054,0020");
		if (tagDetecteur!=null && !tagDetecteur.isEmpty()) tagDetecteur=tagDetecteur.trim();
		String delims = "[ ]+";
		String[] sequenceDeteceur = tagDetecteur.split(delims);
		boolean detecteur1=false;
		
		if (Integer.parseInt(sequenceDeteceur[0])==1) detecteur1 = true;
		
		return detecteur1;
	}
} 

// Fin Vue_Shunpo
