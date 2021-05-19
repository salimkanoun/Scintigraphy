package org.petctviewer.scintigraphy.os;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * DISCLAIMER : Dans cette application, il a été fait comme choix d'initialiser
 * le module par le biais du Contrôleur, qui va ensuite créer la vue et le
 * modèle.
 */
public class FenApplication_Os extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7121587802669948009L;
	private JSlider slider;
	private final JLabel sliderLabel;
	protected Box boxSlider;
	protected JButton reverseButton;

	private final JPanel grid; // Panneau central contenant les DynamicImage de la Scintigrapjie Osseuse

	protected final SidePanel sidePanel;
	final String additionalInfo;
	final String nomFen;

	/**
	 * Constructeur de la fenêtre permettant de visualiser la scintigraphie
	 * osseuse.<br/>
	 * Prend en paramètre la fenêtre qui la lance, ainsi que les images du
	 * patient.<br/>
	 * Le buffer transmis est un tableau à double dimension possédant 2 colonnes et
	 * n ligne (n = nombre de patient). Chaque ligne est un patient. <br/>
	 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus
	 * POST du patient.<br/>
	 * 
	 * Crée un sidePanel, qui affiche quelques informations du patient.<br/>
	 * Appelle finishBuildingWindow() à la fin de l'execution.<br/>
	 *
	 * @param controleur_os
	 *            Contrôleur permettant de réagir au click.
	 */
	public FenApplication_Os(Controleur_Os controleur_os) {
		super(new BorderLayout());

		this.additionalInfo = "Info";
		this.nomFen = "Fen";

		sidePanel = new SidePanel(null, "Bone scintigraphy", controleur_os.getImageSelection().getImagePlus());

		// sidePanel.addCaptureBtn(scin, "_other");
		this.add(sidePanel, BorderLayout.WEST);

		this.grid = new JPanel(new GridLayout(1, controleur_os.getNbScinti() * 2));

		this.add(grid, BorderLayout.CENTER);

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.finishBuildingWindow(controleur_os);
	}

	/**
	 * Fini la contruction de la fenêtre.<br/>
	 * Crée un slider et le place. Ce slider servira à faire varier le contraste des
	 * images.<br/>
	 * Crée le tableau des DynamicImage à partir des ImagePlus reçues.<br/>
	 * Affete à chaque DynamicImage un Listener, permettant de le selectionner.<br/>
	 * Enregistre la valeure macimale du slider, qui permet de gérer le
	 * contraste.<br/>
	 * Crée un gridLayout et ajoute dedans les DynamicImage créés.<br/>
	 */
	public void finishBuildingWindow(Controleur_Os controleur_Os) {

		this.slider = new JSlider(SwingConstants.HORIZONTAL, 0,
				(int) controleur_Os.getImageSelection().getImagePlus().getStatistics().max, 4);
		slider.addChangeListener(controleur_Os);

		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);

		this.reverseButton = new JButton("Invert"); // Boutton inversant le contraste.
		this.reverseButton.addActionListener(controleur_Os);
		this.reverseButton.setPreferredSize(new Dimension(200, 40));

		JPanel gbl = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 2; // third row
		c.insets = new Insets(10, 0, 0, 0); // top padding
		gbl.add(boxSlider);
		gbl.add(this.reverseButton, c);

		sidePanel.addContent(gbl);

		controleur_Os.createCaptureButton(sidePanel, new Component[] { this.slider, this.sliderLabel, this.reverseButton }, new Component[] {});

		// sidePanel.addCaptureBtn(getScin(), this.additionalInfo, new Component[] {
		// this.slider });
		this.add(sidePanel, BorderLayout.WEST);

		// get local graphics environment
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		// get maximum window bounds
		Rectangle maximumWindowBounds = graphicsEnvironment.getMaximumWindowBounds();
		double width = maximumWindowBounds.getWidth() * 0.75;
		double height = maximumWindowBounds.getHeight() * 0.75;
		this.setPreferredSize(new Dimension((int) width, (int) height));

	}

	public Box getBoxSlider() {
		return this.boxSlider;
	}

	public JSlider getSlider() {
		return this.slider;
	}

	public void cadrer(int index, boolean selected) {
		if (selected) {
			Border border1 = new CompoundBorder(LineBorder.createBlackLineBorder(),
					BorderFactory.createLineBorder(new Color(150, 40, 27), 4));

			Border border2 = new CompoundBorder(BorderFactory.createLoweredBevelBorder(), border1);
			((DynamicImage) grid.getComponent(index)).setBorder(border2);

		} else
			((DynamicImage) grid.getComponent(index))
					.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.black));
	}

	public JPanel getZoneAffichage() {
		return this.grid;
	}

	public JButton getReverseButton() {
		return this.reverseButton;
	}

}
