package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.Component;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.ControllerWorkflow_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.NoFit;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabDefaultMethod extends TabResult {
	
	private XYSeriesCollection data;
	private JValueSetter valueSetter;

	public TabDefaultMethod(FenResults parent) {
		super(parent, "Default method", true);

		this.createChart();

		this.reloadDisplay();
	}
	
	private Model_Gastric getModel() {
		return (Model_Gastric) this.parent.getModel();
	}

	public void createChart() {
		// Create chart
		this.data = new XYSeriesCollection();
		XYSeries stomachSeries = getModel().getStomachSeriesDefaultMethod();
		this.data.addSeries(stomachSeries);

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (min)", "Stomach retention (counts)",
				data, PlotOrientation.VERTICAL, true, true, true);

		// Set bounds
		XYPlot plot = chart.getXYPlot();
		// X axis
		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setRange(-10., stomachSeries.getMaxX() + 10.);
		// Y axis
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setRange(-10., stomachSeries.getMaxY() + 10.);

		// Create value setter
		double startX = stomachSeries.getMinX() + .1 * (stomachSeries.getMaxX() - stomachSeries.getMinX());
		double endX = stomachSeries.getMinX() + .7 * (stomachSeries.getMaxX() - stomachSeries.getMinX());
		valueSetter = new JValueSetter(chart);
		valueSetter.addSelector(new Selector(" ", startX, -1, RectangleAnchor.TOP_LEFT), "start");
		valueSetter.addSelector(new Selector(" ", endX, -1, RectangleAnchor.TOP_LEFT), "end");
		valueSetter.addArea("start", "end", "area", null);
		valueSetter.addChartMouseListener((ControllerWorkflow_Gastric) this.parent.getController());

		// By default, no extrapolation
		getModel().setExtrapolation(new NoFit());
//		this.drawFit(getModel().getFittedSeries());
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		JPanel panResult = new JPanel();

		panResult.add(this.valueSetter);
		return panResult;
	}

}
