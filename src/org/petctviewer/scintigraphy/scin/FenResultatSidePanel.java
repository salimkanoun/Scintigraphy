package org.petctviewer.scintigraphy.scin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ij.ImagePlus;

public class FenResultatSidePanel extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5212479342782678916L;

	private BufferedImage capture;
	protected Box side;
	private JButton btn_capture;
	private VueScin vue;

	public FenResultatSidePanel(String nomFen, VueScin vueScin, BufferedImage capture) {

		this.vue = vueScin;
		this.capture = capture;

		this.setLayout(new BorderLayout());

		this.side = Box.createVerticalBox();
		this.side.setBorder(new EmptyBorder(0, 10, 0, 10));

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		JLabel titreFen = new JLabel("<html><h1>" + nomFen + "</h1><html>");
		titreFen.setHorizontalAlignment(JLabel.CENTER);
		flow.add(titreFen);
		this.side.add(flow);

		// ajout des informations du patient
		
		HashMap<String, String> infoPatient = ModeleScin.getDicomInfo(vueScin.getImp());
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("nom")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));
		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		side.add(flow1);
	}
	
	public void finishBuildingWindow() {
		side.add(Box.createVerticalGlue());
		
		for(Component c : this.getSidePanelContent()) {
			side.add(c);
		}

		side.add(Box.createVerticalGlue());

		this.btn_capture = new JButton("Capture");
		this.btn_capture.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.side.add(btn_capture);

		JLabel credits = new JLabel("Provided by petctviewer.org");
		credits.setVisible(false);
		side.add(credits);

		this.vue.fen_application.getControleur().setCaptureButton(btn_capture, credits, this);

		JLabel img = new JLabel();
		img.setIcon(new ImageIcon(capture));

		this.add(img, BorderLayout.WEST);
		this.add(side, BorderLayout.EAST);

		this.pack();
		this.setResizable(false);
		this.setVisible(true);
		this.setSize(this.getPreferredSize());
		this.setLocationRelativeTo(null);
	}

	public Component[] getSidePanelContent() {
		return new Component[] {new JLabel()};
	}

}
