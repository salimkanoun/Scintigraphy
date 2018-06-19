package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

class TabROE extends FenResultatSidePanel implements ActionListener{

	private static final long serialVersionUID = -8303889633428224794L;

	public TabROE(Scintigraphy vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "roe");

		// on recupere le modele et les series
		Modele_Renal modele = (Modele_Renal) vue.getFenApplication().getControleur().getModele();
		List<XYSeries> series = modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Blood pool fitted L", "Final KL", "Output KL" },
				{ "Blood pool fitted R", "Final KR", "Output KR" } };

		ChartPanel[] cPanels = ModeleScinDyn.associateSeries(asso, series);

		BasicStroke stroke = new BasicStroke(5.0F);
		JPanel p = new JPanel(new GridLayout(2, 1));

		if (modele.getKidneys()[1]) {
			// graphique rein droit
			ChartPanel c = cPanels[1];
			FenResultats_Renal.renameSeries(c, "Blood pool fitted R", "Blood Pool");
			FenResultats_Renal.renameSeries(c, "Final KR", "Right Kidney");
			FenResultats_Renal.renameSeries(c, "Output KR", "Output");
			

			XYItemRenderer ren = c.getChart().getXYPlot().getRenderer();
			ren.setDefaultStroke(stroke);
			ren.setSeriesPaint(0, Color.GREEN);
			ren.setSeriesPaint(1, Color.BLUE);
			ren.setSeriesPaint(2, Color.MAGENTA);
			
			c.getChart().setTitle("Right Kidney");
			c.getChart().getLegend().setPosition(RectangleEdge.LEFT);
			c.setPreferredSize(new Dimension(w, h / 2));
			p.add(c);
		}

		if (modele.getKidneys()[0]) {
			// graphique rein gauche
			ChartPanel c1 = cPanels[0];
			
			XYItemRenderer ren = c1.getChart().getXYPlot().getRenderer();

			ren.setDefaultStroke(stroke);
			ren.setSeriesPaint(0, Color.GREEN);
			ren.setSeriesPaint(1, Color.RED);
			ren.setSeriesPaint(2, Color.MAGENTA);
			
			FenResultats_Renal.renameSeries(c1, "Output KL", "Output");
			FenResultats_Renal.renameSeries(c1, "Blood pool fitted L", "Blood Pool");
			FenResultats_Renal.renameSeries(c1, "Final KL", "Left Kidney");
			
			c1.getChart().setTitle("Left Kidney");
			c1.getChart().getLegend().setPosition(RectangleEdge.LEFT);
			c1.setPreferredSize(new Dimension(w, h / 2));
			p.add(c1);
		}

		this.add(new JPanel(), BorderLayout.WEST);
		this.add(p, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(w, h));

		this.finishBuildingWindow(true);
	}

	private Component getPanelROE() {
		Modele_Renal modele = (Modele_Renal) getScin().getFenApplication().getControleur().getModele();

		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		Double[] mins = new Double[10];
		for (int i = 0; i < mins.length; i++) {
			mins[i] = ModeleScin.round((modele.getSerie("Blood Pool").getMaxX() / (mins.length * 1.0)) * i + 1, 1);
		}

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(mins.length + 1, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);

		XYSeries serieLK = null;
		// on recupere les series
		if (modele.getKidneys()[0]) {
			serieLK = modele.getSerie("Output KL");
			// minutes a observer pour la capacite d'excretion
		}

		XYSeries serieRK = null;
		if (modele.getKidneys()[1]) {
			serieRK = modele.getSerie("Output KR");
		}

		for (int i = 0; i < mins.length; i++) {
			// aligne a droite
			JLabel lbl_min = new JLabel(mins[i] + "  min");
			pnl_roe.add(lbl_min);

			if (modele.getKidneys()[0]) {
				JLabel lbl_g = new JLabel(modele.getPercentage(mins[i], serieLK, "L") + " %");
				lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
				pnl_roe.add(lbl_g);
			} else {
				pnl_roe.add(new JLabel("N/A"));
			}

			if (modele.getKidneys()[1]) {
				JLabel lbl_d = new JLabel(modele.getPercentage(mins[i], serieRK, "R") + " %");
				lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
				pnl_roe.add(lbl_d);
			} else {
				pnl_roe.add(new JLabel("N/A"));
			}

		}

		return pnl_roe;
	}

	@Override
	public Component getSidePanelContent() {
		Box box = Box.createVerticalBox();
		box.add(getPanelROE());
		box.add(Box.createVerticalStrut(30));
		JButton btn_bpi = new JButton("Check fit");
		btn_bpi.addActionListener(this);
		JPanel pnl_bpi = new JPanel();
		pnl_bpi.add(btn_bpi);
		box.add(pnl_bpi);
		return box;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Modele_Renal modele = (Modele_Renal) this.getScin().getFenApplication().getControleur().getModele();
		JFrame frameBPI = new JFrame();
		frameBPI.add(ModeleScinDyn.associateSeries(new String[] {"BPI", "Blood pool fitted"}, modele.getSeries()));
		frameBPI.pack();
		frameBPI.setVisible(true);
	}

}
