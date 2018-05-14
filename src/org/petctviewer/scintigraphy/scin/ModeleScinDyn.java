package org.petctviewer.scintigraphy.scin;

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
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ij.ImagePlus;

public abstract class ModeleScinDyn extends ModeleScin {

	private HashMap<String, List<Double>> data;
	public static int[] FRAMEDURATION;

	private boolean lock = false;

	public ModeleScinDyn(int[] frameDuration) {
		this.data = new HashMap<String, List<Double>>();
		ModeleScinDyn.FRAMEDURATION = frameDuration;
	}

	public int getNbRoi() {
		return this.data.size();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		if (!lock) {
			String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));

			// on cree la liste si elle n'existe pas
			if (this.data.get(name) == null) {
				this.data.put(name, new ArrayList<Double>());
			}
			this.data.get(name).add(this.getCounts(imp));
		}
	}

	public List<XYSeries> getSeries() {
		List<XYSeries> listSeries = new ArrayList<XYSeries>();

		for (String k : this.data.keySet()) {
			List<Double> data = this.data.get(k);
			listSeries.add(createSerie(data, k));
		}
		return listSeries;
	}

	public static Double getTDemiObs(XYSeries series, Double startX) {
		int yDemi = (int) (getY(series, startX) / 2);
		for (int i = 1; i < series.getItemCount(); i++) {
			if (series.getY(i - 1).doubleValue() >= yDemi && series.getY(i).doubleValue() <= yDemi) {
				Double x = (series.getX(i - 1).doubleValue() + series.getX(i).doubleValue()) / 2;
				if (x >= startX)
					return x;
			}
		}
		return 0.0;
	}

	/**
	 * renvoie une serie avec les ordonnees en coups / sec
	 * 
	 * @param l
	 *            liste de points
	 * @param nom
	 *            nom de la serie
	 * @return la serie
	 */
	public static XYSeries createSerie(List<Double> l, String nom) {
		if(l.size() != FRAMEDURATION.length) {
			throw new IllegalArgumentException("List size does not match duration time");
		}
		
		XYSeries points = new XYSeries(nom, true);

		Double dureePriseOld = 0.0;
		for (int i = 0; i < l.size(); i++) {
			// en secondes
			Double dureePrise = FRAMEDURATION[i] / 60000.0;
			points.add(dureePriseOld + dureePrise, l.get(i));
			dureePriseOld += dureePrise;
		}

		return points;
	}

	public static Double getMaxY(XYSeries series) {
		Number maxY = series.getMaxY();
		List<XYDataItem> items = series.getItems();
		for (XYDataItem i : items) {
			if (maxY.equals(i.getY())) {
				return i.getX().doubleValue();
			}
		}
		return null;
	}

	public static double getMaxY(XYDataset ds, int series) {
		Double maxY = 0.0;
		for (int i = 0; i < ds.getItemCount(series); i++) {
			if (ds.getY(series, i).doubleValue() > maxY) {
				maxY = ds.getYValue(series, i);
			}
		}

		for (int i = 0; i < ds.getItemCount(series); i++) {
			if (maxY.equals(ds.getY(series, i))) {
				return ds.getXValue(series, i);
			}
		}

		return 0.0;
	}

	/**
	 * renvoie la liste des ordonnees de la serie passee en parametre
	 * 
	 * @param la
	 *            serie
	 * @return liste des ordonnees
	 */
	public List<Double> seriesToList(XYSeries s) {
		ArrayList<Double> l = new ArrayList<Double>();
		for (int i = 0; i < s.getItemCount(); i++) {
			l.add(s.getY(i).doubleValue());
		}
		return l;
	}

	public List<Double> adjustValues(List<Double> values) {
		List<Double> valuesAdjusted = new ArrayList<Double>();

		for (int i = 0; i < values.size(); i++) {
			Double dureePrise = FRAMEDURATION[i] / 60000.0;
			valuesAdjusted.add(values.get(i) / (dureePrise * 60));
		}

		return valuesAdjusted;
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
		return FRAMEDURATION;
	}

	public void lock() {
		this.lock = true;
	}

	public void unlock() {
		this.lock = false;
	}

}
