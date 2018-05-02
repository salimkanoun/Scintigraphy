package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public class FenResultat_HepaticDyn extends JFrame {

	private static final long serialVersionUID = 1094251580157650693L;
	
	private HashMap<String, String> resultats;

	public FenResultat_HepaticDyn(Vue_HepaticDyn vue, BufferedImage capture) {
		Modele_HepaticDyn modele = (Modele_HepaticDyn) vue.getFen_application().getControleur().getModele();
		this.resultats = modele.getResultsHashMap();
		
		this.setLayout(new BorderLayout());

		// capture de la projection
		JPanel grille = new JPanel(new GridLayout(2, 1));
		
		JPanel grilleTop = new JPanel(new GridLayout(1,2));
		JLabel cpt = new JLabel();
		cpt.setIcon(new ImageIcon(capture));
		grilleTop.add(cpt);

		// montage pour une vision globale
		JPanel northEast = new JPanel(new GridLayout(4, 4));
		int nSlice = vue.getImpAnt().getStackSize();
		for (int i = 0; i < 16; i++) {
			int start = (nSlice / 16) * i;
			int stop = start + (nSlice / 16);
			ImagePlus tinyImp = ZProjector.run(vue.getImpAnt(), "sum", start, stop);
			ImageProcessor impc = tinyImp.getProcessor().resize(capture.getWidth() / 4);
			ImagePlus projectionImp = new ImagePlus("", impc);

			BufferedImage projection = projectionImp.getBufferedImage();
			JLabel proj = new JLabel();
			proj.setIcon(new ImageIcon(projection));
			northEast.add(proj);
		}
		grilleTop.add(northEast);
		grille.add(grilleTop);

		JPanel grilleBottom = new JPanel(new BorderLayout());
		
		// bouton capture et label credits
		JButton btn_capture = new JButton("Capture");
		JLabel lbl_credits = new JLabel("Provided by petctviewer.org");
		lbl_credits.setVisible(false);
		vue.getFen_application().getControleur().setCaptureButton(btn_capture, lbl_credits, this, modele, "");
		JPanel btnlbl = new JPanel(new GridLayout(2, 1));
		JPanel flow = new JPanel();
		flow.add(btn_capture);
		btnlbl.add(flow);
		btnlbl.add(lbl_credits);

		// texte de resultat
		Box res = Box.createVerticalBox();
		res.setBorder(new EmptyBorder(0, 15, 0, 15));
		
		res.add(Box.createVerticalStrut(10));
		
		HashMap<String, String> infoPatient = ModeleScin.getPatientInfo(vue.getImp());
		res.add(new JLabel("Patient name: " + infoPatient.get("name")));
		res.add(new JLabel("Patient id: " + infoPatient.get("id")));
		res.add(new JLabel("Aquisition date: " + infoPatient.get("date")));
		
		res.add(Box.createVerticalStrut(15));
		
		res.add(getLabel("T1/2 Righ Liver", Color.BLUE));
		res.add(getLabel("T1/2 Righ Liver *", Color.BLUE));
		res.add(getLabel("Maximum Right Liver", Color.BLUE));
		res.add(getLabel("end/max Ratio Right", Color.BLUE));
		
		res.add(Box.createVerticalStrut(10));
		
		Color c = new Color(47, 122, 30);
		res.add(getLabel("T1/2 Left Liver", c));
		res.add(getLabel("T1/2 Left Liver *", c));
		res.add(getLabel("Maximum Left Liver", c));
		res.add(getLabel("end/max Ratio Left", c));
		
		res.add(Box.createVerticalStrut(10));
		
		res.add(getLabel("Blood pool ratio 20mn/5mn", Color.RED));
		res.add(getLabel("T1/2 Blood pool", Color.RED));
		res.add(getLabel("T1/2 Blood pool *", Color.RED));
		
		res.add(Box.createVerticalStrut(10));
		
		res.add(new JLabel("* fitted values"));
		
		res.add(Box.createVerticalGlue());

		grilleBottom.add(res, BorderLayout.EAST);
		grille.add(grilleBottom);
		
		JPanel titre = new JPanel();
		titre.add(new JLabel("<html><h1> " + vue.getExamType() + " </h1><html>"));
		this.add(titre, BorderLayout.NORTH);
		this.add(grille, BorderLayout.CENTER);
		this.add(btnlbl, BorderLayout.SOUTH);
		
		this.pack();
		
		Component chart = modele.getChartPanel();
		chart.setPreferredSize(new Dimension((capture.getWidth()*2) - res.getWidth(), capture.getHeight()));
		grilleBottom.add(chart, BorderLayout.WEST);

		this.pack();
		
		this.setResizable(false);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}
	
	private JLabel getLabel(String key, Color c) {
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.remove(key));
		lbl_hwb.setForeground(c);
		return lbl_hwb;
	}

}
