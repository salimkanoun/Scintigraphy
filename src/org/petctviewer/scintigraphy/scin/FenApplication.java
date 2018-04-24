package org.petctviewer.scintigraphy.scin;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.cardiac.Controleur_Cardiac;

public class FenApplication extends StackWindow {
	private static final long serialVersionUID = -6280620624574294247L;
	private Label lbl_instructions;

	///boutons mode normal
	private Button btn_quitter;
	private Button btn_drawROI;
	private Button btn_contrast;
	private Button btn_showlog;
	private Button btn_precedent;
	private Button btn_suivant;

	private ControleurScin controleur;

	protected Panel instru;
	private String nom;

	/**
	 * Cree et ouvre la fenetre principale de l'application
	 * @param imp ImagePlus a traiter
	 * @param nom Nom du type de scintigraphie
	 */
	public FenApplication(ImagePlus imp, String nom) {
		super(imp, new ImageCanvas(imp));
		
		this.nom = nom;
		setTitle(generateTitle());
		this.imp.setTitle(generateTitle());

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		Panel gauche = new Panel();
		gauche.setLayout(new FlowLayout());

		initButtons();

		Panel btns_glob = new Panel();
		btns_glob.setLayout(new GridLayout(1, 3));
		btns_glob.add(this.btn_quitter);
		btns_glob.add(this.btn_drawROI);
		btns_glob.add(this.btn_contrast);
		gauche.add(btns_glob);

		this.instru = new Panel();
		instru.setLayout(new GridLayout(2, 1));
		this.lbl_instructions = new Label();
		this.lbl_instructions.setBackground(Color.LIGHT_GRAY);
		instru.add(this.lbl_instructions);

		Panel btns_instru = this.createBtnsInstru();
		instru.add(btns_instru);
		
		gauche.add(instru);
		panel.add(gauche);
		add(panel);
		
		this.adaptWindow();
	}
	
	protected void adaptWindow() {
		pack();

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Point loc = getLocation();
		Dimension size = getSize();
		if (loc.y + size.height > screen.height) {
			getCanvas().zoomOut(0, 0);
		}

		// image adaptee dim fenetre
		//getCanvas().setScaleToFit(true);
		// On Pack la fenetre pour la mettre a la preferred Size
		this.pack();
		this.setSize(this.getPreferredSize());
		// On met au premier plan au centre de l'ecran
		this.setLocationRelativeTo(null);
		this.toFront();

		// affiche l'overlay D/G
		this.setOverlay();
	}
	
	protected Panel createBtnsInstru() {
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 3));
		btns_instru.add(this.btn_showlog);
		btns_instru.add(this.btn_precedent);
		btns_instru.add(this.btn_suivant);
		return btns_instru;
	}

	/// affiche l'overlay Droite/Gauche
	private void setOverlay() {
		// On initialise l'overlay avec les label DG
		Overlay overlay = VueScin.initOverlay(this.imp, 7);
		VueScin.setOverlayDG(overlay, this.imp);
		// On met sur l'image
		this.getImagePlus().setOverlay(overlay);
	}

	private void initButtons() {
		this.btn_contrast = new Button("Contrast");
		this.btn_drawROI = new Button("Draw ROI");
		this.btn_precedent = new Button("Previous");
		this.btn_precedent.setEnabled(false);
		this.btn_suivant = new Button("Next");
		this.btn_showlog = new Button("Show Log");
		this.btn_quitter = new Button("Quit");
	}
	
	public void setControleur(ControleurScin ctrl) {
		this.controleur = ctrl;
		
		//on affiche la premiere instruction
		this.setInstructions(0);
		
		this.btn_contrast.addActionListener(ctrl);
		this.btn_drawROI.addActionListener(ctrl);
		this.btn_precedent.addActionListener(ctrl);
		this.btn_quitter.addActionListener(ctrl);
		this.btn_showlog.addActionListener(ctrl);
		this.btn_suivant.addActionListener(ctrl);
	}
	
	@Override
	public boolean close() {		
		if(this.controleur != null) {
			this.controleur.roiManager.close();
		}
		return super.close();
	}

	private String generateTitle() {
		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = this.nom + " - ";
		titre = titre + tagNom + " - " + tagSerie;
		return titre;
	}

	public void windowClosing(WindowEvent we) {
		close();
		System.gc();
	}

	public void setInstructions(int nOrgane) {
		String s = "Delimit the " + this.controleur.getOrganes()[nOrgane];
		this.lbl_instructions.setText(s);
	}
	
	public void setInstructions(String inst) {
		this.lbl_instructions.setText(inst);
	}

	public Button getBtn_quitter() {
		return btn_quitter;
	}

	public Button getBtn_drawROI() {
		return btn_drawROI;
	}

	public Button getBtn_contrast() {
		return btn_contrast;
	}

	public Button getBtn_showlog() {
		return btn_showlog;
	}

	public Button getBtn_precedent() {
		return btn_precedent;
	}

	public Button getBtn_suivant() {
		return btn_suivant;
	}
	
	public Overlay getOverlay() {
		return this.getImagePlus().getOverlay();	
	}
	
	public ControleurScin getControleur() {
		return controleur;
	}
}
