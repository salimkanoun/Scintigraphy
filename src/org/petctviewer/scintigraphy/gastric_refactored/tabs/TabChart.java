package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabChart extends TabResult {

	private XYSeriesCollection data;
	private JValueSetter valueSetter;

	private JComboBox<FitType> fitsChoices;
	private JLabel labelInterpolation;
	private JPanel panResult;

	public TabChart(FenResults parent) {
		super(parent, "Stomach retention");

		// Instantiate components
		fitsChoices = new JComboBox<>();

		this.labelInterpolation = new JLabel();
		this.labelInterpolation.setVisible(false);

		this.createChart();

		Component[] hide = new Component[] { this.fitsChoices };
		Component[] show = new Component[] { this.labelInterpolation };
		this.createCaptureButton(hide, show, null);
		this.reloadDisplay();
	}

	/**
	 * Creates the chart along with all series.
	 */
	private void createChart() {
		// Create chart
		this.data = new XYSeriesCollection();
		XYSeries stomachSeries = ((Model_Gastric) this.parent.getModel()).getStomachSeries();
		this.data.addSeries(stomachSeries);

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (min)", "Stomach retention (%)",
				data, PlotOrientation.VERTICAL, true, true, true);

		// Create value setter
		double startX = stomachSeries.getMinX() + .1 * (stomachSeries.getMaxX() - stomachSeries.getMinX());
		double endX = stomachSeries.getMinX() + .7 * (stomachSeries.getMaxX() - stomachSeries.getMinX());
		valueSetter = new JValueSetter(chart);
		valueSetter.addSelector(new Selector(" ", startX, -1, RectangleAnchor.TOP_LEFT), "start");
		valueSetter.addSelector(new Selector(" ", endX, -1, RectangleAnchor.TOP_LEFT), "end");
		valueSetter.addArea("start", "end", "area", null);
	}

	/**
	 * Removes all previous fits (annotations included).
	 */
	private void clearFits() {
		for (int i = 1; i < this.data.getSeriesCount(); i++)
			this.data.removeSeries(i);
	}

	/**
	 * Displays the fit and removes the previous fit.
	 * 
	 * @param fit Type of regression to fit the values of the chart
	 */
	public void drawSeries(XYSeries series) {
		this.clearFits();

		this.data.addSeries(series);
	}

	/**
	 * Changes the text of the extrapolation name (used for capture).
	 * 
	 * @param labelName New name of the extrapolation
	 */
	public void changeLabelInterpolation(String labelName) {
		// Change label interpolation text (for capture)
		this.labelInterpolation.setText("-- " + labelName + " --");
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel();

		// Instantiate combo box
		// Remove polynomial fit if not enough data
		FitType[] possibleFits = FitType.values();

		for (FitType type : possibleFits)
			this.fitsChoices.addItem(type);

		fitsChoices.addActionListener(this.parent.getController());
		panel.add(fitsChoices);

		panel.add(this.labelInterpolation);

		return panel;
	}

	@Override
	public JPanel getResultContent() {
		this.panResult = new JPanel();

		this.panResult.add(this.valueSetter);
		return this.panResult;
	}

}