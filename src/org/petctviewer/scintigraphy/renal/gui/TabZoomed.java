package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

public class TabZoomed extends FenResultatSidePanel {

	private static final long serialVersionUID = -2647720655737610538L;

	public TabZoomed(VueScin vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "");

		Modele_Renal modele = ((Modele_Renal) vue.getFenApplication().getControleur().getModele());

		XYSeriesCollection dataset = new XYSeriesCollection();

		try {
			XYSeries finalKL = modele.getSerie("Final KL");
			XYSeries finalKLCropped = Modele_Renal.cropSeries(finalKL, 0.0, 1.0);
			finalKLCropped.setKey("Left Kidney");
			dataset.addSeries(finalKLCropped);
		} catch (NullPointerException e) {
		}

		try {
			XYSeries finalKR = modele.getSerie("Final KR");
			XYSeries finalKRCropped = Modele_Renal.cropSeries(finalKR, 0.0, 1.0);
			finalKRCropped.setKey("Right Kidney");
			dataset.addSeries(finalKRCropped);
		} catch (NullPointerException e) {
		}

		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "count/sec", dataset);

		ChartPanel cp = new ChartPanel(chart);
		cp.getChart().getPlot().setBackgroundPaint(null);

		chart.setTitle("First minute of nephrogram");

		this.add(cp, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(w, h));

		this.finishBuildingWindow(true);
		this.setVisible(false);
	}

	@Override
	public Component[] getSidePanelContent() {
		return null;
	}

}
