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
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;

public class Vue_Shunpo implements PlugIn {

	protected HashMap<String, Button> lesBoutons;

	private boolean imageOuverte = false;

	private Label img_inst;

	private Controleur_Shunpo leControleur = new Controleur_Shunpo(this, new Modele_Shunpo());

	protected Overlay overlay;

	private Label[] labRes;

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
		RoiManager rm = new RoiManager(false);
		leRoi = rm;
		instructions = new Label("");
		instructions.setBackground(Color.LIGHT_GRAY);
		img_inst = new Label();
		labRes = new Label[10];
		for (int i = 0; i < 10; i++)
			labRes[i] = new Label("");
		initBoutons();
		addEcouteurs();
		lesBoutons.get("Valider").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Button b = (Button) arg0.getSource();
				if (WindowManager.getCurrentImage() != null && WindowManager.getCurrentImage() != imp
						&& imageOuverte == true) {
					ImagePlus imp = WindowManager.getCurrentImage();
					ouvertureImageBrain(imp);
					((Frame) b.getParent().getParent()).dispose();
				}
				if (WindowManager.getCurrentImage() != null && imageOuverte == false) {
					imageOuverte = true;
					ImagePlus imp = WindowManager.getCurrentImage();
					ouvertureImage(imp);
					((Frame) b.getParent().getParent()).dispose();
				}

			}

		});
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

	// Extraite puis modifi茅e du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	// Cette classe permet d'avoir une image et des 茅l茅ments graphiques sur une
	// fen锚tre
	// On l'utilise pour la fen锚tre de r茅sultats

	class CustomWindow extends ImageWindow {

		private static final long serialVersionUID = -9097595151860174657L;

		CustomWindow(ImagePlus imp) {
			super(imp, new ImageCanvas(imp));
			addPanel();
		}
		// Fin constructeur CustomWindow

		// Permet d'ajouter les boutons 脿 la fen锚tre

		private void addPanel() {
			// Agencement des composants
			Panel panel = new Panel();
			panel.setLayout(new FlowLayout());

			Panel resultats = new Panel();
			resultats.setLayout(new GridLayout(6, 2));
			for (int i = 0; i < 10; i++) {
				resultats.add(labRes[i]);
				resultats.setSize(resultats.getMaximumSize());
			}
			Panel csv = new Panel();
			csv.setLayout(new GridLayout(1, 1));

			// On test la pr茅sence d'un repertoire CSV defini
			String path = Prefs.get("dir.preferred", null);
			if (path == null) {
				Csv.setText("No CSV output");
				Csv.setForeground(Color.RED);
			} else {
				Csv.setText("CSV Save OK");
			}
			csv.add(Csv);
			Panel Capture = new Panel();
			Capture.setLayout(new GridLayout(1, 1));
			Button capture = lesBoutons.get("Capture");
			Capture.add(capture);
			resultats.add(csv);
			resultats.add(Capture);
			panel.add(resultats);
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

	// Extraite puis modifi茅e du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	// Cette classe permet d'avoir une pile d'images et des 茅l茅ments graphiques
	// sur une fen锚tre

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
			// addEcouteurs();

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

			// Permet d'avoir la fen锚tre ouverte au meme endroit que l'image
			// selectionnee par l'utilisateur
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Point loc = getLocation();
			Dimension size = getSize();
			if (loc.y + size.height > screen.height)
				getCanvas().zoomOut(0, 0);
		} // Fin addPanel

		@Override
		public void windowClosing(WindowEvent we) {
			// On ferme le ROI manager en plus de la fenetre
			leRoi.close();
			win.close();
			System.gc();
		}

	} // Fin CustomStackWindow

	// Ouvre le dialog pour charger une image
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
		// tj au dessus Pour eviter un click qui ferait perdre la fenetre
		f.setAlwaysOnTop(true);
		f.toFront();

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				end("dialog");
				f.dispose();
			}
		});

		if (imageOuverte == true) {
			lesBoutons.get("Suivant").setEnabled(false);
		}

	}

	private void ouvertureImage(ImagePlus imp) {
		int nombreImage = imp.getStackSize();
		if (nombreImage != 2)
			IJ.showMessage("Wrong Input, number of image should be 2, please restart");
		// Tri des image dans une nouvelle imageplus
		ImagePlus imp2 = VueScin.sortImageAntPost(imp);
		imp2.show();
		imp.close();
		// Suite du programme sur la nouvelle imageplus
		this.imp = imp2;
		// Charge la LUT
		VueScin.setCustomLut(this.imp);
		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		CustomStackWindow win = new CustomStackWindow(this.imp);
		this.win = win;
		win.setTitle(setTitre(win.getImagePlus()));
		this.imp.setTitle(setTitre(this.imp));
		// On initialise l'overlay
		this.overlay = VueScin.initOverlay(imp, 12);
		VueScin.setOverlayDG(overlay, imp);
		// On affiche l'image en 512*512 en forcant le zoom adhoc
		win.getCanvas().setSize(new Dimension(512, 512));
		// Adaptation automatique de l'image au resize
		win.getCanvas().setScaleToFit(true);
		// On Pack la fenetre pour la mettre a la preferred Size
		win.pack();
		win.setSize(win.getPreferredSize());
		// On met au premier plan au centre de l'ecran
		win.setLocationRelativeTo(null);
		win.toFront();
		// On met sur l'image
		win.getImagePlus().setOverlay(overlay);
		if (instructions.getText().equals(""))
			instructions.setText("Delimit the right lung.");
		IJ.setTool(Toolbar.POLYGON);
		win.showSlice(2);
	}

	// Definit le titre de la forme : ShunPo - XXXXX Xxxxx - organe et renvoit la
	// string titre
	private String setTitre(ImagePlus imp) {
		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = "ShunPo - ";
		titre = titre + tagNom + " - " + tagSerie;
		return titre;
	}

	// Interface graphique pour resultats
	protected void UIResultats(ImagePlus screen) {
		// On cree la fenetre resultat avec le panel resultat
		res = new CustomWindow(screen);
		// On resize la window pour laisser la place a l'image et au pannel
		// Ici on ajoute que 70 pixel en hauteur car il n'y a pas l'ascenseur horizontal
		// du stack
		res.setLocationRelativeTo(null);
		res.getCanvas().setMagnification(1.0);
		res.getCanvas().setScaleToFit(true);
		res.getCanvas().hideZoomIndicator(true);
		res.pack();
		res.setSize(res.getPreferredSize());
		// On prend le focus
		res.toFront();
		// On implemente le titre de la fenetre
		res.setTitle("Pulmonary Shunt - Results");
		// On quitte l'outil ROI pour Hand (evite d'avoir le curseur qui fait des ROI)
		IJ.setTool("hand");
	}

	// routine de fermeture du programme
	protected void end(String dialog) {
		if (dialog == "dialog" && imageOuverte == false) {
			leRoi.close();
			f.dispose();
			System.gc();
		}

		if (dialog == "dialog" && imageOuverte == true) {
			int optionChoisie = JOptionPane.showConfirmDialog(null, "The program will now shut down", "",
					JOptionPane.OK_CANCEL_OPTION);
			if (optionChoisie == JOptionPane.OK_OPTION) {
				f.dispose();
				win.close();
				leRoi.close();
				System.gc();
			}

		}
		if (dialog == null) {
			int optionChoisie = JOptionPane.showConfirmDialog(null, "The program will now shut down", "",
					JOptionPane.OK_CANCEL_OPTION);
			if (optionChoisie == JOptionPane.OK_OPTION) {
				leRoi.close();
				win.close();
				System.gc();
			}
		}

	}

	// Injecte une nouvelle imageplus dans la fenetre existante
	private void ouvertureImageBrain(ImagePlus imp) {
		// On reactive le boutton suivant
		lesBoutons.get("Suivant").setEnabled(true);
		// On ordonne les images dans une nouvelle imageplus
		ImagePlus cerveau2 = VueScin.sortImageAntPost(imp);
		// On injecte l'image cerveau dans la fenetre
		win.setImage(cerveau2);
		imp.getWindow().close();
		// On applique la LUT des preference si presente
		VueScin.setCustomLut(cerveau2);
		// On set le Titre
		cerveau2.setTitle(setTitre(cerveau2));
		win.setTitle(setTitre(cerveau2));
		// on set le canvas et on repaint
		win.getCanvas().fitToWindow();
		win.repaint();
		win.getImagePlus().killRoi();
		// On ajouter l'overlay Droite/Gauche
		this.overlay = VueScin.initOverlay(imp, 12);
		VueScin.setOverlayDG(overlay, imp);
		win.getImagePlus().setOverlay(overlay);
		// Variable pour notifier que l'image 2 est ouverte
		image2Ouverte = true;
		// On envoi un event au controleur pour passer 脿 l'茅tat suivant
		ActionEvent e = new ActionEvent(lesBoutons.get("Suivant"), ActionEvent.ACTION_PERFORMED, "Suivant");
		leControleur.actionPerformed(e);
	}

	// Remplis les labels des resultats
	protected void labelsResultats(String[] resultats) {
		for (int i = 0; i < resultats.length; i++) {
			labRes[i].setText(resultats[i]);
		}
		if (Modele_Shunpo.shunt < 2) {
			// Si shunt inf�rieur � 2% (examen normal) Affiche en vert
			labRes[7].setForeground(new Color(0, 89, 0));
		}
		if (Modele_Shunpo.shunt < 5 && Modele_Shunpo.shunt > 2) {
			// Affiche en Orange
			labRes[7].setForeground(new Color(229, 148, 0));
		}
		if (Modele_Shunpo.shunt > 5) {
			// Affiche en Rouge
			labRes[7].setForeground(new Color(230, 0, 0));
		}
		// Affiche le nom en gras
		labRes[9].setFont(new Font("Arial", Font.BOLD, 12));
	}

}

// Fin Vue_Shunpo
