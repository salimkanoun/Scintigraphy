package org.petctviewer.scintigraphy.platelet_refactored.tabs;

import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.platelet_refactored.ModelPlatelet;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;

public class GraphsTab extends TabResult {

	private boolean geoAvg;

	public GraphsTab(FenResults parent, boolean geoAvg) {
		super(parent, "More Graphs");
		this.geoAvg = geoAvg;
		this.reloadResultContent();
	}

	private ModelPlatelet getModel() {
		return (ModelPlatelet) this.parent.getModel();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public Container getResultContent() {
		JPanel panel = new JPanel(new GridLayout(0, 1));

		XYSeriesCollection datasetRatio = new XYSeriesCollection();
		datasetRatio.addSeries(getModel().seriesSpleenRatio(geoAvg));
		datasetRatio.addSeries(getModel().seriesLiverRatio(geoAvg));

		panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.BLUE},
												 "Ratio from J0", datasetRatio));

		return panel;
	}
}
