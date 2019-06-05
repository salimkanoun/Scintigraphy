package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.controller.Controller_OrganeFixe;
import org.petctviewer.scintigraphy.scin.exceptions.UnauthorizedRoiLoadException;
import org.petctviewer.scintigraphy.scin.exceptions.UnloadRoiException;
import org.petctviewer.scintigraphy.scin.json.SaveAndLoad;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.util.DicomTools;

/**
 * Interface graphique principale de quantification dans imageJ
 * 
 * @author diego
 *
 */
public class FenApplication extends StackWindow implements ComponentListener, MouseWheelListener {
	private static final long serialVersionUID = -6280620624574294247L;

	public static final String BTN_TXT_NEXT = "Next";

	// Panel d'instruction avec le textfield et boutons precedent et suivant
	private Panel panel_Instructions_btns_droite;

	// Panel avec boutons quit, draw roi, contrast
	private final Panel panel_btns_gauche;
	private final Panel panel_btns_droite;

	protected final JTextField textfield_instructions;

	final Button btn_quitter;
	final Button btn_drawROI;
	final Button btn_contrast;
	final Button btn_precedent;
	protected final Button btn_suivant;

	private ControllerScin controleur;

	private final Panel panelPrincipal;
	final Panel panelContainer;

	protected final String studyName;

	private int canvasW, canvasH;

	private final MenuBar menuBar;
	protected final DocumentationDialog documentation;

	private Menu options;

	private Menu help;

	/**
	 * Cree et ouvre la fenetre principale de l'application
	 * 
	 * @param imp
	 *            ImagePlus a traiter
	 * @param studyName
	 *            Nom du type de scintigraphie
	 */
	public FenApplication(ImagePlus imp, String studyName) {
		this(imp, studyName, new ImageCanvas(imp));
	}

	public FenApplication(ImagePlus imp, String studyName, ImageCanvas canvas) {
		super(imp, canvas);
		// on set la lut des preferences
		Library_Gui.setCustomLut(imp);
		
		this.studyName = studyName;

		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = this.studyName + " - " + tagNom + " - " + tagSerie;
		setTitle(titre);// frame title
		this.imp.setTitle(titre);// imp title
		

		panelContainer = new Panel(new BorderLayout());
		this.panelPrincipal = new Panel(new FlowLayout());

		// construit tous les boutons
		this.btn_contrast = new Button("Contrast");
		this.btn_drawROI = new Button("Draw ROI");
		this.btn_precedent = new Button("Previous");
		this.btn_precedent.setEnabled(false);
		this.btn_suivant = new Button(BTN_TXT_NEXT);
		this.btn_quitter = new Button("Quit");

		// panel contenant les boutons
		panel_btns_gauche = new Panel();
		panel_btns_gauche.setLayout(new GridLayout(1, 3));
		panel_btns_gauche.add(this.btn_quitter);
		panel_btns_gauche.add(this.btn_drawROI);
		panel_btns_gauche.add(this.btn_contrast);
		panelPrincipal.add(panel_btns_gauche);

		// Creation du panel instructions
		this.panel_Instructions_btns_droite = new Panel();
		this.panel_Instructions_btns_droite.setLayout(new GridLayout(2, 1));
		this.textfield_instructions = new JTextField("Click To Start Exam");
		this.textfield_instructions.setEditable(false);
		this.textfield_instructions.setBackground(Color.LIGHT_GRAY);
		this.panel_Instructions_btns_droite.add(textfield_instructions);

		panel_btns_droite = this.createPanelInstructionsBtns();
		this.panel_Instructions_btns_droite.add(panel_btns_droite);

		panelPrincipal.add(this.panel_Instructions_btns_droite);

		panelContainer.add(this.panelPrincipal, BorderLayout.CENTER);
		this.add(panelContainer);

		this.documentation = this.createDocumentation();
		// Menu bar
		this.menuBar = new MenuBar();
		this.createMenuBar();

		this.setDefaultSize();
		this.addComponentListener(this);
		this.setResizable(false);
		

		Image icon = new ImageIcon(ClassLoader.getSystemResource("images/icons/frameIconBis.png")).getImage();
		this.setIconImage(icon);
	}

	protected DocumentationDialog createDocumentation() {
		return new DocumentationDialog(this);
	}

	public String getStudyName() {
		return this.studyName;
	}

	public void resizeCanvas() {
		ImagePlus imp = this.getImagePlus();

		this.getCanvas().setSize(canvasW, canvasH);

		// on calcule le facteur de magnification
		List<Double> magnifications = new ArrayList<>();
		magnifications.add(canvasW / (1.0 * imp.getWidth()));
		magnifications.add(canvasH / (1.0 * imp.getHeight()));

		Double magnification = Collections.min(magnifications);

		this.getCanvas().setMagnification(magnification);

		this.pack();
	}

