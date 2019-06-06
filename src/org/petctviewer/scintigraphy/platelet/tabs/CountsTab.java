package org.petctviewer.scintigraphy.platelet.tabs;

import ij.ImagePlus;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.platelet.ModelPlatelet;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CountsTab extends TabResult {

	private List<ImagePlus> capture;
	private boolean geoAvg;

	public CountsTab(FenResults parent, List<ImagePlus> capture, boolean geoAvg) {
		super(parent, "Results Counts");
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
		JPanel panel = new JPanel(new GridLayout(0, 2));

		// Capture
		if (capture.size() > 0) {
			panel.add(new DynamicImage(capture.get(0).getBufferedImage()));
			if (capture.size() > 1) {
				panel.add(new DynamicImage(capture.get(1).getBufferedImage()));
			} else panel.add(Library_Gui.generateBlank());
		} else panel.add(Library_Gui.generateBlank());


		// Posterior
		XYSeriesCollection datasetPosterior = new XYSeriesCollection();
		datasetPosterior.addSeries(getModel().seriesSpleenHeart(false));
		datasetPosterior.addSeries(getModel().seriesLiverHeart(false));
		datasetPosterior.addSeries(getModel().seriesSpleenLiver(false));
		panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.GREEN, Color.BLUE},
												 "Posterior", datasetPosterior));

		// Geometrical means
		if (this.geoAvg) {
			XYSeriesCollection datasetGM = new XYSeriesCollection();
			datasetGM.addSeries(getModel().seriesGMSpleenHeart(false));
			datasetGM.addSeries(getModel().seriesGMLiverHeart(false));
			datasetGM.addSeries(getModel().seriesGMSpleenLiver(false));
			panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.GREEN,
															 Color.BLUE},
													 "Geometrical Mean", datasetGM));
		} else {
			panel.add(Library_Gui.generateBlank());
		}

		// Corrected Spleen Posterior
		XYSeriesCollection datasetJORatio = new XYSeriesCollection(getModel().seriesSpleenPost(false));
		panel.add(
				Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED}, "Corrected Spleen Posterior",
											   datasetJORatio));

		// Ratio from J0
		XYSeriesCollection datasetRatio = new XYSeriesCollection();
		datasetRatio.addSeries(getModel().seriesSpleenRatio(geoAvg, false));
		datasetRatio.addSeries(getModel().seriesLiverRatio(geoAvg, false));
		panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.BLUE},
												 "Ratio from " + "J0", datasetRatio));

		return panel;
	}
}
