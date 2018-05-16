package org.petctviewer.scintigraphy.renal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenOptionalCharts extends FenResultatSidePanel {

	private static final long serialVersionUID = -2647720655737610538L;

	public FenOptionalCharts(VueScin vue) {
		super("Renal scintigraphy", vue, null, "");
		JPanel grid = new JPanel(new GridLayout(2, 2));

		Modele_Renal modele = ((Modele_Renal) vue.getFen_application().getControleur().getModele());

		XYSeries finalKL = modele.getSerie("Final KL");
		XYSeries finalKR = modele.getSerie("Final KR");

		XYSeries finalKLCropped = Modele_Renal.cropSeries(finalKL, 0.0, 1.0);
		XYSeries finalKRCropped = Modele_Renal.cropSeries(finalKR, 0.0, 1.0);
		finalKLCropped.setKey("Left Kidney");
		finalKRCropped.setKey("Right Kidney");
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(finalKRCropped);
		dataset.addSeries(finalKLCropped);
		
		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "count/sec", dataset);
		
		ChartPanel cp = new ChartPanel(chart);
		cp.getChart().getPlot().setBackgroundPaint(null);
		cp.setPreferredSize(new Dimension(300, 300));
		
		grid.add(cp);

		// on recupere les series
		List<XYSeries> listSeries = modele.getSeries();
		// recuperation des chart panel avec association
		String[][] asso = { { "Bladder" }, { "Blood Pool" }, {"L. Ureter", "R. Ureter"} };

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, listSeries);

		for (ChartPanel c : cPanels) {
			grid.add(c);
			c.setPreferredSize(new Dimension(300, 300));
		}

		this.add(grid, BorderLayout.WEST);

		this.finishBuildingWindow();
	}

	@Override
	public Component[] getSidePanelContent() {
		return null;
	}

}
