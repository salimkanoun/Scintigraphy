package org.petctviewer.scintigraphy.dynamic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;

public class Modele_GeneralDyn extends ModeleScin {

	private HashMap<String, List<Double>> data;
	private Vue_GeneralDyn vue;

	public Modele_GeneralDyn(Vue_GeneralDyn vue) {
		this.data = new HashMap<String, List<Double>>();
		this.vue = vue;
	}

	public int getNbRoi() {
		return this.data.size();
	}

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));

		// on cree la liste si elle n'existe pas
		if (this.data.get(name) == null) {
			this.data.put(name, new ArrayList<Double>());
		}
		this.data.get(name).add(this.getCounts(imp));
	}

	public List<ChartPanel> getChartPanels() {
		for (String k : this.data.keySet()) {
			while (this.data.get(k).size() > vue.getFrameDurations().length) {
				this.data.get(k).remove(0);
			}
		}

		List<ChartPanel> listChart = new ArrayList<ChartPanel>();
		for (String k : this.data.keySet()) {
			List<Double> data = this.data.get(k);
			listChart.add(this.createGraph(data, k));
		}
		return listChart;
	}

	private ChartPanel createGraph(List<Double> l, String nom) {
		XYDataset dataset = createDataset(l, nom);
		JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", "counts/sec", dataset,
				PlotOrientation.VERTICAL, true, true, true);

		ChartPanel chartPanel = new ChartPanel(xylineChart);

		final XYPlot plot = xylineChart.getXYPlot();

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesShapesVisible(0, false);
		plot.setRenderer(renderer);

		return chartPanel;
	}

	private XYDataset createDataset(List<Double> l, String nom) {
		XYSeries points = new XYSeries(nom);

		Double dureePriseOld = 0.0;
		for (int i = 0; i < l.size(); i++) {
			Double dureePrise = vue.getFrameDurations()[i] / 60000.0;
			points.add(dureePriseOld + dureePrise, l.get(i) / (dureePrise * 60));
			dureePriseOld += dureePrise;
		}

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(points);
		return dataset;
	}

	@Override
	public void calculerResultats() {
	}

	@Override
	public HashMap<String, String> getResultsHashMap() {
		return null;
	}

}
