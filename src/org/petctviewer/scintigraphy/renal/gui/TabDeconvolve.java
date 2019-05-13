package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
/**
 * Tab showing the deconvolve of Left, Right or both kidney, depending of the initial choice, using the Blood Pool values.
 *
 */
public class TabDeconvolve extends TabResult implements ChangeListener {

	private int deconvolve;

	private JSpinner spinner;

	public TabDeconvolve(FenResults parent, String title) {
		super(parent, title);
		// TODO Auto-generated constructor stub

		this.deconvolve = 0;

		this.spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		spinner.addChangeListener(this);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {

		JPanel resultPane = new JPanel(new GridLayout(1, 1));
		resultPane.add(spinner);
		return resultPane;

	}

	@Override
	public JPanel getResultContent() {
		Modele_Renal modele = (Modele_Renal) this.parent.getModel();

		if (modele.getKidneys()[0] && modele.getKidneys()[1]) {
			
			
			
			List<Double> bloodPoolCorrected = getAVGPixelCounts(modele, "Blood Pool");
			
			bloodPoolCorrected = truckage(bloodPoolCorrected);
			
			List<Double> leftKidneyCorrected = getAVGPixelCounts(modele, "L. Kidney");
			
			
//			List<Double> bp = modele.getData("Blood Pool");
//			List<Double> leftKidney = modele.getData("L. Kidney");
			Double[] kernel = { 1.0d, 2.0d, 1.0d };
			Double[] convolvedBP = Library_Quantif.processNConvolv(bloodPoolCorrected, kernel, 12);
			Double[] convolvedKDNLeft = Library_Quantif.processNConvolv(leftKidneyCorrected, kernel, 12);

			@SuppressWarnings("deprecation")
			List<Double> deconvolveLeft = Library_Quantif.deconvolv(convolvedBP, convolvedKDNLeft, this.deconvolve);

			XYSeriesCollection dataLeft = new XYSeriesCollection();
			dataLeft.addSeries(modele.createSerie(deconvolveLeft, "Deconvolve"));
//			dataLeft.addSeries(modele.getSerie("Blood Pool"));
//			dataLeft.addSeries(modele.getSerie("L. Kidney"));
//			dataLeft.addSeries(modele.createSerie(Arrays.asList(convolvedKDNLeft), "L. Kidney convolved"));
			JFreeChart chartLeft = ChartFactory.createXYLineChart("", "min", "counts/sec", dataLeft);
			
			
			XYSeriesCollection dataset2 = new XYSeriesCollection();
			dataset2.addSeries(modele.createSerie(Arrays.asList(convolvedKDNLeft), "L. Kidney convolved"));
			dataset2.addSeries(modele.createSerie(Arrays.asList(convolvedBP), "Blood Pool convolved"));
			
			XYPlot plot = new XYPlot();
	        plot.setDataset(0, dataLeft);
	        plot.setDataset(1, dataset2);

	        //customize the plot with renderers and axis
	        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,false);
			plot.setRenderer(renderer);
	        plot.setRangeAxis(0, new NumberAxis("Initial values (count/sec)"));
	        plot.setRangeAxis(1, new NumberAxis("6 times convolved"));
	        plot.setDomainAxis(new NumberAxis("Time (minutes)"));

	        //Map the data to the appropriate axis
	        plot.mapDatasetToRangeAxis(0, 0);
	        plot.mapDatasetToRangeAxis(1, 1);

	        //generate the chart
	        JFreeChart chart = new JFreeChart("Left Kifney", null, plot, true);

			ChartPanel chartpanelLeft = new ChartPanel(chart);
			
			
			
			
			
			
			
			
			
			List<Double> rightKidneyCorrected = getAVGPixelCounts(modele, "R. Kidney");
//			List<Double> rightKidney = modele.getData("R. Kidney");
			Double[] convolvedKDNRight = Library_Quantif.processNConvolv(rightKidneyCorrected, kernel, 12);

			@SuppressWarnings("deprecation")
			List<Double> deconvolveRight = Library_Quantif.deconvolv(convolvedBP, convolvedKDNRight, this.deconvolve);

			XYSeriesCollection dataRight = new XYSeriesCollection();
			dataRight.addSeries(modele.createSerie(deconvolveRight, "Deconvolve"));
//			dataRight.addSeries(modele.getSerie("Blood Pool"));
//			dataRight.addSeries(modele.getSerie("R. Kidney"));
//			dataRight.addSeries(modele.createSerie(Arrays.asList(convolvedKDNRight), "R. Kidney convolved"));
			JFreeChart chartRight = ChartFactory.createXYLineChart("", "min", "counts/sec", dataRight);
			
			
			
			XYSeriesCollection dataset2Right = new XYSeriesCollection();
			dataset2Right.addSeries(modele.createSerie(Arrays.asList(convolvedKDNRight), "R. Kidney convolved"));
			dataset2Right.addSeries(modele.createSerie(Arrays.asList(convolvedBP), "Blood Pool convolved"));
			
			XYPlot plotRight = new XYPlot();
			plotRight.setDataset(0, dataRight);
			plotRight.setDataset(1, dataset2Right);

	        //customize the plot with renderers and axis
	        XYLineAndShapeRenderer rendererRight = new XYLineAndShapeRenderer(true,false);
	        rendererRight.setSeriesPaint(0, Color.green);
	        rendererRight.setSeriesPaint(1, Color.red);
	        rendererRight.setSeriesPaint(2, Color.blue);
	        plotRight.setRenderer(rendererRight);
	        plotRight.setRangeAxis(0, new NumberAxis("Initial values (count/sec)"));
	        plotRight.setRangeAxis(1, new NumberAxis("6 times convolved"));
	        plotRight.setDomainAxis(new NumberAxis("Time (minutes)"));

	        //Map the data to the appropriate axis
	        plotRight.mapDatasetToRangeAxis(0, 0);
	        plotRight.mapDatasetToRangeAxis(1, 1);

	        //generate the chart
	        JFreeChart chart2 = new JFreeChart("Right Kidney", null, plotRight, true);

			ChartPanel chartpanelRight = new ChartPanel(chart2);

			JPanel grid = new JPanel(new GridLayout(2, 1));

			grid.add(chartpanelLeft);
			grid.add(chartpanelRight);

			return grid;
		} else if (modele.getKidneys()[0]) {
			List<Double> bp = modele.getData("Blood Pool");
			List<Double> kidney = modele.getData("L. Kidney");
			Double[] kernel = { 1.0d, 2.0d, 1.0d };
			Double[] convolvedBP = Library_Quantif.processNConvolv(bp, kernel, 6);
			Double[] convolvedKDN = Library_Quantif.processNConvolv(kidney, kernel, 6);

			@SuppressWarnings("deprecation")
			List<Double> deconv = Library_Quantif.deconvolv(convolvedBP, convolvedKDN, this.deconvolve);

			XYSeriesCollection data = new XYSeriesCollection();
			data.addSeries(modele.createSerie(deconv, "Deconvolve"));
			data.addSeries(modele.getSerie("Blood Pool"));
			data.addSeries(modele.getSerie("L. Kidney"));
			JFreeChart chart = ChartFactory.createXYLineChart("", "min", "counts/sec", data);

			ChartPanel chartpanel = new ChartPanel(chart);

			return chartpanel;
		} else {
			List<Double> bp = modele.getData("Blood Pool");
			List<Double> kidney = modele.getData("R. Kidney");
			Double[] kernel = { 1.0d, 2.0d, 1.0d };
			Double[] convolvedBP = Library_Quantif.processNConvolv(bp, kernel, 6);
			Double[] convolvedKDN = Library_Quantif.processNConvolv(kidney, kernel, 6);

			@SuppressWarnings("deprecation")
			List<Double> deconv = Library_Quantif.deconvolv(convolvedBP, convolvedKDN, this.deconvolve);

			XYSeriesCollection data = new XYSeriesCollection();
			data.addSeries(modele.createSerie(deconv, "Deconvolve"));
			data.addSeries(modele.getSerie("Blood Pool"));
			data.addSeries(modele.getSerie("R. Kidney"));
			JFreeChart chart = ChartFactory.createXYLineChart("", "min", "counts/sec", data);

			ChartPanel chartpanel = new ChartPanel(chart);

			return chartpanel;
		}

	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		JSpinner spin = (JSpinner) arg0.getSource();
		this.deconvolve = ((int) spin.getValue());
		this.reloadDisplay();
	}
	
	
	public List<Double> getAVGPixelCounts(Modele_Renal modele, String name){
		
		List<Double> values = modele.getData(name);
		
		List<Double> finalValues = new ArrayList<>();

		
		for(Double doubles : values)
			finalValues.add(doubles/modele.getPixelCount(name));
		
		System.out.println("Nombre de pixels : "+modele.getPixelCount(name));

		
		return finalValues;
	}
	
	public List<Double> truckage(List<Double> truck){
		
		List<Double> values = truck;
		
		List<Double> finalValues = new ArrayList<>();

		
		for(Double doubles : values)
			finalValues.add(doubles*3);

		
		return finalValues;
	}

}
