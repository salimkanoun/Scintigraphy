package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.hepatic.dyn.HepaticDynamicScintigraphy;

public class FenResultat_HepaticDyn {

	private Container principal, vasculaire, third;
	private final int width = 1000, height = 800;

	public FenResultat_HepaticDyn(HepaticDynamicScintigraphy vue, BufferedImage capture) {
		this.principal = new TabPrincipal(vue, capture, width, height);
		this.third = new TabTAC(vue, width, height);
		this.vasculaire = new TabVasculaire(vue, width, height);
		showGUI();
	}

	private void showGUI() {
		JFrame frame = new JFrame("Results Hepatic");
		frame.setLayout(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Main", this.principal);
		tabbedPane.addTab("TAC", this.third);
		tabbedPane.addTab("Vascular", this.vasculaire);
		
		frame.getContentPane().add(tabbedPane);
		frame.setPreferredSize(new Dimension(width, height));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
