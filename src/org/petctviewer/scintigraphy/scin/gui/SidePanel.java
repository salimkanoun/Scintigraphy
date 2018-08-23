package org.petctviewer.scintigraphy.scin.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.StaticMethod;

import ij.ImagePlus;

public class SidePanel extends Box {

	private static final long serialVersionUID = 6151539441728624822L;
	private Component sidePanelContent;

	public SidePanel(Component sidePanelContent, String titre, ImagePlus imp) {
		super(BoxLayout.Y_AXIS);
		this.sidePanelContent = sidePanelContent;

		this.fillbox(titre, imp);

		if (sidePanelContent != null) {
			this.add(sidePanelContent);
		}
		
	}

	public void addCaptureBtn(Scintigraphy scin, String additionalInfo, Component[] hide) {
		Component[] comp = setButtonAndLabel();

		ModeleScin modele = scin.getFenApplication().getControleur().getModele();
		
		Component[] hide1 = new Component[] {comp[0]};
		Component[] both = (Component[])ArrayUtils.addAll(hide1, hide);
		
		scin.setCaptureButton((JButton) comp[0], new Component[] {comp[1]}, both, comp[0], modele, additionalInfo);
	}

	public void addCaptureBtn(Scintigraphy scin, String additionalInfo) {
		Component[] comp = setButtonAndLabel();

		ModeleScin modele = scin.getFenApplication().getControleur().getModele();
		scin.setCaptureButton((JButton) comp[0], comp[1], this, modele, additionalInfo);
	}

	private Component[] setButtonAndLabel() {
		// bouton capture
		JButton btn_capture = new JButton("Capture");
		btn_capture.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(btn_capture);

		// label de credits
		JLabel lbl_credits = new JLabel("Provided by petctviewer.org");
		lbl_credits.setVisible(false);
		this.add(lbl_credits);

		return new Component[] { btn_capture, lbl_credits };
	}

	private void fillbox(String titre, ImagePlus imp) {
		this.setBorder(new EmptyBorder(0, 10, 0, 10));

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		JLabel titreFen = new JLabel("<html><h1>" + titre + "</h1><html>");
		titreFen.setHorizontalAlignment(SwingConstants.CENTER);
		flow.add(titreFen);
		this.add(flow);

		// ajout des informations du patient
		HashMap<String, String> infoPatient = StaticMethod.getPatientInfo(imp);
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("name")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));

		FenResultatSidePanel.setFontAllJLabels(patientInfo, new Font("Calibri", Font.BOLD, 16));

		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		this.add(flow1);
	}

	public void setSidePanelContent(Component sidePanelContent) {
		this.sidePanelContent = sidePanelContent;
	}

	public Component getSidePanelContent() {
		return this.sidePanelContent;
	}

}
