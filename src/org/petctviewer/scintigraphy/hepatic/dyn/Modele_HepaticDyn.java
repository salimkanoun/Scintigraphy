package org.petctviewer.scintigraphy.hepatic.dyn;

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
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import ij.ImagePlus;

public class Modele_HepaticDyn extends ModeleScinDyn {

	private ChartPanel chartPanel;

	// resultats calcules
	private Double tDemiFoieDFit, tDemiFoieGFit, tDemiVascFit, tDemiFoieDObs, tDemiFoieGObs, tDemiVascObs;
	private Double maxFoieD, maxFoieG, finPicD, finPicG, pctVasc;

	public Modele_HepaticDyn(HepaticDynamicScintigraphy vue) {
		super(vue.getFrameDurations());
		this.imp = (ImagePlus) vue.getImp().clone();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		super.enregistrerMesure(nomRoi, imp);
	}

	@Override
	public void calculerResultats() {
		XYSeries liverL = this.getSerie("L. Liver");
		XYSeries liverR = this.getSerie("R. Liver");
		XYSeries bloodPool = this.getSerie("Blood pool");

		this.maxFoieD = ModeleScinDyn.getAbsMaxY(liverR);
		//this.tDemiFoieDFit = Modele_HepaticDyn.getTDemiFit(liverR, (this.maxFoieD + 2)*1.0);
		this.tDemiFoieDObs = ModeleScinDyn.getTDemiObs(liverR, this.maxFoieD + 2);
		this.finPicD = liverR.getY(liverR.getItemCount() - 1).doubleValue() / liverR.getMaxY();

		this.maxFoieG = ModeleScinDyn.getAbsMaxY(liverL);
		//this.tDemiFoieGFit = Modele_HepaticDyn.getTDemiFit(liverL, this.maxFoieG + 2);
		this.tDemiFoieGObs = ModeleScinDyn.getTDemiObs(liverL, this.maxFoieG + 2);
		this.finPicG = liverL.getY(liverL.getItemCount() - 1).doubleValue() / liverL.getMaxY();

		this.pctVasc = ModeleScinDyn.getY(bloodPool, 20.0) / ModeleScinDyn.getY(bloodPool, 5.0);
		//this.tDemiVascFit = Modele_HepaticDyn.getTDemiFit(bloodPool, 20.0);
		this.tDemiVascObs = ModeleScinDyn.getTDemiObs(bloodPool, 20.0);

	}

	private static int getTDemiFit(XYSeries series, Double startX) {
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
	
	private XYDataset createDataset() {
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(this.getSerie("Blood pool"));
		dataset.addSeries(this.getSerie("L. Liver"));
		dataset.addSeries(this.getSerie("R. Liver"));
		return dataset;
	}

	public HashMap<String, String> getResultsHashMap() {
		HashMap<String, String> hm = new HashMap<>();

		// foie droit
		hm.put("T1/2 Righ Liver", this.tDemiFoieDObs + "mn");
		hm.put("T1/2 Righ Liver *", this.tDemiFoieDFit + "mn");
		hm.put("Maximum Right Liver", round(this.maxFoieD, 1) + "mn");
		hm.put("end/max Ratio Right", (int) (this.finPicD * 100) + "%");

		// foie gauche
		hm.put("T1/2 Left Liver", this.tDemiFoieGObs + "mn");
		hm.put("T1/2 Left Liver *", this.tDemiFoieGFit + "mn");
		hm.put("Maximum Left Liver", round(this.maxFoieG, 1) + "mn");
		hm.put("end/max Ratio Left", (int) (this.finPicG * 100) + "%");

		// vasculaire
		hm.put("Blood pool ratio 20mn/5mn", (int) (this.pctVasc * 100) + "%");
		hm.put("T1/2 Blood pool", this.tDemiVascObs + "mn");
		hm.put("T1/2 Blood pool *", this.tDemiVascFit + "mn");

		return hm;
	}

	@Override
	public String toString() {
		XYSeries liverL = this.getSerie("L. Liver");
		XYSeries liverR = this.getSerie("R. Liver");
		XYSeries bloodPool = this.getSerie("Blood pool");
		
		String s = "";
		s += "Time (mn),";
		for (int i = 0; i < bloodPool.getItemCount(); i++) {
			s += round(bloodPool.getX(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Blood Pool (counts/sec),";
		for (int i = 0; i < bloodPool.getItemCount(); i++) {
			s += round(bloodPool.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Right Liver (counts/sec),";
		for (int i = 0; i < liverR.getItemCount(); i++) {
			s += round(liverR.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "Left Liver (counts/sec),";
		for (int i = 0; i < liverL.getItemCount(); i++) {
			s += round(liverL.getY(i).doubleValue(), 2) + ",";
		}
		s += "\n";

		s += "T1/2 Right Liver Obs," + this.tDemiFoieDObs + "mn" + "\n";
		s += "T1/2 Right Liver Fit," + this.tDemiFoieDFit + "mn" + "\n";
		s += "Maximum Right Liver," + this.maxFoieD + "mn" + "\n";
		s += "END/MAX Ratio Right," + (int) (this.finPicD * 100) + "%" + "\n";

		s += "T1/2 Left Liver Obs," + this.tDemiFoieGObs + "mn" + "\n";
		s += "T1/2 Left Liver Fit," + this.tDemiFoieGFit + "mn" + "\n";
		s += "Maximum Left Liver," + this.maxFoieG + "mn" + "\n";
		s += "END/MAX Ratio Left," + (int) (this.finPicG * 100) + "%" + "\n";

		s += "Blood pool ratio 20mn/5mn," + (int) (this.pctVasc * 100) + "%" + "\n";
		s += "T1/2 Blood pool Obs," + this.tDemiVascObs + "mn" + "\n";
		s += "T1/2 Blood pool Fit," + this.tDemiVascFit + "mn" + "\n";

		return s;
	}

}