	// Close la fenetre
	@Override
	public boolean close() {
		if (this.controleur != null) {
			this.controleur.close();
		}
		return super.close();
	}

	public Panel createPanelInstructionsBtns() {
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 2));
		btns_instru.add(this.btn_precedent);
		btns_instru.add(this.btn_suivant);
		return btns_instru;
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

	public Button getBtn_precedent() {
		return this.btn_precedent;
	}

	public Button getBtn_suivant() {
		return this.btn_suivant;
	}

	public Overlay getOverlay() {
		return this.getImagePlus().getOverlay();
	}

	public ControllerScin getControleur() {
		return this.controleur;
	}

	public Panel getPanel_Instructions_btns_droite() {
		return this.panel_Instructions_btns_droite;
	}

	public JTextField getTextfield_instructions() {
		return this.textfield_instructions;
	}

	public void setText_instructions(String instruction) {
		textfield_instructions.setText(instruction);
		this.pack();
	}

	public Panel getPanel_btns_gauche() {
		return panel_btns_gauche;
	}

	public Panel getPanelPrincipal() {
		return panelPrincipal;
	}

	public void setController(ControllerScin ctrl) {
		this.controleur = ctrl;

		// on affiche la premiere instruction
		if (ctrl instanceof Controller_OrganeFixe)
			((Controller_OrganeFixe) ctrl).setInstructionsDelimit(0);

		// on ajoute le controleur a tous les boutons
		this.btn_contrast.addActionListener(ctrl);
		this.btn_drawROI.addActionListener(ctrl);
		this.btn_precedent.addActionListener(ctrl);
		this.btn_quitter.addActionListener(ctrl);
		this.btn_suivant.addActionListener(ctrl);
	}

	public void setpanel_Instructions_btns_droite(Panel instru) {
		this.panel_Instructions_btns_droite = instru;
	}

	protected Panel getPanel_bttns_droit() {
		return panel_btns_droite;
	}

	public MenuBar getMenuBar() {
		return this.menuBar;
	}
	
	public Menu getMenuBarOptions() {
		return this.options;
	}
	
	public Menu getMenuBarHelp() {
		return this.help;
	}

	private void createMenuBar() {
		this.options = new Menu("Options");
		MenuItem loadRois = new MenuItem("Load ROIs from .zip");
		loadRois.addActionListener(e -> {
			try {
				SaveAndLoad saveAndLoad = new SaveAndLoad();
				saveAndLoad.importRoiList(FenApplication.this,
						FenApplication.this.controleur.getModel(), (ControllerWorkflow) FenApplication.this.controleur);

				FenApplication.this.getImagePlus()
						.setRoi(FenApplication.this.controleur.getModel().getRoiManager().getRoi(0));
				FenApplication.this.getImagePlus().getRoi().setStrokeColor(Color.RED);

				System.out.println(FenApplication.this.controleur.getModel().getRoiManager().getRoi(0));

			} catch (UnauthorizedRoiLoadException e1) {
				JOptionPane.showMessageDialog(FenApplication.this, "Error while loading ROIs:\n" + e1.getMessage(),
						"Selection error", JOptionPane.ERROR_MESSAGE);
				// e1.printStackTrace();
			} catch (UnloadRoiException e1) {
				IJ.log("ROIs not loaded");
			}

		});

		this.help = new Menu("Help");
		MenuItem doc = new MenuItem("Documentation");
		doc.addActionListener((event) -> documentation.setVisible(true));
		help.add(doc);

		options.add(loadRois);
		this.menuBar.add(options);
		this.menuBar.add(help);
		this.setMenuBar(this.menuBar);
	}

	protected void setDefaultSize() {
		this.setPreferredCanvasSize(512);
	}

	@Override
	public void setImage(ImagePlus imp) {
		super.setImage(imp);
		Library_Gui.setCustomLut(imp);
		this.imp = imp;
		this.revalidate();
		this.resizeCanvas();
	}

	/**
	 * redimension de la canvas selon la largeur voulue et aux dimensions de
	 * l'imageplus affichee
	 */
	protected void setPreferredCanvasSize(int width) {
		int w = this.getImagePlus().getWidth();
		int h = this.getImagePlus().getHeight();
		double ratioImagePlus = w * 1.0 / h * 1.0;

		if (ratioImagePlus < 1) {
			canvasW = (int) (width * ratioImagePlus);
			canvasH = width;

		} else {
			canvasW = width;
			canvasH = (int) (width / ratioImagePlus);
		}

		resizeCanvas();
	}

	@Override
	public void windowClosing(WindowEvent we) {
		close();
		System.gc();
	}

	@Override
	public void componentResized(ComponentEvent e) {

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
