/**
Copyright (C) 2017 PING Xie and KANOUN Salim
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

package org.petctviewer.scintigraphy.gastric;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.util.DicomTools;

public class Vue_VG_Roi extends JPanel implements PlugIn {

	private static final long serialVersionUID = 926913215780633247L;

	public HashMap<String, Button> lesBoutons;// touts les buttons de l'interface

	protected boolean imageOuverte = false;// signifie si les images sont ouverts

	protected RoiManager leRoi;

	protected ImagePlus imp;

	protected static String timeStart;//l'horaire où le patient commence a manger

	private Controleur_VG_Roi leControleur = new Controleur_VG_Roi(this, new Modele_VG_Roi());

	private JTable tabRes;// le tableau pour afficher le pourcentage de chaque organe (une partie de resultat)

	private JPanel infoRes;//le panel pour afficher les resultats 

	protected String nomProgramme = "Gastric Emptying";

	protected Label instructions;//pour afficher les instructions

	protected CustomStackWindow windowstack;//la fenetre principale

	protected CustomWindow res;//la fenetre du resultat
	
	//private Dimension dimensionPanelPrincipal;

	protected Overlay overlay;

	protected static boolean estArgume;//signifie si il y a des arguments pour ce programme

	protected static String[] resultatsDynamique;//resultats du programme VG_Ingestion_Dynamique transmettant en argument
	
	protected Label csv = new Label();

	@Override
	public void run(String arg) {
		String argumentString = Macro.getOptions();
		estArgume = (argumentString != null);
		// si il y a des arguments recupere de VG_Ingestion_Dynamique, passe le permier
		// argument au timestart, pas les autres arguments des resultats
		if (estArgume) {
			String[] argumentTab = argumentString.split("\\;");
			timeStart = argumentTab[0];
			// on prend pas le dernier argument car il est null, donc on prend
			// du deuxieme a l'avant dernier
			resultatsDynamique = new String[argumentTab.length - 2];
			for (int i = 1; i < argumentTab.length - 1; i++) {
				resultatsDynamique[i - 1] = argumentTab[i];
			}
		} else {
			//sinon on initialise resultatsDynamique
			resultatsDynamique = new String[4];
			resultatsDynamique[0] = "0";
			resultatsDynamique[1] = "100.0";
			resultatsDynamique[2] = "100.0";
			resultatsDynamique[3] = "0.0";
		}

		// Initialisation des differents attributs
		RoiManager rm = new RoiManager(false);
		leRoi = rm;
		instructions = new Label("");
		instructions.setBackground(Color.LIGHT_GRAY);
		initBoutons();
		FenSelectionDicom selection=new FenetreSelection(this);
		selection.setVisible(true);


	}

	private void initBoutons() {

		lesBoutons = new HashMap<>();
		lesBoutons.put("Show", new Button("Show MG%"));
		lesBoutons.put("Draw ROI", new Button(" Draw ROI "));
		lesBoutons.put("Quitter", new Button(" Quit"));
		lesBoutons.put("Contrast", new Button(" Contrast "));
		lesBoutons.put("Precedent", new Button(" Previous "));
		lesBoutons.put("Suivant", new Button(" Next "));
		lesBoutons.put("Valider", new Button(" Confirm "));
		lesBoutons.put("Sauvegarder", new Button("Save Results"));
		lesBoutons.put("Return", new Button("Return to adjust ROIs"));
		
	}

	public void setInstructions(String inst) {
		instructions.setText(inst);
	}

	// Extraite du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	class CustomCanvas extends ImageCanvas {

		private static final long serialVersionUID = -5710708795558320699L;

		CustomCanvas(ImagePlus imp) {

			super(imp);

		} // Fin constructeur CustomCanvas

	} // Fin CustomCanvas

	// Extraite puis modifiee du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	// Cette classe permet d'avoir une image et des elements graphiques sur une
	// fenêtre
	// On l'utilise pour la fenêtre de resultats
	class CustomWindow extends ImageWindow {

		private static final long serialVersionUID = -9097595151860174657L;

		CustomWindow(ImagePlus imp) {
			super(imp, new CustomCanvas(imp));
			//setLayout(new GridLayout(1,2));
			setLayout(new FlowLayout());
			addPanel();
		} // Fin constructeur CustomWindow

		// Permet d'ajouter les boutons a la fenêtre
		private void addPanel() {

			// Agencement des composants
			Panel panel = new Panel();
			panel.setLayout(new BorderLayout());

			Panel panelResultats = new Panel();
			panelResultats.setLayout(new FlowLayout());
			panelResultats.add(infoRes);
			panelResultats.add(tabRes);

			Panel panelNonResultats = new Panel();
			panelNonResultats.setLayout(new GridLayout(2, 1));

			// On test la presence d'un repertoire CSV defini
			String path = Prefs.get("dir.preferred", null);
			if (path == null) {
				csv.setText(" No CSV output");
				csv.setForeground(Color.RED);
			} else {
				csv.setText(" CSV Save OK");
			}
			panelNonResultats.add(csv);

			Panel panelButtons = new Panel();
			panelButtons.setLayout(new FlowLayout());

			panelButtons.add(lesBoutons.get("Return"));
			panelButtons.add(lesBoutons.get("Sauvegarder"));
			panelNonResultats.add(panelButtons);

			panel.add(panelResultats, BorderLayout.CENTER);
			panel.add(panelNonResultats, BorderLayout.SOUTH);
			add(panel);
			pack();
		
		} // Fin addPanel

	} // Fin CustomWindow

	// Extraite puis modifiee du code de la classe Panel_Window de Wayne Rasband
	// Source : https://imagej.nih.gov/ij/plugins/panel-window.html
	// Cette classe permet d'avoir une pile d'images et des elements graphiques
	// sur une fenêtre
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
			addEcouteurs();

			// Partie gauche
			Panel gauche = new Panel();
			gauche.setLayout(new FlowLayout());

			// Premiers boutons
			Panel btns_glob = new Panel();
			btns_glob.setLayout(new GridLayout(1, 3));
			btns_glob.add(lesBoutons.get("Quitter"));
			btns_glob.add(lesBoutons.get("Draw ROI"));
			lesBoutons.get("Draw ROI").setBackground(Color.LIGHT_GRAY);
			btns_glob.add(lesBoutons.get("Contrast"));
			gauche.add(btns_glob);

			// Instructions
			Panel instru = new Panel();
			instru.setLayout(new GridLayout(2, 1));
			instru.add(instructions);
			Panel btns_instru = new Panel();
			btns_instru.setLayout(new GridLayout(1, 3));
			btns_instru.add(lesBoutons.get("Show"));
			btns_instru.add(lesBoutons.get("Precedent"));
			lesBoutons.get("Precedent").setEnabled(false);
			btns_instru.add(lesBoutons.get("Suivant"));
			instru.add(btns_instru);
			gauche.add(instru);
			panel.add(gauche);
			add(panel);
			pack();

			// Permet d'avoir la fenêtre ouverte au même endroit que l'image
			// selectionnee par l'utilisateur
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Point loc = getLocation();
			Dimension size = getSize();
			if (loc.y + size.height > screen.height)
				getCanvas().zoomOut(0, 0);
		} // Fin addPanel

		private void addEcouteurs() {
			for (Entry<String, Button> entry : lesBoutons.entrySet())
				entry.getValue().addActionListener(leControleur);
		}

		public void windowClosing(WindowEvent we) {
			// On ferme le ROI manager en plus de la fenetre
			leRoi.close();
			windowstack.close();
			System.gc();
		}

	} // Fin CustomStackWindow


	protected void ouvertureImage(ImagePlus[] imageTitles) {

		// Si serie selectionnees on les traites
		if (imageTitles !=null) {
			ArrayList<ImagePlus> imagesOpened = new ArrayList<ImagePlus>();
			for (int i = 0; i < imageTitles.length; i++) {
				ImagePlus currentimp = imageTitles[i];
				// On verifie qu'il y a 2 images par stack
				if (currentimp.getStackSize() != 2) {
					IJ.log("Error Not Image Ant/Post, Discarding, Check your original Images");
					currentimp.close();
					continue;
				}
				else {
				// On trie les images
				imagesOpened.add(Library_Dicom.sortImageAntPost(currentimp)) ;
				currentimp.close();
				}
				
			}
	
			// trie les images de la serie
			ImagePlus[] imagesOrdred = Library_Dicom.orderImagesByAcquisitionTime(imagesOpened);
			Concatenator enchainer = new Concatenator();
	
			// enchaine les images
			ImagePlus imp = enchainer.concatenate(imagesOrdred, false);
			imp.show();
			HyperStackConverter convert = new HyperStackConverter();
			convert.run("hstostack");
	
			String serie = DicomTools.getTag(imp, "0008,103E");
			String titre = nomProgramme + " - ";
			String tag = DicomTools.getTag(imp, "0010,0010");
			titre = titre + tag + " - " + serie;
			// met la LUT preferee si existe
			Library_Gui.setCustomLut(imp);
			// Son cree une fenetre pour la pile d'images
			windowstack = new CustomStackWindow(imp);
			// On demande la 1ere image du stack
			windowstack.showSlice(1);
			this.imp = imp;
			// On change les titres
			imp.setTitle(titre);
			windowstack.setTitle(titre);
			this.overlay=Library_Gui.initOverlay(imp, 12);
			Library_Gui.setOverlayDG(overlay, imp);
			windowstack.getImagePlus().setOverlay(overlay);
			// On set la dimension de l'image
			windowstack.getCanvas().setSize(new Dimension(512,512));
			windowstack.getCanvas().setScaleToFit(true);
			//On Pack la fenetre pour la mettre a la preferred Size
			windowstack.pack();
			windowstack.setSize(windowstack.getPreferredSize());
			//On met au premier plan au centre de l'ecran
			windowstack.setLocationRelativeTo(null);
			windowstack.toFront();
			IJ.setTool(Toolbar.POLYGON);
			if (!estArgume) {
				String acquisitionTimeAP1 = DicomTools.getTag(windowstack.getImagePlus(), "0008,0032");
				lesBoutons.get("Suivant").setEnabled(false);
				// saisie le temps de commence
				saisiTempsCommence(acquisitionTimeAP1);
			}
	
			if (instructions.getText().equals(""))
				instructions.setText("Delimit the Stomache");
		}
		else {
			end("dialog");
		}
	}

	// permet de saisir le temps de commence
	private void saisiTempsCommence(String timeAP1) {
		JDialog dialog = new JDialog();
		dialog.setLayout(new GridLayout(3, 1));

		// ajoute un panel pour afficher le temps de la premierer image et
		// l'information de demande
		JPanel panelTemPremier = new JPanel();
		panelTemPremier.setLayout(new GridLayout(2, 1));
		JLabel labelTempsPremier = new JLabel("The acquisition time of first image is " + timeAP1.substring(1, 3)
				+ " h " + timeAP1.substring(3, 5) + " m " + timeAP1.substring(5, 7) + " s.");
		JLabel labelAlerte = new JLabel("please input the time of beginning!");
		panelTemPremier.add(labelTempsPremier);
		panelTemPremier.add(labelAlerte);
		dialog.add(panelTemPremier);

		// ajoute un panel du temps
		JPanel panelTemps = new JPanel();
		panelTemps.setLayout(new GridLayout(1, 6));
		panelTemps.setBorder(new EmptyBorder(5, 5, 5, 5));
		// ajoute un label et un ComboBox de l'heure au panel du temps
		JLabel labelHeure = new JLabel("heure:");
		panelTemps.add(labelHeure);
		JComboBox<String> comboBoxHeure = addComboBox(24);
		panelTemps.add(comboBoxHeure);
		// ajoute un label et un ComboBox de la minute au panel du temps
		JLabel labelMinu = new JLabel("minute:");
		panelTemps.add(labelMinu);
		JComboBox<String> comboBoxMinu = addComboBox(60);
		panelTemps.add(comboBoxMinu);
		// ajoute un label et un ComboBox de la seconde au panel du temps
		JLabel labelSec = new JLabel("second:");
		panelTemps.add(labelSec);
		JComboBox<String> comboBoxSec = addComboBox(60);
		panelTemps.add(comboBoxSec);
		dialog.add(panelTemps);

		// ajoute un panel du valide
		JPanel panelValide = new JPanel();
		panelValide.setLayout(null);
		// ajoute un button au panel du valide
		JButton buttonOK = new JButton("OK");
		panelValide.add(buttonOK);
		buttonOK.setBounds(120, 10, 60, 30);
		//permet de recupere les valeurs qu'on saisie au comboBox
		buttonOK.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				timeStart = (String) comboBoxHeure.getSelectedItem();
				timeStart += (String) comboBoxMinu.getSelectedItem();
				timeStart += (String) comboBoxSec.getSelectedItem();
				dialog.dispose();
				lesBoutons.get("Suivant").setEnabled(true);
			}
		});
		dialog.add(panelValide);
		dialog.setTitle("Please input  the time of beginning");
		dialog.setVisible(true);
		dialog.setAlwaysOnTop(true);
		dialog.setSize(330, 170);
		dialog.setLocationRelativeTo(windowstack);
		dialog.requestFocus();
	}

	private JComboBox<String> addComboBox(int number) {
		JComboBox<String> comboBox = new JComboBox<String>();
		String[] valeurs = new String[number];
		for (int i = 0; i < number; i++) {
			if (i < 10) {
				valeurs[i] = "0" + i;
			} else {
				valeurs[i] = "" + i;
			}
		}
		for (int i = 0; i < valeurs.length; i++) {
			comboBox.addItem(valeurs[i]);
		}
		return comboBox;
	}

	// Interface graphique pour resultats
	public void UIResultats(ImagePlus screen) {
		ImageProcessor ip=screen.getProcessor();
		ip.setInterpolate(true);
		ip.setInterpolationMethod(ImageProcessor.BICUBIC);
		ip=screen.getProcessor().resize((int) (screen.getWidth()*1.5));
		ImagePlus screen2=new ImagePlus("Final Capture",ip);
		// On cree la fenetre resultat avec le panel resultat
		res = new CustomWindow(screen2);
		res.setLocationRelativeTo(null);
		res.getCanvas().setMagnification(1.0);
		res.getCanvas().setScaleToFit(true);
		res.getCanvas().hideZoomIndicator(true);
		res.setTitle(nomProgramme + " - Results");
		res.pack();
		res.setSize(res.getPreferredSize());
		res.toFront();
		// On ferme l'outil ROI
		IJ.setTool("hand");
	}

	// fermeture de fermeture du programme
	public void end(String dialog) {
		if (dialog == null) {
			int optionChoisie = JOptionPane.showConfirmDialog(null, "The program will now shut down", "",
					JOptionPane.OK_CANCEL_OPTION);
			if (optionChoisie == JOptionPane.OK_OPTION) {
					imp.close();
					leRoi.close();
				}
			}
		else if (dialog == "dialog") {
				leRoi.close();
			}
	}
	//les resultats consistent a deux parties, la premiere est le pourcentage des MGs pour chaque serie, 
	//la deuxieme est les informations bases(nom du patient, date...) et les autres informations( debut de l'antre, retention a 1h...)
	// Remplis le table qui affiche la premiere partie des resultats
	public void tablesResultats(String[] resultats) {
		//on cree un tableau avec 4 colonnes
		tabRes = new JTable(0, 4);
		DefaultTableModel tableModel = (DefaultTableModel) tabRes.getModel();
		String[] arr = new String[tableModel.getColumnCount()];
		//le nombre de ligne concerne a nombre du serie statique + nombre du serie dynamique + 1, cette ligne pour afficher les titres
		for (int i = 0; i < (imp.getStackSize() / 2 + resultatsDynamique.length / 4 + 1); i++) {
			for (int j = 0; j < tableModel.getColumnCount(); j++) {
				arr[j] = resultats[i * tableModel.getColumnCount() + j];
			}
			tableModel.insertRow(i, arr);
		}
		tabRes.setRowHeight(30);
		MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
		tabRes.setBorder(border);
	}

	// Remplis le panel qui affiche la deuxieme partie des resultats
	public void infoResultats(String[] resultats) {
		infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(14, 1));
		//la deuxime partir du resultats contient 13 ligne
		for (int i = ((imp.getStackSize() / 2 + resultatsDynamique.length / 4 + 1) * 4); i < resultats.length; i++) {
			infoRes.add(new Label(resultats[i]));
			// on ajoute deux lignes vides pour separer les informations
			// bases(nom du patient, date...) et les autres informations
			if (i == (resultats.length - 9)) {
				infoRes.add(new Label("    "));
				infoRes.add(new Label("    "));

			}
		}
	}

	public void setNonVisibleButtons() {
		lesBoutons.get("Sauvegarder").setVisible(false);
		lesBoutons.get("Return").setVisible(false);
	}

	
} // Fin
