package org.petctviewer.scintigraphy.scin.gui;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;

import javax.swing.JTextField;
import javax.swing.UIManager;

import org.petctviewer.scintigraphy.scin.ControleurScin;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.util.DicomTools;

public class FenApplication extends StackWindow implements ComponentListener {
	private static final long serialVersionUID = -6280620624574294247L;

	//Panel d'instruction avec le textfield et boutons precedent et suivant
	protected Panel panel_Instructions_Text_Btn;
	
	//Panel avec boutons quit, draw roi, contrast
	private Panel panel_btns_Quit_Draw_Contrast;
	
	private JTextField textfield_instructions;

	private Button btn_quitter;
	private Button btn_drawROI;
	private Button btn_contrast;
	private Button btn_precedent;
	private Button btn_suivant;

	private ControleurScin controleur;
	
	private Panel panelPrincipal, global;
	
	
		

	private String nom;


	private int canvasW, canvasH;

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
/*
		try {
		    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		 } catch (Exception e) {
		            e.printStackTrace();
		 }
*/	 
		this.nom = nom;
		
		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = this.nom + " - "+tagNom + " - " + tagSerie;
		setTitle(titre);//frame title
		this.imp.setTitle(titre);//imp title

		global=new Panel();
		this.panelPrincipal = new Panel(new FlowLayout());

		// construit tous les boutons
		this.btn_contrast = new Button("Contrast");
		this.btn_drawROI = new Button("Draw ROI");
		this.btn_precedent = new Button("Previous");
		this.btn_precedent.setEnabled(false);
		this.btn_suivant = new Button("Next");
		this.btn_quitter = new Button("Quit");
		
		// panel contenant les boutons
		panel_btns_Quit_Draw_Contrast = new Panel();
		panel_btns_Quit_Draw_Contrast.setLayout(new GridLayout(1, 3));
		panel_btns_Quit_Draw_Contrast.add(this.btn_quitter);
		panel_btns_Quit_Draw_Contrast.add(this.btn_drawROI);
		panel_btns_Quit_Draw_Contrast.add(this.btn_contrast);
		panelPrincipal.add(panel_btns_Quit_Draw_Contrast);

		// Creation du panel instructions
		this.panel_Instructions_Text_Btn = new Panel();
		this.panel_Instructions_Text_Btn.setLayout(new GridLayout(2, 1));
		this.textfield_instructions = new JTextField();
		this.textfield_instructions.setEditable(false);
		this.textfield_instructions.setBackground(Color.LIGHT_GRAY);
		this.panel_Instructions_Text_Btn.add(this.getTextfield_instructions());

		Panel panel_Instructions_btns = this.createPanelInstructionsBtns();
		this.panel_Instructions_Text_Btn.add(panel_Instructions_btns);
		panelPrincipal.add(this.panel_Instructions_Text_Btn);
		
		
		global.add(this.panelPrincipal);
		this.add(global);

		this.setDefaultSize();
		this.addComponentListener(this);
		
	}

	public void resizeCanvas() {
		ImagePlus imp = this.getImagePlus();
		
		// on enleve puis remet l'image afin qu'elle reprenne ses dimension originales
		this.setImage(null);
		this.setImage(imp);
		
		this.getCanvas().setBounds(0,0,canvasW,canvasH);
		this.getCanvas().setSize(canvasW, canvasH);
		
		// on calcule le facteur de magnification
		double magnification =( canvasW / (1.0 * imp.getWidth()) );
		
		this.getCanvas().setMagnification(magnification);
		// pour que le pack prenne en compte les dimensions du panel
		//System.out.println(panelPrincipal.getPreferredSize());
		//this.panelPrincipal.setPreferredSize(panelPrincipal.getPreferredSize());
		global.revalidate();
		this.revalidate();
		System.out.println(panelPrincipal.getSize());

		//this.panel.setPreferredSize(panel.getPreferredSize());
	
		this.pack();
		this.setSize(this.getPreferredSize());
	}
	
	//Close la fenetre
	@Override
	public boolean close() {
		if (this.controleur != null) {
			this.controleur.getRoiManager().close();
		}
		return super.close();
	}

	
	/************Private Method *********/
	public Panel createPanelInstructionsBtns() {
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 3));
		btns_instru.add(this.btn_precedent);
		btns_instru.add(this.btn_suivant);
		return btns_instru;
	}


	/*************** Getter ******/
	public Button getBtn_quitter() {
		return this.btn_quitter;
	}
	
	public Button getBtn_drawROI() {
		return this.btn_drawROI;
	}

	public Button getBtn_contrast() {
		return this.btn_contrast;
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

	public Panel getPanelInstructionsTextBtn() {
		return this.panel_Instructions_Text_Btn;
	}
	
	public JTextField getTextfield_instructions() {
		return this.textfield_instructions;
	}

	public Panel getPanel_Quit_Draw_Contrast_btns() {
		return panel_btns_Quit_Draw_Contrast;
	}

	public Panel getPanelPrincipal() {
		return panelPrincipal;
	}

	/************* Setter *************/
	public void setControleur(ControleurScin ctrl) {
		this.controleur = ctrl;

		// on affiche la premiere instruction
		ctrl.setInstructionsDelimit(0);

		// on ajoute le controleur a tous les boutons
		this.btn_contrast.addActionListener(ctrl);
		this.btn_drawROI.addActionListener(ctrl);
		this.btn_precedent.addActionListener(ctrl);
		this.btn_quitter.addActionListener(ctrl);
		this.btn_suivant.addActionListener(ctrl);
	}

	public void setInstructionsTextBtn(Panel instru) {
		this.panel_Instructions_Text_Btn = instru;
	}

	public void setDefaultSize() {
		this.setPreferredCanvasSize(512);
	}

	public void setImp(ImagePlus imp) {
		this.setImage(imp);
		this.imp = imp;
		this.revalidate();
		this.resizeCanvas();
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

		if (ratioImagePlus<1) {
			canvasW = (int) (width * ratioImagePlus);
			canvasH = (int) (width);
			
		}else {
			canvasW = width;
			canvasH = (int) (width / ratioImagePlus);
		}
	

		resizeCanvas();
	}

	// // affiche l'overlay Droite/Gauche
	// private void setOverlay() {
	// // On initialise l'overlay avec les label DG
	// Overlay overlay = VueScin.initOverlay(this.imp, 7);
	// VueScin.setOverlayDG(overlay, imp);
	// // On met sur l'image
	// this.getImagePlus().setOverlay(overlay);
	// }
	
	
	/************Component***********/
	@Override
	public void windowClosing(WindowEvent we) {
		close();
		System.gc();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		this.canvasH = this.getCanvas().getHeight();
		this.canvasW = this.getCanvas().getWidth();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}
