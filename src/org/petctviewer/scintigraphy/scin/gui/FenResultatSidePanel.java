package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

public abstract class FenResultatSidePanel extends JFrame {

	private static final long serialVersionUID = -5212479342782678916L;

	private Box side;
	private Scintigraphy scin;
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
	public FenResultatSidePanel(String nomFen, Scintigraphy scin, BufferedImage capture, String additionalInfo) {
		this.scin = scin;
		this.modele = scin.getFenApplication().getControleur().getModele();
		this.additionalInfo = additionalInfo;

		this.setLayout(new BorderLayout());

		this.side = Box.createVerticalBox();
		this.side.setBorder(new EmptyBorder(0, 10, 0, 10));

		// ajout de la capture
		DynamicImage img = null;

		if (capture != null) {
			img = new DynamicImage(capture);
			this.add(img, BorderLayout.CENTER);
		}

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		JLabel titreFen = new JLabel("<html><h1>" + nomFen + "</h1><html>");
		titreFen.setHorizontalAlignment(SwingConstants.CENTER);
		flow.add(titreFen);
		this.side.add(flow);

		// ajout des informations du patient
		HashMap<String, String> infoPatient = ModeleScin.getPatientInfo(scin.getImp());
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("name")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));
		
		this.setFontAllJLabels(patientInfo, new Font("Calibri", Font.BOLD, 16));
		
		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		this.side.add(flow1);
	}
	
	public static void setFontAllJLabels(Container container, Font font) {
		if(container instanceof JLabel)
			container.setFont(font);
		
		for (Component c : container.getComponents()) {
			if(c instanceof Container) {
				setFontAllJLabels((Container) c, font);
			}
		}
	}

	/**
	 * Fini de construire la fenetre en incluant tous les components de la methode
	 * {@link #getSidePanelContent()}
	 */
	public void finishBuildingWindow(boolean capture) {
		this.side.add(Box.createVerticalGlue());

		// on ajoute tous les components de la methode getSidePanelContent
		Component comp = this.getSidePanelContent();

		if (comp != null) {
			this.side.add(comp);
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

		// on ajoute le side panel a droite de la fenetre
		this.add(this.side, BorderLayout.EAST);

		this.pack();

		this.setResizable(true);
		this.setLocationRelativeTo(null);
	}

	/**
	 * Renvoie les components a afficher dans le side panel
	 * 
	 * @return les components
	 */
	public abstract Component getSidePanelContent();

	public void setModele(ModeleScin modele) {
		this.modele = modele;
	}
	
	public ModeleScin getModele() {
		return this.modele;
	}

	public Component getSide() {
		return this.side;
	}

	public void setCaptureButton(JButton btn_capture, JLabel lbl_credits) {
		// on ajoute le listener pour la capture
		this.scin.setCaptureButton(btn_capture, lbl_credits, this, this.modele, this.additionalInfo);
	}

	public Scintigraphy getScin() {
		return this.scin;
	}

}
