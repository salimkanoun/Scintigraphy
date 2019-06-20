package org.petctviewer.scintigraphy.renal.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class TabUreter extends TabResult {

	public TabUreter(FenResults parent) {
		super(parent, "Ureters", true);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		String[][] asso = new String[][] { { "L. Ureter", "R. Ureter" } };
		List<XYSeries> series = ((Model_Renal) parent.getModel()).getSeries();
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);

		cPanels[0].getChart().setTitle("Ureters");
		return cPanels[0];
	}
}
