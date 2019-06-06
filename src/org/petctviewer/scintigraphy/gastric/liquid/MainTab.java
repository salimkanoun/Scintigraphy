package org.petctviewer.scintigraphy.gastric.liquid;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.model.Fit;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import javax.swing.*;
import java.awt.*;

public class MainTab extends TabResult implements ChartMouseListener {

	private boolean displayFit;
	private Fit fit;
	private ChartPanel chartPanel;
	private XYSeriesCollection data;

	private JLabel labelErrors;

	public MainTab(FenResults parent) {
		super(parent, "Liquid Phase");

		this.fit = Fit.createFit(Fit.FitType.LINEAR, getModel().createSeries().toArray(), Unit.COUNTS);

		this.labelErrors = new JLabel();
		this.labelErrors.setForeground(Color.RED);

		this.reloadSidePanelContent();
		this.createGraph();
		this.reloadResultContent();
	}

	private LiquidModel getModel() {
		return (LiquidModel) parent.getModel();
	}

	private void drawFit() {
		// Remove previous fit
		if (data.getSeriesCount() > 1) data.removeSeries(1);

		data.addSeries(this.fit.generateFittedSeries(getModel().generateXValues()));
	}

	private void setErrorMessage(String message) {
		this.labelErrors.setText(message);
	}

	private void reloadFit() {
		try {
			// Create fit
			XYSeries series = ((XYSeriesCollection) ((JValueSetter) this.chartPanel).retrieveValuesInSpan())
					.getSeries(0);
			this.fit = Fit.createFit(Fit.FitType.LINEAR, Library_JFreeChart.invertArray(series.toArray()),
					Unit.COUNTS);

			this.drawFit();
			this.setErrorMessage(null);
			this.reloadSidePanelContent();
		} catch (IllegalArgumentException error) {
			this.setErrorMessage("Cannot fit data: " + error.getMessage());
		}
	}

	private void createGraph() {
		XYSeries series = getModel().createSeries();
		data = new XYSeriesCollection(series);

		final String graphTitle = "Stomach retention - Liquid phase";
		final String xLabel = "Time (" + Unit.MINUTES.abbrev() + ")";
		final String yLabel = "Stomach retention (" + Unit.COUNTS.abbrev() + ")";

		if (this.displayFit) {
			JValueSetter valueSetter = new JValueSetter(ChartFactory
					.createXYLineChart(graphTitle, xLabel, yLabel, data, PlotOrientation.VERTICAL, true, true, true));
			valueSetter.addSelector(new Selector(" ", series.getMinX(), -1, RectangleAnchor.TOP_LEFT), "start");
			valueSetter.addSelector(new Selector(" ", series.getMaxX(), -1, RectangleAnchor.TOP_LEFT), "end");
			valueSetter.addArea("start", "end", "area", null);
			valueSetter.addChartMouseListener(this);
			chartPanel = valueSetter;
		} else {
			chartPanel = Library_JFreeChart
					.createGraph(xLabel, yLabel, new Color[]{Color.BLUE}, graphTitle, data);
		}
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel panCenter = new JPanel(new GridLayout(0, 2));

		// T1/2
		ResultRequest request = new ResultRequest(LiquidModel.RES_T_HALF);
		request.setFit(this.fit);

		ResultValue resTHalf = getModel().getResult(request);
		panCenter.add(new JLabel("T 1/2"));
		panCenter.add(new JLabel(resTHalf.formatValue()));
		panel.add(panCenter, BorderLayout.CENTER);

		if (resTHalf.isExtrapolated()) {
			this.displayFit = true;
			panel.add(new JLabel("(*) The results are calculated with a " + this.fit.getType() + " extrapolation"),
					BorderLayout.SOUTH);
		}

		return panel;
	}

	@Override
	public Container getResultContent() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(this.chartPanel, BorderLayout.CENTER);
		panel.add(this.labelErrors, BorderLayout.SOUTH);
		return panel;
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// Does nothing
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		if (((JValueSetter) this.chartPanel).getGrabbedSelector() != null) {
			this.reloadFit();
		}
	}
}
