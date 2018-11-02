package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

class TabZoomed extends JPanel {

	private static final long serialVersionUID = -2647720655737610538L;

	public TabZoomed(Scintigraphy scin) {
		this.setLayout(new BorderLayout());
		SidePanel side = new SidePanel(null, "Renal scintigraphy", scin.getImp());
		side.addCaptureBtn(scin, "_vascular");

		Modele_Renal modele = ((Modele_Renal) scin.getModele());
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		//creation du chartPanel
		boolean[] kidneys = ((Modele_Renal) scin.getModele()).getKidneys();
		if(kidneys[0]) {
			XYSeries finalKL = modele.getSerie("Final KL");
			XYSeries finalKLCropped = Modele_Renal.cropSeries(finalKL, 0.0, 1.0);
			finalKLCropped.setKey("Left Kidney");
			dataset.addSeries(finalKLCropped);
		}
		if(kidneys[1]) {
			XYSeries finalKR = modele.getSerie("Final KR");
			XYSeries finalKRCropped = Modele_Renal.cropSeries(finalKR, 0.0, 1.0);
			finalKRCropped.setKey("Right Kidney");
			dataset.addSeries(finalKRCropped);
		}
		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "count/sec", dataset);
		ChartPanel cp = new ChartPanel(chart);
		cp.getChart().getPlot().setBackgroundPaint(null);
		chart.setTitle("First minute of nephrogram");

		this.add(cp, BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
	}
}
