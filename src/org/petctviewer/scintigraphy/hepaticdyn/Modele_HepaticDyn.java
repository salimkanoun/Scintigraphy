package org.petctviewer.scintigraphy.hepaticdyn;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.ImagePlus;
import ij.util.DicomTools;

public class Modele_HepaticDyn extends ModeleScin {

	private List<Double> vasc, foieD, foieG;
	private ChartPanel chartPanel;

	private XYSeries bloodPool, liverR, liverL;

	// resultats calcules
	private int tDemiFoieD, tDemiFoieG, maxFoieD, maxFoieG, tDemiVasc;

	private Double finPicD, finPicG, pctVasc;
	
	private Vue_HepaticDyn vue;

	public Modele_HepaticDyn(Vue_HepaticDyn vue) {
		this.imp = (ImagePlus) vue.getImp().clone();
		this.vasc = new ArrayList<Double>();
		this.foieD = new ArrayList<Double>();
		this.foieG = new ArrayList<Double>();
		this.vue = vue;
	}

	@Override
	public void enregisterMesure(String nomRoi, ImagePlus imp) {
		Double counts = this.getCounts(imp);

		if (nomRoi.contains("Blood pool")) {
			vasc.add(counts);
		} else if (nomRoi.contains("Liver R")) {
			foieD.add(counts);
		} else if (nomRoi.contains("Liver L")) {
			foieG.add(counts);
		}

		System.out.println(nomRoi + " : " + counts);
	}

	@Override
	public void calculerResultats() {
		// on supprime les premieres valeurs venant de la projection
		this.vasc.remove(0);
		this.foieD.remove(0);
		this.foieG.remove(0);

		// on cree le graphique
		createGraph();
		
		this.maxFoieD = this.getMax(this.liverR).intValue();
		this.tDemiFoieD = this.getTDemi(liverR, maxFoieD);
		this.finPicD = this.liverR.getY(liverR.getItemCount() - 1).doubleValue() / liverR.getMaxY();

		this.maxFoieG = this.getMax(this.liverL).intValue();
		this.tDemiFoieG = this.getTDemi(liverL, maxFoieG);
		this.finPicG = this.liverL.getY(liverL.getItemCount() - 1).doubleValue() / liverL.getMaxY();

		this.pctVasc = this.getY(bloodPool, 20.0) / this.getY(bloodPool, 5.0);

	}

	private Double getY(XYSeries series, double x) {
		List<XYDataItem> items = series.getItems();
		for (int i = 1; i < items.size(); i++) {
			if (items.get(i - 1).getX().doubleValue() <= x && x <= items.get(i).getX().doubleValue()) {
				Double y = (items.get(i).getY().doubleValue() + items.get(i - 1).getY().doubleValue()) / 2;
				return y;
			}
		}
		return 0.0;
	}

	private int getTDemi(XYSeries series, int max) {
		int demi = max / 2;
		List<XYDataItem> items = series.getItems();
		for (int i = 1; i < items.size(); i++) {
			if (items.get(i - 1).getY().intValue() <= demi && items.get(i).getY().intValue() >= demi) {
				if (items.get(i).getX().intValue() > this.maxFoieD) {
					int x = (items.get(i).getX().intValue() + items.get(i - 1).getX().intValue()) / 2;
					return x;
				}
			}
		}
		return 0;
	}

	private Number getMax(XYSeries series) {
		Number maxY = series.getMaxY();
		List<XYDataItem> items = series.getItems();
		for (XYDataItem i : items) {
			if (maxY.equals(i.getY())) {
				return i.getX();
			}
		}
		return null;
	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	private void createGraph() {
		JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", "counts/sec", createDataset(),
				PlotOrientation.VERTICAL, true, true, true);
		this.chartPanel = new ChartPanel(xylineChart);
		final XYPlot plot = xylineChart.getXYPlot();

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.GREEN);
		renderer.setSeriesPaint(2, Color.BLUE);
		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesShapesVisible(1, false);
		renderer.setSeriesShapesVisible(2, false);
		plot.setRenderer(renderer);
	}

	private XYDataset createDataset() {
		this.bloodPool = new XYSeries("Blood Pool");
		this.liverR = new XYSeries("Right Liver");
		this.liverL = new XYSeries("Left Liver");
		
		System.out.println(vasc.size());
		
		Double dureePriseOld = 0.0;
		for (int i = 0; i < this.vasc.size(); i++) {
			Double dureePrise = vue.frameDurations[i] / 60000.0;
			bloodPool.add(dureePriseOld + dureePrise, vasc.get(i) / (dureePrise * 60));
			liverR.add(dureePriseOld + dureePrise, foieD.get(i) / (dureePrise * 60));
			liverL.add(dureePriseOld + dureePrise, foieG.get(i) / (dureePrise * 60));
			dureePriseOld += dureePrise;
		}

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(bloodPool);
		dataset.addSeries(liverL);
		dataset.addSeries(liverR);
		return dataset;
	}

	@Override
	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> hm = new HashMap<String, String>();

		// foie droit
		hm.put("T1/2 Righ Liver", tDemiFoieD + "mn");
		hm.put("Maximum Right Liver", maxFoieD + "mn");
		hm.put("END/MAX Ratio Right", (int) (finPicD * 100) + "%");

		// foie gauche
		hm.put("T1/2 Left Liver", tDemiFoieG + "mn");
		hm.put("Maximum Left Liver", maxFoieG + "mn");
		hm.put("END/MAX Ratio Left", (int) (finPicG * 100) + "%");

		// vasculaire
		hm.put("Blood pool ratio 20mn/5mn", (int) (pctVasc * 100) + "%");
		hm.put("T1/2 Blood pool", tDemiVasc + "mn");

		return hm;
	}

	@Override
	public String toString() {
		return this.vasc.toString() + this.foieD.toString() + this.foieG.toString();
	}

}
