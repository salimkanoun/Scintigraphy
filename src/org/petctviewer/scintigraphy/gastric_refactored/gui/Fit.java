package org.petctviewer.scintigraphy.gastric_refactored.gui;

import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;

/**
 * This class represents a fit for a certain dataset. The fit can be used to
 * extrapolate values.
 * 
 * @author Titouan QUÉMA
 *
 */
public abstract class Fit {

	/**
	 * Type of a fit.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	public enum FitType {
		NONE("No Fit"), LINEAR("Linear"), EXPONENTIAL("Exponential");
		private String s;

		private FitType(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}

	private FitType type;

	public Fit(FitType type) {
		this.type = type;
	}

	/**
	 * Instantiates the fit corresponding to the specified type.
	 * 
	 * @param type    Type of fit to create
	 * @param dataset Dataset for the fit
	 * @return Instance of a fit
	 */
	public static Fit createFit(FitType type, double[][] dataset) {
		switch (type) {
		case LINEAR:
			return new LinearFit(dataset);
		case EXPONENTIAL:
			return new ExponentialFit(dataset);
		default:
			return new NoFit();
		}
	}

	/**
	 * Extrapolates the X value from the specified Y value according to this fit
	 * instance.
	 * 
	 * @param valueY Y value to extrapolate the X value from
	 * @return X value extrapolated
	 */
	public abstract double extrapolateX(double valueY);

	/**
	 * Extrapolates the Y value from the specified X value according to this fit
	 * instance.
	 * 
	 * @param valueX X value to extrapolate the Y value from
	 * @return Y value extrapolated
	 */
	public abstract double extrapolateY(double valueX);

	public double[] generateOrdinates(double[] valuesX) {
		double[] ordinates = new double[valuesX.length];
		for (int i = 0; i < valuesX.length; i++)
			ordinates[i] = this.extrapolateY(valuesX[i]);
		return ordinates;
	}

	/**
	 * @return series for the selected fit of the graph
	 */
	public XYSeries getFittedSeries(double[] xValues) {
		double[] y = this.generateOrdinates(xValues);
		XYSeries fittedSeries = new XYSeries(this.toString());
		for (int i = 0; i < xValues.length; i++)
			fittedSeries.add(xValues[i], y[i]);
		return fittedSeries;
	}

	/**
	 * @return type of this fit
	 */
	public FitType getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.type.s;
	}

	/**
	 * Linear extrapolation.
	 * 
	 * @see Regression#getOLSRegression
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	public static class LinearFit extends Fit {

		private double[] coefs;

		public LinearFit(double[][] dataset) {
			super(FitType.LINEAR);
			this.coefs = Regression.getOLSRegression(dataset);
		}

		@Override
		public double extrapolateX(double valueY) {
			double res = valueY - coefs[0] / coefs[1];
			return res;
		}

		@Override
		public double extrapolateY(double valueX) {
			double res = coefs[1] * valueX + coefs[0];
			return res;
		}

	}

	/**
	 * Exponential extrapolation.
	 * 
	 * @see Regression#getPowerRegression
	 * @author Titouan QUÉMA
	 *
	 */
	public static class ExponentialFit extends Fit {

		private double[] coefs;

		public ExponentialFit(double[][] dataset) {
			super(FitType.EXPONENTIAL);
			double[][] lnSeries = new double[dataset.length][2];
			for (int i = 0; i < dataset.length; i++) {
				lnSeries[i][0] = dataset[i][0];
				lnSeries[i][1] = Math.log(dataset[i][1]);
			}
			this.coefs = Regression.getOLSRegression(lnSeries);
		}

		@Override
		public double extrapolateX(double valueY) {
			double res = valueY - coefs[0] / coefs[1];
			return res;
		}

		@Override
		public double extrapolateY(double valueX) {
			double res = Math.exp(coefs[0]) * Math.exp(coefs[1] * valueX);
			return res;
		}

	}

	/**
	 * No extrapolation.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	public static class NoFit extends Fit {

		public NoFit() {
			super(FitType.NONE);
		}

		@Override
		public double extrapolateX(double valueY) {
			return 0.;
		}

		@Override
		public double extrapolateY(double valueX) {
			return 0.;
		}

	}

}