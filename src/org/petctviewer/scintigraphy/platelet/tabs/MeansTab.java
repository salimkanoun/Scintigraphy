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

public class MeansTab extends TabResult {

	private final List<ImagePlus> capture;
	private final boolean geoAvg;

	public MeansTab(FenResults parent, List<ImagePlus> capture, boolean geoAvg) {
		super(parent, "Results Means");
		this.capture = capture;
		this.geoAvg = geoAvg;
		this.reloadDisplay();
	}

	private ModelPlatelet getModel() {
		return (ModelPlatelet) this.parent.getModel();
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
		if (this.capture.size() > 0) {
			panel.add(new DynamicImage(capture.get(0).getBufferedImage()));
			if (this.capture.size() > 1) panel.add(new DynamicImage(capture.get(1).getBufferedImage()));
			else panel.add(Library_Gui.generateBlank());
		} else {
			panel.add(Library_Gui.generateBlank());
		}

		// Posterior
		XYSeriesCollection datasetPosterior = new XYSeriesCollection();
		datasetPosterior.addSeries(getModel().seriesSpleenHeart(true));
		datasetPosterior.addSeries(getModel().seriesLiverHeart(true));
		datasetPosterior.addSeries(getModel().seriesSpleenLiver(true));
		panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.GREEN, Color.BLUE},
												 "Posterior", datasetPosterior));

		// Geometrical means
		if (this.geoAvg) {
			XYSeriesCollection datasetGM = new XYSeriesCollection();
			datasetGM.addSeries(getModel().seriesGMSpleenHeart(true));
			datasetGM.addSeries(getModel().seriesGMLiverHeart(true));
			datasetGM.addSeries(getModel().seriesGMSpleenLiver(true));
			panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.GREEN,
															 Color.BLUE},
													 "Geometrical Mean", datasetGM));
		} else {
			panel.add(Library_Gui.generateBlank());
		}

		// Corrected Spleen Posterior
		XYSeriesCollection datasetJORatio = new XYSeriesCollection(getModel().seriesSpleenPost(true));
		panel.add(
				Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED}, "Corrected Spleen Posterior",
											   datasetJORatio));

		// Ratio from J0
		XYSeriesCollection datasetRatio = new XYSeriesCollection();
		datasetRatio.addSeries(getModel().seriesSpleenRatio(geoAvg, true));
		datasetRatio.addSeries(getModel().seriesLiverRatio(geoAvg, true));
		panel.add(Library_JFreeChart.createGraph("Hours", "Values", new Color[]{Color.RED, Color.BLUE},
												 "Ratio from " + "J0", datasetRatio));

		return panel;
	}
}
