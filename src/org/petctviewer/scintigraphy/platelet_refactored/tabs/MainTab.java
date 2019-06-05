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
	private boolean geoAvg;

	public MainTab(FenResults parent, ImagePlus capture, boolean geoAvg) {
		super(parent, "Results");
		this.capture = capture;
		this.geoAvg = geoAvg;
		this.reloadDisplay();
	}

	private ModelPlatelet getModel() {
		return (ModelPlatelet) parent.getModel();
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Corrected with " + getModel().getIsotope()));
		return panel;
	}

	@Override
	public Container getResultContent() {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		// Posterior
		XYSeriesCollection datasetPosterior = new XYSeriesCollection();
		datasetPosterior.addSeries(getModel().seriesSpleenHeart());
		datasetPosterior.addSeries(getModel().seriesLiverHeart());
		datasetPosterior.addSeries(getModel().seriesSpleenLiver());

		panel.add(new DynamicImage(capture.getBufferedImage()));
		panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.GREEN, Color.BLUE},
												 "Posterior", datasetPosterior));

		// Corrected Spleen Posterior
		XYSeriesCollection datasetJORatio = new XYSeriesCollection(getModel().seriesSpleenPost());
		panel.add(
				Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED}, "Corrected Spleen Posterior",
											   datasetJORatio));

		// Geometrical means
		if (this.geoAvg) {
			XYSeriesCollection datasetGM = new XYSeriesCollection();
			datasetGM.addSeries(getModel().seriesGMSpleenHeart());
			datasetGM.addSeries(getModel().seriesGMLiverHeart());
			datasetGM.addSeries(getModel().seriesGMSpleenLiver());

			panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.GREEN,
															 Color.BLUE},
													 "Geometrical Mean", datasetGM));
		}

		return panel;
	}
}
