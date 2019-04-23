package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

class TabCort extends TabResult {

	public TabCort(Scintigraphy scin, FenResults parent) {
		super(parent, "Corticals/Pelvis");
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		ModeleScinDyn modele = (ModeleScinDyn) parent.getModel();

		List<XYSeries> listSeries = modele.getSeries();
		// recuperation des chart panel avec association
		String[][] asso = { { "L. Cortical", "L. Pelvis" }, { "R. Cortical", "R. Pelvis" } };
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, listSeries);

		cPanels[0].getChart().setTitle("Left kidney");
		cPanels[1].getChart().setTitle("Right kidney");
		// on change la couleur des courbes
		cPanels[0].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.RED);
		cPanels[0].getChart().getXYPlot().getRenderer().setSeriesPaint(1, Color.RED);
		cPanels[1].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
		cPanels[1].getChart().getXYPlot().getRenderer().setSeriesPaint(1, Color.BLUE);

		JPanel grid = new JPanel(new GridLayout(2, 1));
		grid.add(cPanels[0]);

		grid.add(cPanels[1]);
		return grid;
	}

}
