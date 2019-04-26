package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Component;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

class TabZoomed extends TabResult {

	public TabZoomed(Scintigraphy scin, FenResults parent) {
		super(parent, "Vascular phase", true);
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		Modele_Renal modele = ((Modele_Renal) parent.getModel());
		XYSeriesCollection dataset = new XYSeriesCollection();
		boolean[] kidneys = modele.getKidneys();
		if (kidneys[0]) {
			XYSeries finalKL = modele.getSerie("Final KL");
			XYSeries finalKLCropped = Library_JFreeChart.cropSeries(finalKL, 0.0, 1.0);
			finalKLCropped.setKey("Left Kidney");
			dataset.addSeries(finalKLCropped);
		}
		if (kidneys[1]) {
			XYSeries finalKR = modele.getSerie("Final KR");
			XYSeries finalKRCropped = Library_JFreeChart.cropSeries(finalKR, 0.0, 1.0);
			finalKRCropped.setKey("Right Kidney");
			dataset.addSeries(finalKRCropped);
		}
		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "count/sec", dataset);
		ChartPanel cp = new ChartPanel(chart);
		cp.getChart().getPlot().setBackgroundPaint(null);
		chart.setTitle("First minute of nephrogram");
		return cp;
	}
}