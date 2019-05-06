package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public class TabVasculaire extends TabResult {

	public TabVasculaire(FenResults parent) {
		super(parent, "Vascular");

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		ModelHepaticDynamic modele = (ModelHepaticDynamic) this.parent.getModel();
		List<XYSeries> series = modele.getSeries();
		ChartPanel chartVasculaire = Library_JFreeChart.associateSeries(new String[] { "Blood pool" }, series);

		return chartVasculaire;
	}

}
