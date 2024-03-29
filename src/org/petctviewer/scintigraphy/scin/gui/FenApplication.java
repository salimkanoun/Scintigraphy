package org.petctviewer.scintigraphy.scin.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.exceptions.UnauthorizedRoiLoadException;
import org.petctviewer.scintigraphy.scin.exceptions.UnloadRoiException;
import org.petctviewer.scintigraphy.scin.json.SaveAndLoad;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.preferences.PrefTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interface graphique principale de quantification dans imageJ
 *
 * @author diego
 */
public class FenApplication extends StackWindow implements ComponentListener{
	public static final String BTN_TXT_NEXT = "Next";
	private static final long serialVersionUID = -6280620624574294247L;
	protected final JTextField textfield_instructions;
	protected final Button btn_suivant;
	protected final String studyName;
	final Button btn_quitter;
	final Button btn_drawROI;
	final Button btn_reverse;
	private final JSlider slider;


	final Button zoom_in;
	final Button zoom_out;

	final Button btn_precedent;
	final Panel panelContainer;
	// Panel avec boutons quit, draw roi, contrast
	private final Panel panel_btns_gauche;
	private final Panel panel_btns_droite;
	private final Panel panelPrincipal;
	private DocumentationDialog documentation;
	private MenuBar menuBar;

	// Panel d'instruction avec le textfield et boutons precedent et suivant
	private final Panel panel_Instructions_btns_droite;
	private ControllerScin controleur;
	private int canvasW, canvasH;
	private JDialog preferences;
	private Menu options;
	private MenuItem menuItem_preferences;
	private MenuItem loadRois;
	private Menu help;

	/**
	 * Cree et ouvre la fenetre principale de l'application
	 *
	 * @param imp       ImagePlus a traiter
	 * @param studyName Nom du type de scintigraphie
	 */
	public FenApplication(ImagePlus imp, String studyName) {
		this(imp, studyName, new ImageCanvas(imp));
	}

	public FenApplication(ImagePlus imp, String studyName, ImageCanvas canvas) {
		super(imp, canvas);

		Library_Gui.setCustomLut(this.imp);

		this.studyName = studyName;

		String tagSerie = DicomTools.getTag(this.imp, "0008,103E");
		String tagNom = DicomTools.getTag(this.imp, "0010,0010");
		String titre = this.studyName + " - " + tagNom + " - " + tagSerie;
		setTitle(titre);// frame title
		this.imp.setTitle(titre);// imp title

		panelContainer = new Panel(new BorderLayout());

		this.panelPrincipal = new Panel(new GridLayout(2, 2));

		// construit tous les boutons
		this.btn_reverse = new Button("Reverse");
		this.btn_drawROI = new Button("Draw ROI");
		this.zoom_out = new Button("-");
		this.zoom_in = new Button("+");



		this.btn_precedent = new Button("Previous");
		this.btn_precedent.setEnabled(false);
		this.btn_suivant = new Button(BTN_TXT_NEXT);
		this.btn_quitter = new Button("Quit");
		this.slider = new JSlider(SwingConstants.HORIZONTAL, 1, 255, 1);

		// panel contenant les boutons
		panel_btns_gauche = new Panel();
		panel_btns_gauche.setLayout(new GridLayout(1, 3));
		panel_btns_gauche.add(this.btn_quitter);
		panel_btns_gauche.add(this.btn_drawROI);
		panel_btns_gauche.add(this.btn_reverse);
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

		//creation panel slider
		Panel panel_slider = new Panel(new GridLayout(2, 1));
		panel_slider.add(new Label("Contrast", Label.CENTER));
		panel_slider.add(this.slider);
		this.slider.addChangeListener( e-> {
			double max = IJ.getValue(this.imp, "Max");
			double coef = this.slider.getMaximum() / max;
			double val = (slider.getMaximum() - slider.getValue())/ coef;
			IJ.setMinAndMax(0, val);

		});

		panelPrincipal.add(panel_slider);

		//création du panel Zoom
		Panel panel_zoom = new Panel(new GridBagLayout());
		Panel panel_container_zoom = new Panel();
		panel_container_zoom.add(new Label("Zoom", Label.CENTER));
		panel_container_zoom.add(this.zoom_out);
		panel_container_zoom.add(this.zoom_in);
		panel_zoom.add(panel_container_zoom, new GridBagConstraints());
		panelPrincipal.add(panel_zoom);

		panelContainer.add(this.panelPrincipal);
		this.add(panelContainer, BorderLayout.SOUTH);

//		panelContainer.setPreferredSize(new Dimension(512, (int) panelContainer.getPreferredSize().getHeight()));
		// Menu bar
		this.createMenuBar();
		this.getLayout().layoutContainer(panelContainer);

		this.setDefaultSize();
//		this.setSize(new Dimension(512, (int)this.getPreferredSize().getHeight()));
		this.addComponentListener(this);
		this.setResizable(false);
		this.pack();

		URL res = this.getClass().getClassLoader().getResource("images/icons/frameIconBis.png");
		if (res != null) this.setIconImage(Toolkit.getDefaultToolkit().getImage(res));
	}

