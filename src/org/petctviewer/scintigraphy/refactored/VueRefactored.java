package org.petctviewer.scintigraphy.refactored;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JLabel;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;
import ij.util.DicomTools;

public class VueRefactored extends StackWindow implements ComponentListener {

	private static final long serialVersionUID = -6280620624574294247L;

	private Label instructions;

	/// boutons
	private Button btn_quitter;
	private Button btn_drawROI;
	private Button btn_contrast;
	private Button btn_showlog;
	private Button btn_precedent;
	private Button btn_suivant;

	private int canvasW, canvasH;

	private String nom;
	private Panel panel;

	/**
	 * Cree et ouvre la fenetre principale de l'application
	 * 
	 * @param imp
	 *            ImagePlus a traiter
	 * @param nom
	 *            Nom du type de scintigraphie
	 */
	public VueRefactored(ImagePlus imp, String nom) {
		super(imp, new ImageCanvas(imp));
		
		Controleur ctrl = new Controleur(this);
		
		//on cree tous les boutons
		this.btn_contrast = new Button("Contrast");
		this.btn_drawROI = new Button("Draw ROI");
		this.btn_precedent = new Button("Previous");
		this.btn_precedent.setEnabled(false);
		this.btn_suivant = new Button("Next");
		this.btn_showlog = new Button("Show Log");
		this.btn_quitter = new Button("Quit");

		// on ajoute le controleur a tous les boutons
		this.btn_contrast.addActionListener(ctrl);
		this.btn_drawROI.addActionListener(ctrl);
		this.btn_precedent.addActionListener(ctrl);
		this.btn_quitter.addActionListener(ctrl);
		this.btn_showlog.addActionListener(ctrl);
		this.btn_suivant.addActionListener(ctrl);

		setTitle(generateTitle());
		this.imp.setTitle(generateTitle());

		this.nom = nom;

		this.panel = new Panel(new FlowLayout());

		// panel contenant les boutons
		Panel btns_glob = new Panel();
		btns_glob.setLayout(new GridLayout(1, 3));
		btns_glob.add(this.btn_quitter);
		btns_glob.add(this.btn_drawROI);
		btns_glob.add(this.btn_contrast);
		panel.add(btns_glob);

		// Creation du panel instructions
		Panel instru = new Panel();
		instru.setLayout(new GridLayout(2, 1));
		this.instructions = new Label();
		this.instructions.setBackground(Color.LIGHT_GRAY);
		instru.add(instructions);

		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 3));
		btns_instru.add(this.btn_showlog);
		btns_instru.add(this.btn_precedent);
		btns_instru.add(this.btn_suivant);
		instru.add(btns_instru);

		this.panel.add(instru);
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

		// on enleve puis remet l'image afin qu'elle reprenne ses dimension originales
		this.setImage(null);
		this.setImage(imp);

		this.getCanvas().setBounds(0, 0, canvasW, canvasH);
		this.getCanvas().setSize(canvasW, canvasH);

		// on calcule le facteur de magnification
		double magnification = canvasW / (1.0 * imp.getWidth());

		this.getCanvas().setMagnification(magnification);
		// pour que le pack prenne en compte les dimensions du panel
		this.panel.setPreferredSize(panel.getPreferredSize());
		this.pack();

		this.panel.setPreferredSize(null);
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
	
	public Label getInstructions() {
		return instructions;
	}

}
