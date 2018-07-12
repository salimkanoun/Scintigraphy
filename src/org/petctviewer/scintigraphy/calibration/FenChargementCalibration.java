package org.petctviewer.scintigraphy.calibration;

import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class FenChargementCalibration extends JFrame {

	private ControleurChargementCalibration ccc;
	private Box messages;
	
	public FenChargementCalibration(ArrayList<String[]> examList) {
		ccc = new ControleurChargementCalibration(examList, this);
		JButton afficher = new JButton("Afficher");
		afficher.addActionListener(ccc);
		this.add(afficher);
		 messages = Box.createVerticalBox();
		this.messages.add(new JLabel("Messages"));
		this.pack();

	}
	
	public void envoyerMessage(String message) {
		this.messages.add(new JLabel(message));
	}
	
	public void lancerRes(Doublet[][] data) {
		FenResultatsCalibration fen = new FenResultatsCalibration(data);
		fen.setVisible(true);
	}
	
}
