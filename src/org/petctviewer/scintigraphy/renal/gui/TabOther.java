package org.petctviewer.scintigraphy.renal.gui;

import ij.Prefs;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabRenal;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class TabOther extends TabResult {

	public TabOther(Scintigraphy scin, FenResults parent) {
		super(parent, "Other", true);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		String[][] asso = new String[][] { { "Blood Pool" }, { "Bladder" } };
		List<XYSeries> series = ((Model_Renal) parent.getModel()).getSeries();
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);

		JPanel center = new JPanel(new GridLayout(1, 1));
		cPanels[0].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.GREEN);
		center.add(cPanels[0]);

		// si la vessie est activee
		if (Prefs.get(PrefTabRenal.PREF_BLADDER, true)) {
			center.setLayout(new GridLayout(2, 1));
			cPanels[1].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.PINK);
			center.add(cPanels[1]);
		}
		return center;
	}

}
