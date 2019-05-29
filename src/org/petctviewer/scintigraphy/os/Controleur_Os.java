package org.petctviewer.scintigraphy.os;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.frame.RoiManager;

/**
 * DISCLAIMER : Dans cette application, il a été fait comme choix d'initialiser
 * le module par le biais du Contrôleur, qui va ensuite créer la vue et le
 * modèle.
 */
public class Controleur_Os implements ActionListener, ChangeListener, MouseListener {

	Modele_Os modele;
	FenApplication_Os vue;

	ImageSelection imp;

	public Controleur_Os(ImageSelection[] imps, OsScintigraphy scin) {

		this.modele = new Modele_Os(imps);
		this.imp = imps[0];

		this.vue = new FenApplication_Os(this);
		initialiser_Vue();
	}

	/**
	 * Listener permmettant d'inverser la LUT de chaque image, et donc son
	 * contraste.
	 * 
	 * @param arg0
	 * 
	 * @return
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (((JButton) arg0.getSource()) == this.vue.getReverseButton()) {
			this.modele.inverser();
			rafraichir();
		}
	}

	/**
	 * A chaque modification du slider, on change la valeur du contrast par
	 * setContrast(int).<br/>
	 * 
	 * @param e
	 *            Origine de l'évènement
	 * @return
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.modele.setContrast(slider);
		rafraichir();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	/**
	 * Listener permmettant de selectionner les images.
	 * 
	 * @param arg0
	 * 
	 * @return
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
		JPanel di = (JPanel) arg0.getSource();
		boolean ctrl = (arg0.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK;

		List<Integer> selected = modele.getSelected();

		if (!ctrl) {
			this.deselectAll();
		}

		// This loop look for the DynamicImage clicked on the DynamicImage list
		// (dynamicImps)
		for (int i = 0; i < this.modele.getNbScinti(); i++) {
			for (int j = 0; j < 2; j++) {
				// When we found it
				if (di == this.modele.getDynamicImage(i, j)) {

					// We perform this DynamicImage
					this.modele.perform(i, j);
					this.vue.cadrer(i * 2 + j, modele.isSelected(i, j));
					// We put the slider value to the current ImagePlus contrast value.
					this.vue.getSlider().setValue((int) ((this.vue.getSlider().getModel().getMaximum()
							- this.modele.getImagePlus(i, j).getImagePlus().getLuts()[0].max) + 1));
					if ((selected.size() == 1 && (selected.get(0) == (i * 2 + j))))
						this.deselectAll();
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	private void rafraichir() {
		JPanel panel = this.vue.getZoneAffichage();
		panel.revalidate();
		panel.repaint();
	}

	private void initialiser_Vue() {
		JPanel panel = this.vue.getZoneAffichage();
		for (int i = 0; i < this.modele.getNbScinti(); i++) { // Pour toutes les images
			for (int j = 0; j < 2; j++) {
				panel.add(modele.getDynamicImage(i, j));
				modele.getDynamicImage(i, j).addMouseListener(this);

				if (Prefs.get("bone.defaultInverse.preferred", true))
					modele.getDynamicImage(i, j).setBackground(Color.white);
				else
					modele.getDynamicImage(i, j).setBackground(Color.black);
			}
		}
	}

	public FenApplication_Os getFenApplicatio_Os() {
		return this.vue;
	}

	public ImageSelection getImageSelection() {
		return this.modele.getImageSelection();
	}

	public int getNbScinti() {
		return this.modele.getNbScinti();
	}

	public void deselectAll() {
		this.modele.deselectAll();

		for (int i = 0; i < this.modele.getNbScinti(); i++)
			for (int j = 0; j < 2; j++)
				this.vue.cadrer(i * 2 + j, modele.isSelected(i, j));
	}

	public void createCaptureButton(SidePanel sidePanel, Component[] hide, Component[] show, String additionalInfo) {
		// capture button
		JButton captureButton = new JButton("Capture");

		// label de credits
		JLabel lbl_credits = new JLabel("Provided by petctviewer.org");
		lbl_credits.setVisible(false);
		sidePanel.addContent(lbl_credits);

		// generation du tag info
		String info = Library_Capture_CSV.genererDicomTagsPartie1(this.modele.getImagePlus(),
				this.modele.getStudyName(), this.modele.getUID6digits())
				+ Library_Capture_CSV.genererDicomTagsPartie2(this.modele.getImagePlus());

		// on ajoute le listener sur le bouton capture
		captureButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				captureButton.setVisible(false);
				for (Component comp : hide)
					comp.setVisible(false);

				lbl_credits.setVisible(true);
				for (Component comp : show)
					comp.setVisible(true);

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// Capture, nouvelle methode a utiliser sur le reste des programmes
						BufferedImage capture = new BufferedImage(Controleur_Os.this.vue.getWidth(),
								Controleur_Os.this.vue.getHeight(), BufferedImage.TYPE_INT_ARGB);
						Controleur_Os.this.vue.paint(capture.getGraphics());
						ImagePlus imp = new ImagePlus("capture", capture);

						captureButton.setVisible(true);
						for (Component comp : hide)
							comp.setVisible(true);

						lbl_credits.setVisible(false);
						for (Component comp : show)
							comp.setVisible(false);

						// on passe a la capture les infos de la dicom
						imp.setProperty("Info", info);
						// on affiche la capture
						imp.show();

						// on change l'outil
						IJ.setTool("hand");

						// generation du csv
						String resultats = Controleur_Os.this.modele.toString();
						ControllerWorkflow controller = new ControllerWorkflow(null, null, null) {
							@Override
							protected void generateInstructions() {
							}
						};

						try {
							Library_Capture_CSV.exportAll(resultats, new RoiManager(),
									Controleur_Os.this.modele.getStudyName(), imp, "", controller);

							imp.killRoi();
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						// Execution du plugin myDicom
						try {
							IJ.run("myDicom...");
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						System.gc();
					}
				});

			}
		});

		captureButton.setHorizontalAlignment(JButton.CENTER);
		captureButton.setEnabled(true);
		sidePanel.addContent(captureButton);
	}

}
