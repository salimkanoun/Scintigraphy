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

public abstract class Modele_Dynamic extends ModeleScin {

	private HashMap<String, List<Double>> data;
	private int[] frameDuration;

	public Modele_Dynamic(int[] frameDuration) {
		this.data = new HashMap<String, List<Double>>();
		this.frameDuration = frameDuration;
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

	public List<XYSeries> getSeries() {
		List<XYSeries> listSeries = new ArrayList<XYSeries>();
		for (String k : this.data.keySet()) {
			List<Double> data = this.data.get(k);
			listSeries.add(createSerie(data, k));
		}
		return listSeries;
	}

	private XYSeries createSerie(List<Double> l, String nom) {
		XYSeries points = new XYSeries(nom);

		Double dureePriseOld = 0.0;
		for (int i = 0; i < l.size(); i++) {
			Double dureePrise = frameDuration[i] / 60000.0;
			points.add(dureePriseOld + dureePrise, l.get(i) / (dureePrise * 60));
			dureePriseOld += dureePrise;
		}

		return points;
	}

	@Override
	public String toString() {
		String s = "\n";
		for (String k : data.keySet()) {
			s += k;
			for (Double d : data.get(k)) {
				s += "," + d;
			}
			s += "\n";
		}
		return s;
	}

	public HashMap<String, List<Double>> getData() {
		return data;
	}
	
	public List<Double> getData(String key) {
		return data.get(key);
	}

	public int[] getFrameDuration() {
		return frameDuration;
	}

}
