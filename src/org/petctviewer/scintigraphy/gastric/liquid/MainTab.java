package org.petctviewer.scintigraphy.gastric.liquid;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import java.awt.*;
import java.util.Arrays;

public class MainTab extends TabResult {

	public MainTab(FenResults parent) {
		super(parent, "Liquid Phase");
		this.reloadDisplay();
	}

	private LiquidModel getModel() {
		return (LiquidModel) parent.getModel();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public Container getResultContent() {
		XYSeries series = getModel().createSeries();
		for(double[] v : series.toArray())
			System.out.println(Arrays.toString(v));
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		return Library_JFreeChart.createGraph("Stomach retention (liquid)", new Color[]{Color.BLUE}, "", dataset,
				series.getMaxY() * 1.1);
	}
}
