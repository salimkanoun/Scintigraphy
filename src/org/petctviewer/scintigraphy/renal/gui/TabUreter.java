package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

class TabUreter extends TabResult {

	public TabUreter(Scintigraphy scin, FenResults parent) {
		super(parent, "Ureters", true);
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		String[][] asso = new String[][] { { "L. Ureter", "R. Ureter" } };
		List<XYSeries> series = ((Modele_Renal) parent.getModel()).getSeries();
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);

		cPanels[0].getChart().setTitle("Ureters");
		return cPanels[0];
	}
}
