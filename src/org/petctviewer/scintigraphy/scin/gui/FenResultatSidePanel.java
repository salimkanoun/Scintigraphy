package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.petctviewer.scintigraphy.renal.gui.BackgroundPanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.plugin.Concatenator;
import ij.plugin.MontageMaker;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public abstract class FenResultatSidePanel extends JFrame {

	private static final long serialVersionUID = -5212479342782678916L;

	protected Box side;
	private VueScin vue;

	private String additionalInfo;

	private ModeleScin modele;

	/**
	 * Fenetre d'affichage de resultat presentant les informations du patient et le
	 * bouton capture dans un side panel sur la droite
	 * 
	 * @param nomFen
	 *            : Nom de la fenetre
	 * @param vueScin
	 *            : Vue correspondante
	 * @param capture
	 *            : capture de l'imp a afficher
	 * @param additionalInfo
	 *            : informations supplementaires a ajouter au nom de fichier lors de
	 *            la sauvegarde
	 */
	public FenResultatSidePanel(String nomFen, VueScin vue, BufferedImage capture, String additionalInfo) {

		this.vue = vue;
		this.modele = vue.getFenApplication().getControleur().getModele();
		this.additionalInfo = additionalInfo;

		this.setLayout(new BorderLayout());

		this.side = Box.createVerticalBox();
		this.side.setBorder(new EmptyBorder(0, 10, 0, 10));

		// ajout de la capture
		BackgroundPanel img = null;

		if (capture != null) {
			img = new BackgroundPanel(capture);
			this.add(img, BorderLayout.WEST);
		}else {
			this.add(new JPanel(), BorderLayout.WEST);
		}

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		JLabel titreFen = new JLabel("<html><h1>" + nomFen + "</h1><html>");
		titreFen.setHorizontalAlignment(SwingConstants.CENTER);
		flow.add(titreFen);
		this.side.add(flow);

		// ajout des informations du patient
		HashMap<String, String> infoPatient = ModeleScin.getPatientInfo(vue.getImp());
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("name")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));
		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		this.side.add(flow1);
	}

	/**
	 * Fini de construire la fenetre en incluant tous les components de la methode
	 * {@link #getSidePanelContent()}
	 */
	public void finishBuildingWindow(boolean capture) {
		this.side.add(Box.createVerticalGlue());

		// on ajoute tous les components de la methode getSidePanelContent
		Component[] comp = this.getSidePanelContent();

		if (comp != null) {
			for (Component c : comp) {
				if (c != null) {
					this.side.add(c);
				}
			}
		}

		this.side.add(Box.createVerticalGlue());

		if (capture) {
			// bouton capture
			JButton btn_capture = new JButton("Capture");
			btn_capture.setAlignmentX(Component.CENTER_ALIGNMENT);
			this.side.add(btn_capture);

			// label de credits
			JLabel lbl_credits = new JLabel("Provided by petctviewer.org");
			lbl_credits.setVisible(false);
			this.side.add(lbl_credits);

			this.setCaptureButton(btn_capture, lbl_credits);
		}

		// on ajoute le sie panel a droite de la fenetre
		this.add(this.side, BorderLayout.EAST);

		this.pack();

		this.setResizable(false);
		this.setSize(this.getPreferredSize());
		this.setLocationRelativeTo(null);
	}

	/**
	 * Renvoie les components a afficher dans le side panel
	 * 
	 * @return les components
	 */
	public abstract Component[] getSidePanelContent();

	public void setModele(ModeleScin modele) {
		this.modele = modele;
	}

	/**
	 * Renvoie un montage
	 * 
	 * @param frameDuration
	 * @param imp
	 * @param size
	 * @return
	 */
	public static ImagePlus creerMontage(int[] frameDuration, ImagePlus imp, int size, int rows, int columns) {
		int nSlice = frameDuration.length;

		int[] summed = new int[frameDuration.length];
		summed[0] = frameDuration[0];
		for (int i = 1; i < nSlice; i++) {
			summed[i] = summed[i - 1] + frameDuration[i];
		}

		int[] sliceIndex = new int[(rows * columns) + 1];
		int pas = summed[nSlice - 1] / (rows * columns);
		for (int i = 0; i < (rows * columns) + 1; i++) {
			for (int j = 0; j < summed.length; j++) {
				if (i * pas <= summed[j] || j == summed.length - 1) {
					sliceIndex[i] = j;
					break;
				}
			}
		}

		ImagePlus[] impList = new ImagePlus[rows * columns];
		for (int i = 1; i < sliceIndex.length; i++) {
			int start = sliceIndex[i - 1];
			int stop = sliceIndex[i];
			ImagePlus tinyImp = ZProjector.run(imp, "sum", start, stop);

			ImageProcessor impc = tinyImp.getProcessor();
			impc.setInterpolationMethod(ImageProcessor.BICUBIC);
			impc = impc.resize(size);

			ImagePlus projectionImp = new ImagePlus("", impc);

			impList[i - 1] = projectionImp;
		}

		Concatenator enchainer = new Concatenator();
		ImagePlus impStacked = enchainer.concatenate(impList, false);

		MontageMaker mm = new MontageMaker();

		return mm.makeMontage2(impStacked, columns, rows, 1.0, 1, impList.length, 1, 0, false);
	}

	public Component getSide() {
		return this.side;
	}

	public static BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();

		return dimg;
	}

	public void setCaptureButton(JButton btn_capture, JLabel lbl_credits) {
		// on ajoute le listener pour la capture
		this.vue.setCaptureButton(btn_capture, lbl_credits, this, this.modele, this.additionalInfo);
	}

	public VueScin getVue() {
		return this.vue;
	}

}
