package org.petctviewer.scintigraphy.hepatic.dyn;

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
import org.jfree.data.statistics.Regression;
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
	private XYDataset dataset;

	// resultats calcules
	private int tDemiFoieDFit, tDemiFoieGFit, tDemiVascFit, tDemiFoieDObs, tDemiFoieGObs, tDemiVascObs;
	private int maxFoieD, maxFoieG;

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
		this.tDemiFoieDFit = this.getTDemiFit(liverR, (maxFoieD + 2)*1.0);
		this.tDemiFoieDObs = this.getTDemiObs(liverR, (maxFoieD + 2)*1.0);
		this.finPicD = this.liverR.getY(liverR.getItemCount() - 1).doubleValue() / liverR.getMaxY();

		this.maxFoieG = this.getMax(this.liverL).intValue();
		this.tDemiFoieGFit = this.getTDemiFit(liverL, (maxFoieG + 2)*1.0);
		this.tDemiFoieGObs = this.getTDemiObs(liverL, (maxFoieG + 2)*1.0);
		this.finPicG = this.liverL.getY(liverL.getItemCount() - 1).doubleValue() / liverL.getMaxY();

		this.pctVasc = this.getY(bloodPool, 20.0) / this.getY(bloodPool, 5.0);
		this.tDemiVascFit = this.getTDemiFit(bloodPool, 20.0);
		this.tDemiVascObs = this.getTDemiObs(bloodPool, 20.0);

	}

	private Double getY(XYSeries series, double x) {
		for (int i = 1; i < series.getItemCount(); i++) {
			if ((series.getX(i-1).doubleValue() <= x && x <= series.getX(i).doubleValue())
					|| (series.getX(i-1).doubleValue() >= x && x >= series.getX(i).doubleValue())) {
				Double y = (series.getY(i-1).doubleValue() + series.getY(i).doubleValue()) / 2;
				return y;
			}
		}
		return 0.0;
	}

	private int getTDemiObs(XYSeries series, Double startX) {
		int yDemi = (int) (getY(series, startX) / 2);
		for(int i = 1; i < series.getItemCount(); i++) {
			if(series.getY(i-1).doubleValue() >= yDemi && series.getY(i).doubleValue() <= yDemi) {
					int x = (series.getX(i-1).intValue() + series.getX(i).intValue()) / 2;
					if(x >= startX)
						return x;
			}
		}
		return 0;
	}

	private int getTDemiFit(XYSeries series, Double startX) {
		XYSeries linear = new XYSeries("linear");
		for(int i = 0; i < series.getItemCount(); i++) {
			XYDataItem item = series.getDataItem(i);
			if(item.getX().doubleValue() >= startX) {
				linear.add(item.getX().doubleValue(), Math.log(item.getY().doubleValue()));
			}			
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(linear);
		
		double[] results = Regression.getOLSRegression(dataset, 0);
		
		int tdemi = (int) (Math.log(2.0) / results[1]) * -1;
		return tdemi;		
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
		this.dataset = createDataset();
		JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", "counts/sec", this.dataset,
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

		Double dureePriseOld = 0.0;
		for (int i = 0; i < this.vasc.size(); i++) {
			Double dureePrise = vue.getFrameDurations()[i] / 60000.0;
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
		hm.put("T1/2 Righ Liver", tDemiFoieDObs + "mn");
		hm.put("T1/2 Righ Liver *", tDemiFoieDFit + "mn");
		hm.put("Maximum Right Liver", maxFoieD + "mn");
		hm.put("end/max Ratio Right", (int) (finPicD * 100) + "%");

		// foie gauche
		hm.put("T1/2 Left Liver", tDemiFoieGObs + "mn");
		hm.put("T1/2 Left Liver *", tDemiFoieGFit + "mn");
		hm.put("Maximum Left Liver", maxFoieG + "mn");
		hm.put("end/max Ratio Left", (int) (finPicG * 100) + "%");

		// vasculaire
		hm.put("Blood pool ratio 20mn/5mn", (int) (pctVasc * 100) + "%");
		hm.put("T1/2 Blood pool", tDemiVascObs + "mn");
		hm.put("T1/2 Blood pool *", tDemiVascFit + "mn");

		return hm;
	}

	@Override
	public String toString() {
		String s = "";
		s += "Time (mn),";
		for (int i = 0; i < this.bloodPool.getItemCount(); i++) {
			s += round(this.bloodPool.getX(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Blood Pool (counts/sec),";
		for (int i = 0; i < this.bloodPool.getItemCount(); i++) {
			s += round(this.bloodPool.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Right Liver (counts/sec),";
		for (int i = 0; i < this.liverR.getItemCount(); i++) {
			s += round(this.liverR.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Left Liver (counts/sec),";
		for (int i = 0; i < this.liverL.getItemCount(); i++) {
			s += round(this.liverL.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "T1/2 Right Liver Obs," + this.tDemiFoieDObs + "mn" + "\n";
		s += "T1/2 Right Liver Fit," + this.tDemiFoieDFit + "mn" + "\n";
		s += "Maximum Right Liver," + this.maxFoieD + "mn" + "\n";
		s += "END/MAX Ratio Right," + (int) (finPicD * 100) + "%" + "\n";

		s += "T1/2 Left Liver Obs," + this.tDemiFoieGObs + "mn" + "\n";
		s += "T1/2 Left Liver Fit," + this.tDemiFoieGFit + "mn" + "\n";
		s += "Maximum Left Liver," + this.maxFoieG + "mn" + "\n";
		s += "END/MAX Ratio Left," + (int) (finPicG * 100) + "%" + "\n";

		s += "Blood pool ratio 20mn/5mn," + (int) (pctVasc * 100) + "%" + "\n";
		s += "T1/2 Blood pool Obs," + this.tDemiVascObs + "mn" + "\n";
		s += "T1/2 Blood pool Fit," + this.tDemiVascFit + "mn" + "\n";

		return s;
	}

}
