package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
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

	private XYSeries polynomialSeries, exponentialSeries;

	private JComboBox<Fit> fitsChoices;
	private JLabel labelInterpolation;

	public TabChart(FenResults parent) {
		super(parent, "Stomach retention");
		Component[] hide = new Component[] { this.fitsChoices };
		Component[] show = new Component[] { this.labelInterpolation };
		this.createCaptureButton(hide, show, null);
	}

	// TODO: maybe move this method in a library
	/**
	 * From an array of the form:
	 * 
	 * <pre>
	 * [0][i] = x
	 * [1][i] = y
	 * </pre>
	 * 
	 * inverts it to the form:
	 * 
	 * <pre>
	 * [i][0] = x
	 * [i][1] = y
	 * </pre>
	 * 
	 * @param toInvert Array to invert
	 * @return Array where columns are rows
	 */
	private double[][] invertArray(double[][] toInvert) {
		double[][] res = new double[toInvert[0].length][2];
		for (int j = 0; j < 2; j++)
			for (int i = 0; i < toInvert[j].length; i++)
				res[i][j] = toInvert[j][i];
		return res;
	}

	/**
	 * Creates the chart along with all series.
	 */
	private void createChart() {
		// Create chart
		this.data = new XYSeriesCollection();
		this.data.addSeries(((Model_Gastric) this.parent.getModel()).getStomachSerie());

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (min)", "Stomach retention (%)",
				data, PlotOrientation.VERTICAL, true, true, true);
		this.chart = chart;

		// Create series
		// - Polynomial
		if (data.getItemCount(0) >= 4) {
			double[] regression = Regression.getPolynomialRegression(data, 0, 3);
			this.polynomialSeries = this.createPolynomialSerie(regression, 10);
		} else {
			// Remove fit
			this.fitsChoices.removeItem(Fit.POLYNOMIAL_FIT);
		}

		// - Exponential
		double[] regression = Regression.getOLSRegression(this.invertArray(this.createLnSerie().toArray()));
		this.exponentialSeries = this.createExponential(regression);
	}

	/**
	 * Creates a polynomial series with the specified coefficients.<br>
	 * The number of x calculated is equal to
	 * <code>(maxX - minX) / resolution</code>.
	 * 
	 * @param coefficients Coefficients for the polynomial function
	 * @param resolution   Gap between two X calculations
	 * @return XYSeries representing the polynomial series for the specified
	 *         coefficient
	 */
	private XYSeries createPolynomialSerie(double[] coefficients, int resolution) {
		XYSeries serie = new XYSeries(Fit.POLYNOMIAL_FIT.getName());
		for (int x = (int) this.data.getSeries(0).getMinX() - 1; x < this.data.getSeries(0)
				.getMaxX(); x += resolution) {
			double y = coefficients[0];
			for (int i = 1; i < coefficients.length - 1; i++)
				y += Math.pow(x, i) * coefficients[i];
			serie.add(x, y);
		}
		return serie;
	}

	/**
	 * Creates a logarithm series from the values of the stomach percentage of the
	 * model.
	 * 
	 * @return XYSeries of the logarithm function applied to the stomach percentage
	 */
	private XYSeries createLnSerie() {
		XYSeries serie = new XYSeries("Ln(x)");
		double[] time = Model_Gastric.temps; // TODO: do not use public attributes
		double[] estomac = Model_Gastric.estomacPourcent;
		for (int i = 0; i < time.length; i++)
			serie.add(time[i], Math.log(estomac[i]));
		return serie;
	}

	/**
	 * Creates an exponential series with the specified coefficients fitting the
	 * stomach percentage of the model.
	 * 
	 * @param coefs Coefficients of the exponential function:
	 *              <code>exp^(coef[0]) * exp^(coef[1] * x)</code>
	 * @return XYSeries representing the exponential function applied to the stomach
	 *         percentage
	 */
	private XYSeries createExponential(double[] coefs) {
		XYSeries serie = new XYSeries("e(x)");
		double[] time = Model_Gastric.temps;
		for (int i = 0; i < time.length; i++)
			serie.add(time[i], Math.exp(coefs[0]) * Math.exp(coefs[1] * time[i]));
		return serie;
	}

	/**
	 * Removes all previous fits (annotations included).
	 */
	private void clearFits() {
		if (linearFit != null)
			this.chart.getXYPlot().removeAnnotation(linearFit);
		for (int i = 1; i < this.data.getSeriesCount(); i++)
			this.data.removeSeries(i);
	}

	/**
	 * Displays the specified fit and removes the previous fit.
	 * 
	 * @param fit Type of regression to fit the values of the chart
	 */
	private void drawFit(Fit fit) {
		this.clearFits();

		Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
				new float[] { 6.0f, 6.0f }, 0.0f);
		double maxValue = chart.getXYPlot().getDomainAxis().getUpperBound();

		if (fit == Fit.LINEAR_FIT) {
			double[] regression = Regression.getOLSRegression(this.data, 0);
			linearFit = new XYLineAnnotation(0, regression[0], maxValue, maxValue * regression[1] + regression[0],
					stroke, Color.BLUE);
			chart.getXYPlot().addAnnotation(linearFit);
		} else if (fit == Fit.POLYNOMIAL_FIT)
			this.data.addSeries(this.polynomialSeries);
		else if (fit == Fit.POWER_FIT)
			this.data.addSeries(this.exponentialSeries);
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel();

		if (this.fitsChoices == null) {
			// Instantiate combo box
			fitsChoices = new JComboBox<>(Fit.allFits());
			fitsChoices.setRenderer(new FitCellRenderer());
			fitsChoices.addActionListener(this);
		}
		panel.add(fitsChoices);

		if (this.labelInterpolation == null) {
			// Instantiate label
			this.labelInterpolation = new JLabel();
			this.labelInterpolation.setVisible(false);
		}
		panel.add(this.labelInterpolation);

		return panel;
	}

	@Override
	public JPanel getResultContent() {
		this.createChart();
		this.actionPerformed(new ActionEvent(this.fitsChoices, ActionEvent.ACTION_PERFORMED, null));
		return new ChartPanel(chart);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox<Fit> source = (JComboBox<Fit>) e.getSource();
		Fit selectedFit = (Fit) source.getSelectedItem();
		this.drawFit(selectedFit);
		
		// Change label interpolation text (for capture)
		if (selectedFit == Fit.NO_FIT)
			this.labelInterpolation.setText("");
		else
			this.labelInterpolation.setText("-- " + selectedFit.getName() + " interpolation --");
	}

}
