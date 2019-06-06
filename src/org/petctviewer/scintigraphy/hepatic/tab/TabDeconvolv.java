package org.petctviewer.scintigraphy.hepatic.tab;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.YSelector;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab showing the deconvolve of Right liver, using the Blood Pool values.
 *
 */
public class TabDeconvolv {

	private final String title;
	protected final FenResults parent;

	private final JPanel panel;

	private JPanel result;

	private final TabResult tab;
	private int deconvolve;
	private int convolve;

	public TabDeconvolv(FenResults parent, TabResult tab) {

		this.title = "Deconvolv";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.deconvolve = 10;
		this.convolve = 6;

		this.reloadDisplay();

	}

	public JPanel getResultContent() {

		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabCurves) this.tab)
				.getFenApplication().getController().getModel();

		List<Double> bp = modele.getData("Blood Pool AVG");
		List<Double> rliver = modele.getData("Right Liver AVG");
		// double[] kernel = ;
		Double[] kernel = { 1.0d / 4.0d, 2.0d / 4.0d, 1.0d / 4.0d };
		Double[] convolvedBP = Library_Quantif.processNConvolv(bp, kernel, this.convolve);
		Double[] convolvedRL = Library_Quantif.processNConvolv(rliver, kernel, this.convolve);

		@SuppressWarnings("deprecation")
		List<Double> deconv = Library_Quantif.deconvolv(convolvedBP, convolvedRL, this.deconvolve);

		XYSeriesCollection data = new XYSeriesCollection();
		// data.addSeries(modele.createSerie(deconv, "deconv"));
		// data.addSeries(modele.getSerie("Blood Pool AVG"));
		// data.addSeries(modele.getSerie("Right Liver AVG"));
		data.addSeries(modele.createSerie(Arrays.asList(convolvedBP), "Blood Pool convolved"));
		data.addSeries(modele.createSerie(Arrays.asList(convolvedRL), "R. Liver convolved"));

		XYSeriesCollection dataRight = new XYSeriesCollection();
		dataRight.addSeries(modele.createSerie(deconv, "Deconvolve"));

		XYPlot plot = new XYPlot();
		plot.setDataset(0, dataRight);
		plot.setDataset(1, data);

		// customize the plot with renderers and axis
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
		plot.setRenderer(renderer);
		plot.setRangeAxis(0, new NumberAxis("Initial values (count/sec)"));
		plot.setRangeAxis(1, new NumberAxis(this.convolve + " times convolved"));
		plot.setDomainAxis(new NumberAxis("Time (minutes)"));

		// Map the data to the appropriate axis
		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToRangeAxis(1, 1);

		// generate the chart
		JFreeChart chart1 = new JFreeChart("Convolved curves", null, plot, true);
		ChartPanel chartpanel = new ChartPanel(chart1);

		XYSeriesCollection dataset2 = new XYSeriesCollection();
		dataset2.addSeries(modele.getSerie("Blood Pool AVG"));
		dataset2.addSeries(modele.getSerie("Right Liver AVG"));

		XYPlot plotRight = new XYPlot();
		plotRight.setDataset(0, dataRight);
		plotRight.setDataset(1, dataset2);

		// customize the plot with renderers and axis
		XYLineAndShapeRenderer rendererRight = new XYLineAndShapeRenderer(true, false);
		plotRight.setRenderer(rendererRight);
		plotRight.setRangeAxis(0, new NumberAxis("Initial values (count/sec)"));
		plotRight.setRangeAxis(1, new NumberAxis(this.convolve + " times convolved"));
		plotRight.setDomainAxis(new NumberAxis("Time (minutes)"));

		// Map the data to the appropriate axis
		plotRight.mapDatasetToRangeAxis(0, 0);
		plotRight.mapDatasetToRangeAxis(1, 1);

		// generate the chart
		JFreeChart chart2 = new JFreeChart("Not convolved curves", null, plotRight, true);
		ChartPanel chartpanelRight = new ChartPanel(chart2);

		JPanel grid = new JPanel(new GridLayout(2, 1));
		grid.add(prepareValueSetter(chartpanel));
		grid.add(prepareValueSetter(chartpanelRight));

//		grid.setPreferredSize(new Dimension(1000, 650));

		return grid;

		// chartpanel.setPreferredSize(new Dimension(1000, 650));
		// return chartpanel;
	}

	public String getTitle() {
		return this.title;
	}

	public JPanel getPanel() {
		return this.panel;
	}

	public void reloadDisplay() {
		this.reloadResultContent();
	}

	public void reloadResultContent() {
		if (this.result != null)
			this.panel.remove(this.result);
		this.result = this.getResultContent() == null ? new JPanel() : this.getResultContent();
		this.panel.add(this.result, BorderLayout.CENTER);
		this.parent.repaint();
		this.parent.revalidate();
		this.parent.pack();
	}

	public void setDeconvolvFactor(int deconvolv) {
		this.deconvolve = deconvolv;
	}

	public void setConvolvFactor(int convolv) {
		this.convolve = convolv;
	}

	public List<Double> multiplyAllValuesby(List<Double> values, Double value) {

		List<Double> finalValues = new ArrayList<>();

		for (Double doubles : values)
			finalValues.add(doubles * value);

		return finalValues;
	}

	public List<Double> normalizeToOne(List<Double> values) {

		Double maxValue = 0.0d;
		for (Double doubles : values)
			if (doubles > maxValue)
				maxValue = doubles;

		return multiplyAllValuesby(values, 1.0d / maxValue);
	}

	public List<Double> normalizeToOne(Double[] values) {
		return this.normalizeToOne(Arrays.asList(values));
	}
	
	private JValueSetter prepareValueSetter(ChartPanel chart) {
		chart.getChart().getPlot().setBackgroundPaint(null);
		JValueSetter jvs = new JValueSetter(chart.getChart());
		
		YSelector test = new YSelector("TestYSelector", 0.0d, 0,
				RectangleAnchor.TOP_LEFT);
		jvs.addSelector(test, "TestYSelector");
		
		jvs.revalidate();
		jvs.repaint();
		
		return jvs;
	}

}
