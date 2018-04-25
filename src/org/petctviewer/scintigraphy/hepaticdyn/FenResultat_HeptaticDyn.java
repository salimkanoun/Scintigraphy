package org.petctviewer.scintigraphy.hepaticdyn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.petctviewer.scintigraphy.scin.ControleurScin;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public class FenResultat_HeptaticDyn extends JFrame {

	private static final long serialVersionUID = 1094251580157650693L;
	
	private HashMap<String, String> resultats;

	public FenResultat_HeptaticDyn(Vue_HepaticDyn vue, BufferedImage capture) {
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
		ControleurScin.setCaptureButton(btn_capture, lbl_credits, vue, this);
		JPanel btnlbl = new JPanel(new GridLayout(2, 1));
		JPanel flow = new JPanel();
		flow.add(btn_capture);
		btnlbl.add(flow);
		btnlbl.add(lbl_credits);

		// texte de resultat
		JPanel res = new JPanel(new GridLayout(10, 1, 10, 10));
		res.add(getLabel("T1/2 Righ Liver", Color.BLUE));
		res.add(getLabel("Maximum Right Liver", Color.BLUE));
		res.add(getLabel("END/MAX Ratio Right", Color.BLUE));
		
		res.add(new JLabel(""));
		
		Color c = new Color(47, 122, 30);
		res.add(getLabel("T1/2 Left Liver", c));
		res.add(getLabel("Maximum Left Liver", c));
		res.add(getLabel("END/MAX Ratio Left", c));
		
		res.add(new JLabel(""));
		
		res.add(getLabel("Blood pool ratio 20mn/5mn", Color.RED));
		res.add(getLabel("T1/2 Blood pool", Color.RED));
		
		JPanel flow2 = new JPanel();
		flow2.add(res);
		grilleBottom.add(flow2, BorderLayout.EAST);
		grille.add(grilleBottom);
		
		this.add(grille, BorderLayout.CENTER);
		this.add(btnlbl, BorderLayout.SOUTH);
		
		this.pack();
		
		Component chart = modele.getChartPanel();
		chart.setPreferredSize(new Dimension((capture.getWidth()*2) - flow2.getWidth(), capture.getHeight()));
		grilleBottom.add(chart, BorderLayout.WEST);

		this.pack();
		
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}
	
	private JLabel getLabel(String key, Color c) {
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.remove(key));
		lbl_hwb.setForeground(c);
		return lbl_hwb;
	}

}
