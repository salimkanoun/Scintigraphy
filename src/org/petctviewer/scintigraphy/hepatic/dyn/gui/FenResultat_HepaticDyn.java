package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.petctviewer.scintigraphy.hepatic.dyn.Vue_HepaticDyn;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import ij.ImagePlus;

public class FenResultat_HepaticDyn {

	private static final long serialVersionUID = 1094251580157650693L;

	private Container principal, vasculaire, third;
	private final int width = 1000, height = 800;

	public FenResultat_HepaticDyn(Vue_HepaticDyn vue, BufferedImage capture) {
		this.principal = new TabPrincipal(vue, capture, width, height).getContentPane();
		this.third = new TabTAC(vue, width, height).getContentPane();
		this.vasculaire = new TabVasculaire(vue, width, height).getContentPane();
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
