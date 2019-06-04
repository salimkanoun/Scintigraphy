package org.petctviewer.scintigraphy.platelet_refactored.tabs;

import ij.ImagePlus;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.platelet_refactored.ModelPlatelet;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;

public class MainTab extends TabResult {

	private ImagePlus capture;

	public MainTab(FenResults parent, ImagePlus capture) {
		super(parent, "Results");
		this.capture = capture;
	}

	private ModelPlatelet getModel() {
		return (ModelPlatelet) parent.getModel();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public Container getResultContent() {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		XYSeriesCollection datasetPosterior = new XYSeriesCollection();
		datasetPosterior.addSeries(getModel().seriesSpleenHeart());
		datasetPosterior.addSeries(getModel().seriesLiverHeart());
		datasetPosterior.addSeries(getModel().seriesSpleenLiver());

		XYSeriesCollection datasetJORatio = new XYSeriesCollection(getModel().seriesSpleenPost());

		XYSeriesCollection datasetGM = new XYSeriesCollection();
		datasetGM.addSeries(getModel().seriesGMSpleenHeart());
		datasetGM.addSeries(getModel().seriesGMLiverHeart());
		datasetGM.addSeries(getModel().seriesGMSpleenLiver());

		panel.add(new DynamicImage(capture.getBufferedImage()));
		panel.add(Library_JFreeChart.createGraph("Hours", "Counts", new Color[]{Color.RED, Color.GREEN, Color.BLUE},
												 "Posterior", datasetPosterior));
		panel.add(Library_JFreeChart.createGraph("Hours", "Counts", new Color[]{Color.RED}, "JO Ratio",
												 datasetJORatio));
		panel.add(Library_JFreeChart.createGraph("Hours", "Counts", new Color[]{Color.RED, Color.GREEN, Color.BLUE},
												 "Geometrical Mean", datasetGM));

		return panel;
	}
}
