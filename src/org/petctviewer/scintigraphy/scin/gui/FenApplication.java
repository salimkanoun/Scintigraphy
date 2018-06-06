package org.petctviewer.scintigraphy.scin.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import javax.swing.JTextField;

import org.petctviewer.scintigraphy.scin.ControleurScin;

public class FenApplication extends StackWindow implements ComponentListener {
	private static final long serialVersionUID = -6280620624574294247L;

	private JTextField field_instructions;

	/// boutons
	private Button btn_quitter;
	private Button btn_drawROI;
	private Button btn_contrast;
	private Button btn_showlog;
	private Button btn_precedent;
	private Button btn_suivant;

	private ControleurScin controleur;

	private Panel instru;
	private Panel panel;
	private String nom;

	private Panel btns_glob;

	private int canvasW, canvasH;

	public Panel getBtns_glob() {
		return btns_glob;
	}

	/**
	 * Cree et ouvre la fenetre principale de l'application
	 * 
	 * @param imp
	 *            ImagePlus a traiter
	 * @param nom
	 *            Nom du type de scintigraphie
	 */
	public FenApplication(ImagePlus imp, String nom) {
		super(imp, new ImageCanvas(imp));

		this.nom = nom;
		setTitle(generateTitle());
		this.imp.setTitle(generateTitle());

		this.panel = new Panel(new FlowLayout());
		this.panel.setLayout(new FlowLayout());

		Panel gauche = new Panel();
		gauche.setLayout(new FlowLayout());

		// construit tous les boutons
		initButtons();

		// panel contenant les boutons
		btns_glob = new Panel();
		btns_glob.setLayout(new GridLayout(1, 3));
		btns_glob.add(this.btn_quitter);
		btns_glob.add(this.btn_drawROI);
		btns_glob.add(this.btn_contrast);
		gauche.add(btns_glob);

		// Creation du panel instructions
		this.instru = new Panel();
		this.instru.setLayout(new GridLayout(2, 1));
		this.field_instructions = new JTextField();
		this.field_instructions.setEditable(false);
		this.field_instructions.setBackground(Color.LIGHT_GRAY);
		this.instru.add(this.getField_instructions());

		Panel btns_instru = this.createBtnsInstru();
		this.instru.add(btns_instru);

		gauche.add(getInstru());
		this.panel.add(gauche);
		add(this.panel);

		this.setDefaultSize();
		this.addComponentListener(this);
	}

	public void setDefaultSize() {
		this.setPreferredCanvasSize(512);
	}

	/**
	 * redimension de la canvas selon la largeur voulue et aux dimensions de
	 * l'imageplus affichee
	 * 
	 * @param width
	 */
	public void setPreferredCanvasSize(int width) {
		int w = this.getImagePlus().getWidth();
		int h = this.getImagePlus().getHeight();
		Double ratioImagePlus = w * 1.0 / h * 1.0;

		canvasW = width;
		canvasH = (int) (width / ratioImagePlus);

		resizeCanvas();
	}

	public void resizeCanvas() {
		ImagePlus imp = this.getImagePlus();
		
		this.getCanvas().setBounds(0,0,canvasW,canvasH);
		this.getCanvas().setSize(canvasW, canvasH);
		
		double magnification = canvasW / (1.0 * imp.getWidth());
		
		this.getCanvas().setMagnification(magnification);
		// pour que le pack prenne en compte les dimensions du panel
		this.panel.setPreferredSize(panel.getPreferredSize());
		this.pack();
		
		this.panel.setPreferredSize(null);
	}

	/**
	 * @return
	 */
	protected Panel createBtnsInstru() {
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 3));
		btns_instru.add(this.btn_showlog);
		btns_instru.add(this.btn_precedent);
		btns_instru.add(this.btn_suivant);
		return btns_instru;
	}

	// // affiche l'overlay Droite/Gauche
	// private void setOverlay() {
	// // On initialise l'overlay avec les label DG
	// Overlay overlay = VueScin.initOverlay(this.imp, 7);
	// VueScin.setOverlayDG(overlay, imp);
	// // On met sur l'image
	// this.getImagePlus().setOverlay(overlay);
	// }

	// construit tous les boutons
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

		// on affiche la premiere instruction
		ctrl.setInstructionsDelimit(0);

		// on ajoute le controleur a tous les boutons
		this.btn_contrast.addActionListener(ctrl);
		this.btn_drawROI.addActionListener(ctrl);
		this.btn_precedent.addActionListener(ctrl);
		this.btn_quitter.addActionListener(ctrl);
		this.btn_showlog.addActionListener(ctrl);
		this.btn_suivant.addActionListener(ctrl);
	}

	@Override
	public boolean close() {
		if (this.controleur != null) {
			this.controleur.getRoiManager().close();
		}
		return super.close();
	}

	// genere le titre de la fenetre
	private String generateTitle() {
		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = this.nom + " - ";
		titre = titre + tagNom + " - " + tagSerie;
		return titre;
	}

	@Override
	public void windowClosing(WindowEvent we) {
		close();
		System.gc();
	}

	// affiche les instructions dans le text field
	public void setInstructions(String inst) {
		this.getField_instructions().setText(inst);
	}

	public Button getBtn_quitter() {
		return this.btn_quitter;
	}

	public Button getBtn_drawROI() {
		return this.btn_drawROI;
	}

	public Button getBtn_contrast() {
		return this.btn_contrast;
	}

	public Button getBtn_showlog() {
		return this.btn_showlog;
	}

	public Button getBtn_precedent() {
		return this.btn_precedent;
	}

	public Button getBtn_suivant() {
		return this.btn_suivant;
	}

	public Overlay getOverlay() {
		return this.getImagePlus().getOverlay();
	}

	public ControleurScin getControleur() {
		return this.controleur;
	}

	public Panel getInstru() {
		return this.instru;
	}

	public void setInstru(Panel instru) {
		this.instru = instru;
	}

	public JTextField getField_instructions() {
		return this.field_instructions;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		this.canvasH = this.getCanvas().getHeight();
		this.canvasW = this.getCanvas().getWidth();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// Auto-generated method stub

	}

	@Override
	public void componentShown(ComponentEvent e) {
		// Auto-generated method stub

	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// Auto-generated method stub

	}

}
