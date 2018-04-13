/**
Copyright (C) 2017 KANOUN Salim

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

package org.petctviewer.scintigraphy.platelet;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.petctviewer.scintigraphy.scin.view.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.PlugIn;
import ij.plugin.StackReverser;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;

public class Vue_Plaquettes implements PlugIn {

	protected HashMap<String, Button> lesBoutons;

	private Label img_inst = new Label();
	private Modele_Plaquettes leModele = new Modele_Plaquettes();
	private Controleur_Plaquettes leControleur;
	protected Overlay overlay;
	protected Label Csv = new Label();
	protected RoiManager roiManager;
	protected Label instructions = new Label();
	protected CustomStackWindow win;
	private ImagePlus imp;
	private Frame f;

	// Si acquisition antPost
	protected Boolean antPost = false;

	// Nombre de series disponibles a l'ouverture
	protected int nombreAcquisitions;
	
	public Vue_Plaquettes() {
		String[] organes = {"Spleen", "Liver", "Heart"};
		this.leControleur =  new Controleur_Plaquettes(this, leModele, organes);
	}

	@Override
	public void run(String arg) {
		// Initialisation des differents attributs
		RoiManager rm = new RoiManager();
		roiManager = rm;
		instructions.setBackground(Color.LIGHT_GRAY);
		initBoutons();
		addEcouteurs();

		lesBoutons.get("Valider").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Button b = (Button) arg0.getSource();
				if (WindowManager.getCurrentImage() != null) {
					String[] titresFenetres = WindowManager.getImageTitles();
					ouvertureImage(titresFenetres);
					((Frame) b.getParent().getParent()).dispose();
				}

			}

		});

		ouvrirImage("Platelets");
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

		CustomWindow(ImagePlus imp, JTable tableResults) {
			super(imp, new CustomCanvas(imp));
			// SK TESTER SOUS WINDOWS PEUT ETRE BUG DE LINUX ICI
			setLayout(new FlowLayout());
			addPanel(tableResults);
		}
		// Fin constructeur CustomWindow

		// Permet d'ajouter les boutons 脿 la fen锚tre

		private void addPanel(JTable tableResults) {
			// Agencement des composants
			Panel resultats = new Panel();
			resultats.setLayout(new BorderLayout());
			JScrollPane scrollPane = new JScrollPane(tableResults);
			tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			tableResults.setAutoCreateRowSorter(true);
			tableResults.setFillsViewportHeight(true);
			tableResults.getRowSorter().toggleSortOrder(0);

			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new FlowLayout());

			// On test la pr茅sence d'un repertoire CSV defini
			String path = Prefs.get("dir.preferred", null);
			if (path == null) {
				Csv.setText("No CSV output");
				Csv.setForeground(Color.RED);
			} else {
				Csv.setText("CSV Save OK");
			}

			Button capture = lesBoutons.get("Capture");

			buttonPanel.add(Csv);
			buttonPanel.add(capture);

			resultats.add(scrollPane, BorderLayout.CENTER);
			resultats.add(buttonPanel, BorderLayout.SOUTH);

			add(resultats);

			pack();

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
			lesBoutons.get("Precedent").setEnabled(true);
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
			roiManager.close();
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

	}

	private void ouvertureImage(String[] titresFenetres) {

		ArrayList<ImagePlus> series = new ArrayList<ImagePlus>();

		for (int i = 0; i < titresFenetres.length; i++) {

			ImagePlus imp = WindowManager.getImage(titresFenetres[i]);
			if (imp.getStackSize() == 2) {
				antPost = true;
				Boolean ant = VueScin.isAnterieur(imp);
				// Si l'image 1 est anterieur on inverse le stack pour avoir d'abord l'image
				// post
				if (ant != null && ant) {
					StackReverser reverser = new StackReverser();
					reverser.flipStack(imp);
				}
			}
			// Si uniquement une image on verifie qu'elle est post et on la flip
			else if (imp.getStackSize() == 1) {
				// SK Pas propre necessite de mieux orienter les Image pour Ant/Post
				imp.getProcessor().flipHorizontal();
			}
			series.add(VueScin.sortImageAntPost(imp));
			imp.close();
		}
		nombreAcquisitions = series.size();
		// IJ.log(String.valueOf(antPost));

		ImagePlus[] seriesTriee = VueScin.orderImagesByAcquisitionTime(series);

		// On recupere la date et le jour de la 1ere image
		String aquisitionDate = DicomTools.getTag(seriesTriee[0], "0008,0022");
		String aquisitionTime = DicomTools.getTag(seriesTriee[0], "0008,0032");
		String aquisitionDateTime = aquisitionDate.trim() + aquisitionTime.trim();
		int separateurPoint = aquisitionDateTime.indexOf(".");
		if (separateurPoint != -1)
			aquisitionDateTime = aquisitionDateTime.substring(0, separateurPoint);

		DateFormat dateHeure = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date dateHeureDebut = dateHeure.parse(aquisitionDateTime);
			leModele.setDateDebutHeure(dateHeureDebut);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Concatenator enchainer = new Concatenator();
		// enchaine les images
		ImagePlus imp = enchainer.concatenate(seriesTriee, false);
		imp.show();

		HyperStackConverter convert = new HyperStackConverter();
		convert.run("hstostack");

		this.imp = imp;
		// Charge la LUT
		VueScin.setCustomLut(this.imp);

		// Initialisation du Canvas qui permet de mettre la pile d'images
		// dans une fenetre c'est une pile d'images (plus d'une image) on cree une
		// fenetre pour la pile d'images;
		CustomStackWindow win = new CustomStackWindow(this.imp);
		this.win = win;
		win.setTitle(setTitre(this.imp));
		this.imp.setTitle(setTitre(this.imp));
		// On fixe la taille de l'image a 512*512 et on force le zoom pour atteindre
		// cette dimension
		//win.getCanvas().setSize(new Dimension(512, 512));
		//win.getCanvas().setScaleToFit(true);
		// On Pack la fenetre pour la mettre a la preferred Size
		win.pack();
		win.setSize(win.getPreferredSize());
		// On met au premier plan au centre de l'ecran
		win.setLocationRelativeTo(null);
		win.toFront();
		// On initialise l'overlay avec les label DG
		this.overlay = VueScin.initOverlay(imp);
		VueScin.setOverlayDG(overlay, imp);
		// On met sur l'image
		win.getImagePlus().setOverlay(overlay);
		if (instructions.getText().equals(""))
			instructions.setText("Delimit the Spleen");
		IJ.setTool(Toolbar.POLYGON);
	}

	// Definit le titre
	private String setTitre(ImagePlus imp) {
		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = "Platelet - ";
		titre = titre + tagNom + " - " + tagSerie;
		return titre;
	}

	protected void UIResultats(ImagePlus screen, JTable tableresults) {
		// On cree la fenetre resultat avec le panel resultat
		CustomWindow win = new CustomWindow(screen, tableresults);
		// On prend le focus
		win.setTitle("Platelet Results");
		win.setLocationRelativeTo(null);
		win.getCanvas().setMagnification(0.70);
		win.getCanvas().setScaleToFit(true);
		win.pack();
		win.setSize(win.getPreferredSize());

		// On quitte l'outil ROI pour Hand (evite d'avoir le curseur qui fait des ROI)
		IJ.setTool("hand");
	}

	// routine de fermeture du programme
	protected void end(String dialog) {
		roiManager.close();
		win.dispose();
		System.gc();
	}

} // fin
