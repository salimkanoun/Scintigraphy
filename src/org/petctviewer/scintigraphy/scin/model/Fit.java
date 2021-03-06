package org.petctviewer.scintigraphy.scin.model;

import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;

/**
 * This class represents a fit for a certain dataset. The fit can be used to extrapolate values.
 *
 * @author Titouan QUÉMA
 */
public abstract class Fit {

	private final FitType type;
	private final Unit unit;

	public Fit(FitType type, Unit unit) {
		this.type = type;
		this.unit = unit;
	}

	/**
	 * Instantiates the fit corresponding to the specified type.
	 *
	 * @param type    Type of fit to create
	 * @param dataset Dataset for the fit
	 * @return Instance of a fit
	 */
	public static Fit createFit(FitType type, double[][] dataset, Unit unit) {
		switch (type) {
			case LINEAR:
				return new LinearFit(dataset, unit);
			case EXPONENTIAL:
				return new ExponentialFit(dataset, unit);
			default:
				return new NoFit(unit);
		}
	}

	/**
	 * Extrapolates the X value from the specified Y value according to this fit instance.
	 *
	 * @param valueY Y value to extrapolate the X value from
	 * @return X value extrapolated
	 */
	public abstract double extrapolateX(double valueY);

	/**
	 * Extrapolates the Y value from the specified X value according to this fit instance.
	 *
	 * @param valueX X value to extrapolate the Y value from
	 * @return Y value extrapolated
	 */
	public abstract double extrapolateY(double valueX);

	/**
	 * @return unit of the Y values
	 */
	public Unit getYUnit() {
		return this.unit;
	}

	/**
	 * Generates the ordinates of each X value given. Every Y value is extrapolated with this fit.
	 *
	 * @param valuesX X values to generate the Y values from
	 * @return array containing all extrapolated X values
	 */
	public double[] generateOrdinates(double[] valuesX) {
		double[] ordinates = new double[valuesX.length];
		for (int i = 0; i < valuesX.length; i++)
			ordinates[i] = this.extrapolateY(valuesX[i]);
		return ordinates;
	}

	/**
	 * @return series for the selected fit of the graph
	 */
	public XYSeries generateFittedSeries(double[] xValues) {
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
	 * Type of a fit.
	 *
	 * @author Titouan QUÉMA
	 */
	public enum FitType {
		NONE("No Fit"), LINEAR("Linear"), EXPONENTIAL("Exponential");
		private final String s;

		FitType(String s) {
			this.s = s;
		}

		/**
		 * Representation of the form:
		 * <p><code>NAME</code></p>
		 * where the capitalized word is replaced with its value.
		 */
		@Override
		public String toString() {
			return s;
		}
	}

	/**
	 * Linear extrapolation.
	 *
	 * @author Titouan QUÉMA
	 * @see Regression#getOLSRegression
	 */
	public static class LinearFit extends Fit {

		private final double[] coefs;

		public LinearFit(double[][] dataset, Unit unit) {
			super(FitType.LINEAR, unit);
			this.coefs = Regression.getOLSRegression(dataset);
		}

		@Override
		public double extrapolateX(double valueY) {
			return (valueY - coefs[0]) / coefs[1];
		}

		@Override
		public double extrapolateY(double valueX) {
			return coefs[1] * valueX + coefs[0];
		}

	}

	/**
	 * Exponential extrapolation.
	 *
	 * @author Titouan QUÉMA
	 * @see Regression#getPowerRegression
	 */
	public static class ExponentialFit extends Fit {

		private final double[] coefs;

		public ExponentialFit(double[][] dataset, Unit unit) {
			super(FitType.EXPONENTIAL, unit);
			double[][] lnSeries = new double[dataset.length][2];
			for (int i = 0; i < dataset.length; i++) {
				lnSeries[i][0] = dataset[i][0];
				lnSeries[i][1] = Math.log(dataset[i][1]);
			}
			this.coefs = Regression.getOLSRegression(lnSeries);
			this.coefs[0] = Math.exp(coefs[0]);
		}

		@Override
		public double extrapolateX(double valueY) {
			return (Math.log(valueY) - Math.log(coefs[0])) / coefs[1];
		}

		@Override
		public double extrapolateY(double valueX) {
			return coefs[0] * Math.exp(coefs[1] * valueX);
		}

	}

	/**
	 * No extrapolation.
	 *
	 * @author Titouan QUÉMA
	 */
	public static class NoFit extends Fit {

		public NoFit(Unit unit) {
			super(FitType.NONE, unit);
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