package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

public class TabOptionalCharts extends FenResultatSidePanel {

	private static final long serialVersionUID = -2647720655737610538L;

	public TabOptionalCharts(VueScin vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "");
		JPanel grid = new JPanel(new GridLayout(2, 1));

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
		
		JPanel top = new JPanel(new GridLayout(1,1));
		JPanel bottom = new JPanel(new GridLayout(1,1));
		
		top.add(cp);

		// on recupere les series
		List<XYSeries> listSeries = modele.getSeries();
		// recuperation des chart panel avec association
		String[][] asso = {{"L. Ureter" , "R. Ureter"}, { "Bladder" }, { "Blood Pool" } };

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, listSeries);
		
		if(RenalSettings.getSettings()[0]) {
			top.setLayout(new GridLayout(1, 2));
			top.add(cPanels[1]);
		}
		
		bottom.add(cPanels[2]);
		
		if(RenalSettings.getSettings()[2]) {
			bottom.setLayout(new GridLayout(1, 2));
			bottom.add(cPanels[0]);
		}
		
		this.add(new JPanel(), BorderLayout.WEST);
		
		grid.add(bottom);
		grid.add(top);
		
		this.add(grid, BorderLayout.CENTER);

		this.finishBuildingWindow();
		this.setVisible(false);
	}

	@Override
	public Component[] getSidePanelContent() {
		return null;
	}

}
