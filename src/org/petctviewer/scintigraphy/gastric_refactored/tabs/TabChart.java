package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit;
import org.petctviewer.scintigraphy.gastric_refactored.gui.FitCellRenderer;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabChart extends TabResult implements ActionListener {

	private XYSeriesCollection data;
	private JFreeChart chart;

	private XYAnnotation linearFit;

	public TabChart(FenResults parent) {
		super(parent, "Stomach retention");
	}

	private void createChart() {
		this.data = new XYSeriesCollection();
		this.data.addSeries(((Model_Gastric) this.parent.getModel()).getStomachSerie());

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (min)", "Stomach retention (%)",
				data, PlotOrientation.VERTICAL, true, true, true);
		this.chart = chart;
	}

	private XYSeries createExponentialSerie(double[] regression) {
		final int resolution = 10;
		XYSeries serie = new XYSeries("Exponential Fit");
		for (int i = 0; i < this.chart.getXYPlot().getDomainAxis().getUpperBound(); i += resolution) {
			serie.add(i, Math.pow(i, regression[1]) * regression[0]);
			System.out.println("Formula: " + regression[0] + "*x^" + regression[1]);
			System.out.println("New point: (" + i + " ; " + Math.pow(i, regression[1]) * regression[0] + ")");
		}
		return serie;
	}

	private XYSeries createPolynomialSerie(double[] coefficients) {
		final int resolution = 10;
		XYSeries serie = new XYSeries(Fit.POLYNOMIAL_FIT.getName());
		for (int x = 0; x < this.chart.getXYPlot().getDomainAxis().getUpperBound(); x += resolution) {
			double y = coefficients[0];
			for (int i = 1; i < coefficients.length - 1; i++)
				y += Math.pow(x, i) * coefficients[i];
			serie.add(x, y);
		}
		return serie;
	}

	private XYSeries createLnSerie() {
		XYSeries serie = new XYSeries("Ln(x)");
		double[] time = Model_Gastric.temps;
		double[] estomac = Model_Gastric.estomacPourcent;
		for (int i = 0; i < time.length; i++)
			serie.add(time[i], Math.log(estomac[i]));
		return serie;
	}
	
	private XYSeries createExponential(double[] coefs) {
		XYSeries serie = new XYSeries("e(x)");
		double[] time = Model_Gastric.temps;
		for (int i = 0; i < time.length; i++)
			serie.add(time[i], Math.exp(coefs[0]) * Math.exp(coefs[1] * time[i]));
		return serie;
	}
	
	private XYSeries createExponential_2(double[] coefs) {
		XYSeries serie = new XYSeries("e2(x)");
		double[] time = Model_Gastric.temps;
		for (int i = 0; i < time.length; i++)
			serie.add(time[i], coefs[0] * Math.exp(coefs[1] * time[i]));
		return serie;
	}

	private void drawFit(Fit fit) {
		// Remove previous fit
		if (linearFit != null)
			this.chart.getXYPlot().removeAnnotation(linearFit);
		if (this.data.getSeriesCount() > 1)
			this.data.removeSeries(1);

		Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
				new float[] { 6.0f, 6.0f }, 0.0f);
		double maxValue = chart.getXYPlot().getDomainAxis().getUpperBound();

		if (fit == Fit.LINEAR_FIT) {
			double[] regression = Regression.getOLSRegression(this.data, 0);
			linearFit = new XYLineAnnotation(0, regression[0], maxValue, maxValue * regression[1] + regression[0],
					stroke, Color.BLUE);
			chart.getXYPlot().addAnnotation(linearFit);
		} else if (fit == Fit.POWER_FIT) {
			double[] regression = Regression.getPowerRegression(data, 0);
			System.out.println("Regression: " + Arrays.toString(regression));
			this.data.addSeries(this.createExponentialSerie(regression));
		} else if (fit == Fit.POLYNOMIAL_FIT) {
			double[] regression = Regression.getPolynomialRegression(data, 0, 3);
			System.out.println("Regression: " + Arrays.toString(regression));
			this.data.addSeries(this.createPolynomialSerie(regression));
		} else if (fit == Fit.TEST_FIT) {
			this.data.addSeries(this.createLnSerie());
			double[] regression = Regression.getOLSRegression(this.data, 1);
			linearFit = new XYLineAnnotation(0, regression[0], maxValue, maxValue * regression[1] + regression[0],
					stroke, Color.ORANGE);
			chart.getXYPlot().addAnnotation(linearFit);
			this.data.addSeries(this.createExponential(regression));
			this.data.addSeries(this.createExponential_2(regression));
		}
	}

	@Override
	public Component getSidePanelContent() {
		JComboBox<Fit> fitList = new JComboBox<>(Fit.allFits());
		fitList.setRenderer(new FitCellRenderer());
		fitList.addActionListener(this);
		JPanel panel = new JPanel();
		panel.add(fitList);
		return panel;
	}

	@Override
	public JPanel getResultContent() {
		this.createChart();
		return new ChartPanel(chart);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox<Fit> source = (JComboBox<Fit>) e.getSource();
		this.drawFit((Fit) source.getSelectedItem());
	}

}
