package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dyn.Modele_HepaticDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public class TabVasculaire extends TabResult {

	public TabVasculaire(Scintigraphy scin, int width, int height, FenResults parent) {
		super(parent, "Vascular");

		this.getPanel().setPreferredSize(new Dimension(width, height));

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		Modele_HepaticDyn modele = (Modele_HepaticDyn) this.parent.getModel();
		List<XYSeries> series = modele.getSeries();
		ChartPanel chartVasculaire = Library_JFreeChart.associateSeries(new String[] { "Blood pool" }, series);

		return chartVasculaire;
	}

}