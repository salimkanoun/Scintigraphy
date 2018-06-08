package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dyn.Modele_HepaticDyn;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

import ij.ImagePlus;

class TabPrincipal extends FenResultatSidePanel{

	private HashMap<String, String> resultats;

	public TabPrincipal(VueScinDyn vue, BufferedImage capture, int width, int height) {
		super("Biliary scintiraphy", vue, null, "");

		Modele_HepaticDyn modele = (Modele_HepaticDyn) vue.getFenApplication().getControleur().getModele();
		this.resultats = modele.getResultsHashMap();
		
		//montage sur l'ensemble des images
		ImagePlus imp = VueScin.creerMontage(vue.getFrameDurations(), vue.getImpAnt(), capture.getWidth() / 4, 4, 4);
		BufferedImage montage = imp.getBufferedImage();
		
		//panel qui sera plae au centre de la fenetre
		JPanel center = new JPanel(new GridLayout(2, 1));
		
		//panel du haut, contenant les eux images
		JPanel pnl_top = new JPanel(new GridLayout(1,2));
		pnl_top.add(new DynamicImage(capture));
		pnl_top.add(new DynamicImage(montage));
		center.add(pnl_top);
		
		List<XYSeries> series = modele.getSeries();
		ChartPanel cp = ModeleScinDyn.associateSeries(new String[] {"R. Liver","L. Liver"}, series);
		center.add(cp);
		
		this.add(center, BorderLayout.CENTER);
		
		this.finishBuildingWindow(true);
		this.setPreferredSize(new Dimension(width, height));
	}
	
	@Override
	public Component getSidePanelContent() {
		JPanel flow = new JPanel();
		flow.add(getBoxResultats());
		return flow;
	}
	
	private Box getBoxResultats() {		
		// texte de resultat
		Box res = Box.createVerticalBox();
		res.setBorder(new EmptyBorder(0, 15, 0, 15));

		res.add(Box.createVerticalStrut(15));

		Color c = Color.RED;
		res.add(getLabel("T1/2 Righ Liver", c));
		res.add(getLabel("Maximum Right Liver", c));
		res.add(getLabel("end/max Ratio Right", c));

		res.add(Box.createVerticalStrut(10));

		c = Color.BLUE;
		res.add(getLabel("T1/2 Left Liver", c));
		res.add(getLabel("Maximum Left Liver", c));
		res.add(getLabel("end/max Ratio Left", c));

		res.add(Box.createVerticalGlue());

		return res;
	}

	private JLabel getLabel(String key, Color c) {
		JLabel lbl_hwb = new JLabel(key + " : " + this.resultats.remove(key));
		lbl_hwb.setForeground(c);
		return lbl_hwb;
	}

}
