package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
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

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

/**
 * creer une box avec titre du programme et son identification patient
 * 
 * @author diego
 *
 */
public class SidePanel extends JPanel {

	private static final long serialVersionUID = 6151539441728624822L;
	
	public static final String BTN_TXT_CAPTURE =  "Capture";
	
	private Box box;
	private Component sidePanelContent;
	private JPanel panSouth;
	private JLabel titreFen;

	public SidePanel(Component sidePanelContent, String titre, ImagePlus imp) {
		super(new BorderLayout());
		this.box = new Box(BoxLayout.Y_AXIS);
		this.sidePanelContent = sidePanelContent;

		this.panSouth = new JPanel();

		this.fillbox(titre, imp);

		if (sidePanelContent != null) {
			this.box.add(sidePanelContent);
		}

		this.add(this.box, BorderLayout.CENTER);
		this.add(this.panSouth, BorderLayout.SOUTH);

	}

	private void fillbox(String titre, ImagePlus imp) {
		this.box.setBorder(new EmptyBorder(0, 10, 0, 10));

		// ajout du titre de la fenetre
		JPanel flow = new JPanel();
		titreFen = new JLabel("<html><h1>" + titre + "</h1><html>");
		titreFen.setHorizontalAlignment(SwingConstants.CENTER);
		flow.add(titreFen);
		this.box.add(flow);

		// ajout des informations du patient
		HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(imp);
		JPanel patientInfo = new JPanel(new GridLayout(3, 2, 10, 10));
		patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(infoPatient.get("name")));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(infoPatient.get("id")));
		patientInfo.add(new JLabel("Aquisition date: "));
		patientInfo.add(new JLabel(infoPatient.get("date")));

		SidePanel.setFontAllJLabels(patientInfo, new Font("Calibri", Font.BOLD, 16));

		JPanel flow1 = new JPanel(new FlowLayout());
		flow1.add(patientInfo);
		this.box.add(flow1);
	}

	public void setSidePanelContent(Component sidePanelContent) {
		if (this.sidePanelContent != null)
			this.box.remove(this.sidePanelContent);
		this.sidePanelContent = sidePanelContent;
		this.box.add(sidePanelContent);
	}

	public void addContent(Component component) {
		this.box.add(component);
	}

	public Component getSidePanelContent() {
		return this.sidePanelContent;
	}

	public static void setFontAllJLabels(Container container, Font font) {
		if (container instanceof JLabel)
			container.setFont(font);

		for (Component c : container.getComponents()) {
			if (c instanceof Container) {
				setFontAllJLabels((Container) c, font);
			}
		}
	}

	public void createCaptureButton(TabResult tab) {
		this.createCaptureButton(tab, null);
	}

	public void createCaptureButton(TabResult tab, String additionalInfo) {
		this.createCaptureButton(tab, new Component[0], new Component[0], additionalInfo);
	}

	public void createCaptureButton(TabResult tab, Component[] hide, Component[] show, String additionalInfo) {
		
		

		// label de credits
		JLabel lbl_credits = new JLabel("Provided by petctviewer.org");
		lbl_credits.setVisible(false);
		this.panSouth.add(lbl_credits);

		// capture button
		CaptureButton captureButton = new CaptureButton(BTN_TXT_CAPTURE, tab, lbl_credits);
		captureButton.addActionListener(tab.getParent().getController());

		captureButton.setHorizontalAlignment(JButton.CENTER);
		captureButton.setEnabled(true);
		this.panSouth.add(captureButton);
	}

	public void setTitle(String title) {
		titreFen.setText("<html><h1>" + title + "</h1><html>");
	}

}