	private void createMenuBar() {
		this.menuBar = new MenuBar();
		options = new Menu("Options");
		// Load ROIs
		this.loadRois = new MenuItem("Load ROIs from .zip");
		loadRois.addActionListener(e -> {
			try {
				SaveAndLoad saveAndLoad = new SaveAndLoad();
				saveAndLoad.importRoiList(FenApplication.this, FenApplication.this.controleur.getModel(),
						(ControllerWorkflow) FenApplication.this.controleur);

				((ControllerWorkflow) this.controleur).start();

				FenApplication.this.getImagePlus().setRoi(
						FenApplication.this.controleur.getModel().getRoiManager().getRoi(0));

				FenApplication.this.getImagePlus().getRoi().setStrokeColor(Color.RED);
			} catch (UnauthorizedRoiLoadException e1) {
				JOptionPane.showMessageDialog(FenApplication.this, "Error while loading ROIs:\n" + e1.getMessage(),
						"Selection error", JOptionPane.ERROR_MESSAGE);
			} catch (UnloadRoiException e1) {
				IJ.log("ROIs not loaded");
			}

		});
		this.loadRois.setEnabled(false);

		this.help = new Menu("Help");
		MenuItem doc = new MenuItem("Documentation");
		doc.addActionListener((event) -> {
			if (documentation != null) documentation.setVisible(true);
		});
		help.add(doc);

		options.add(loadRois);
		this.menuBar.add(options);
		this.menuBar.add(help);
		this.setMenuBar(this.menuBar);
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
	public Button getZoom_in() {
		return zoom_in;
	}

	public Button getZoom_out() {
		return zoom_out;
	}
	public Button getBtn_quitter() {
		return this.btn_quitter;
	}

	public Button getBtn_drawROI() {
		return this.btn_drawROI;
	}

	public Button getBtn_reverse() {
		return this.btn_reverse;
	}

	public Button getBtn_precedent() {
		return this.btn_precedent;
	}

	public Button getBtn_suivant() {
		return this.btn_suivant;
	}

	public JSlider getSlider(){return this.slider;}

	public Overlay getOverlay() {
		return this.getImagePlus().getOverlay();
	}

	public ControllerScin getController() {
		return this.controleur;
	}

	public void setController(ControllerScin ctrl) {
		this.controleur = ctrl;

		// on ajoute le controleur a tous les boutons
		this.btn_reverse.addActionListener(ctrl);
		this.btn_drawROI.addActionListener(ctrl);
		this.btn_precedent.addActionListener(ctrl);
		this.btn_quitter.addActionListener(ctrl);
		this.btn_suivant.addActionListener(ctrl);
		this.zoom_in.addActionListener(ctrl);
		this.zoom_out.addActionListener(ctrl);
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

	public MenuBar getMenuBar() {
		return this.menuBar;
	}

	@Override
	public void setImage(ImagePlus imp) {
		// Use previous image LUT
		imp.setLut(this.imp.getProcessor().getLut());
		super.setImage(imp);
		this.resizeCanvas();
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

	/**
	 * Sets the documentation dialog for this application. When a preference tab is set, it will be accessible in the
	 * menu bar.<br> If null is passed, it will remove the previous documentation set (if any).
	 *
	 * @param documentation Documentation dialog associated with this application
	 */
	public void setDocumentation(DocumentationDialog documentation) {
		this.documentation = documentation;
	}

	/**
	 * Sets the preference tab for this application. When a preference tab is set, it will be accessible in the menu
	 * bar.<br> If null is passed, it will remove the previous preference set (if any).
	 *
	 * @param preferences Preference tab associated with this application
	 */
	public void setPreferences(PrefTab preferences) {
		if (preferences == null) {
			// Remove menu item
			if (this.menuItem_preferences != null) this.options.remove(this.menuItem_preferences);
			this.preferences = null;
		} else {
			// Create preferences
			this.preferences = new JDialog(this, "Preferences - " + preferences.getTabName(), true);
			this.preferences.add(preferences);

			this.menuItem_preferences = new MenuItem("Preferences");
			this.menuItem_preferences.addActionListener(e -> {
				this.preferences.pack();
				this.preferences.setLocationRelativeTo(this);
				this.preferences.setVisible(true);
			});

			this.options.add(this.menuItem_preferences);
		}
	}

	public Panel getZoneAffichage() {
		return this.panelPrincipal;
	}

	public MenuItem getLoadRoisMenuItem() {
		return this.loadRois;
	}

	public Menu getHelpMenu() {
		return this.help;
	}

	protected Panel getPanel_bttns_droit() {
		return panel_btns_droite;
	}

	protected void setDefaultSize() {
		this.setPreferredCanvasSize(512);
	}

	/**
	 * redimension de la canvas selon la largeur voulue et aux dimensions de l'imageplus affichee
	 */
	protected void setPreferredCanvasSize(int width) {
		int w = this.getImagePlus().getWidth();
		int h = this.getImagePlus().getHeight();
		double ratioImagePlus = w * 1.0 / h;

		if (ratioImagePlus < 1) {
			canvasW = (int) (width * ratioImagePlus);
			canvasH = width;

		} else {
			canvasW = width;
			canvasH = (int) (width / ratioImagePlus);
		}

		resizeCanvas();
	}
}
