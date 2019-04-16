package org.petctviewer.scintigraphy.hepatic.statique;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

public class FenResultat_Hepatic extends JFrame {

	private static final long serialVersionUID = -5261203439330504164L;

	private HashMap<String, String> resultats;

	public FenResultat_Hepatic(Scintigraphy scin, BufferedImage capture, ModeleScin model) {
		this.setLayout(new BorderLayout());
		this.resultats = ((Modele_Hepatic) model).getResultsHashMap();
		
		SidePanel side = new SidePanel(this.getSidePanelContent(), scin.getExamType(), model.getImagePlus());
		side.addCaptureBtn(scin, "", model);
		
		this.add(side, BorderLayout.EAST);
		this.add(new DynamicImage(capture), BorderLayout.CENTER);
		
		this.pack();
		this.setMinimumSize(side.getSize());
		this.setVisible(true);
	}

	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
		
		panel.add(new JLabel(""));
		for(String k : this.resultats.keySet()) {
			panel.add(getLabel(k));
		}
		
		JPanel flow = new JPanel();
		flow.add(panel);
	
		return flow;
	}
	
	private JLabel getLabel(String key) {
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.get(key));
		return lbl_hwb;
	}

}
