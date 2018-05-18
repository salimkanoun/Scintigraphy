package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

public class TabROE extends FenResultatSidePanel {

	private VueScin vue;

	public TabROE(VueScin vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "");

		this.vue = vue;

		// on recupere le modele et les series
		Modele_Renal modele = (Modele_Renal) vue.getFen_application().getControleur().getModele();
		List<XYSeries> series = modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Blood pool fitted L", "Final KL", "Output KL" },
				{ "Blood pool fitted R", "Final KR", "Output KR" } };

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, series);

		BasicStroke stroke = new BasicStroke(5.0F);

		// graphique rein droit
		ChartPanel c = cPanels[1];
		FenResultats_Renal.renameSeries(c, "Blood pool fitted R", "Blood Pool");
		FenResultats_Renal.renameSeries(c, "Final KR", "Right Kidney");
		FenResultats_Renal.renameSeries(c, "Output KR", "Output");
		c.getChart().getXYPlot().getRenderer().setDefaultStroke(stroke);
		c.getChart().setTitle("Right Kidney");
		c.getChart().getLegend().setPosition(RectangleEdge.LEFT);
		c.setPreferredSize(new Dimension(w, h / 2));

		// graphique rein gauche
		ChartPanel c1 = cPanels[0];
		FenResultats_Renal.renameSeries(c1, "Output KL", "Output");
		FenResultats_Renal.renameSeries(c1, "Blood pool fitted L", "Blood Pool");
		FenResultats_Renal.renameSeries(c1, "Final KL", "Left Kidney");
		c1.getChart().getXYPlot().getRenderer().setDefaultStroke(stroke);
		c1.getChart().setTitle("Left Kidney");
		c1.getChart().getLegend().setPosition(RectangleEdge.LEFT);
		c1.setPreferredSize(new Dimension(w, h / 2));

		JPanel p = new JPanel(new GridLayout(2, 1));

		p.add(c);
		p.add(c1);

		this.add(new JPanel(), BorderLayout.WEST);
		this.add(p, BorderLayout.CENTER);

		this.finishBuildingWindow();
	}

	private Component getPanelROE() {
		Modele_Renal modele = (Modele_Renal) vue.getFen_application().getControleur().getModele();

		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// on recupere les series
		XYSeries serieRK = modele.getSerie("Output KR");
		XYSeries serieLK = modele.getSerie("Output KL");

		// minutes a observer pour la capacite d'excretion
		double maxX = serieLK.getMaxX();
		int[] mins = new int[10];		
		for(int i = 0; i < mins.length; i++) {
			mins[i] = (int) ((maxX / (mins.length * 1.0)) * i+1);
		}

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(mins.length + 1, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);
		for (int i = 0; i < mins.length; i++) {
			// aligne a droite
			JLabel lbl_min = new JLabel(mins[i] + "  min");
			pnl_roe.add(lbl_min);

			JLabel lbl_g = new JLabel(modele.getPercentage(mins[i], serieLK, "L") + " %");
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_g);

			JLabel lbl_d = new JLabel(modele.getPercentage(mins[i], serieRK, "R") + " %");
			lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_d);

		}

		return pnl_roe;
	}

	@Override
	public Component[] getSidePanelContent() {
		// TODO Auto-generated method stub
		return new Component[] {getPanelROE()};
	}

}
