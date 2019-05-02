package org.petctviewer.scintigraphy.gastric_refactored.gui;

import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.DefaultXYDataset;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public abstract class Fit {

	public enum FitType {
		NONE("No Fit"), LINEAR("Linear"), EXPONENTIAL("Exponential"), POLYNOMIAL("Polynomial");
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

	public static Fit createFit(FitType type, double[][] dataset) {
		switch (type) {
		case LINEAR:
			return new LinearFit(dataset);
		case EXPONENTIAL:
			return new ExponentialFit(dataset);
		case POLYNOMIAL:
			return new PolynomialFit(dataset);
		default:
			return new NoFit();
		}
	}

	public abstract double extrapolateX(double valueY);

	public abstract double extrapolateY(double valueX);

	public double[] generateOrdinates(double[] valuesX) {
		double[] ordinates = new double[valuesX.length];
		for (int i = 0; i < valuesX.length; i++)
			ordinates[i] = this.extrapolateY(valuesX[i]);
		return ordinates;
	}
	
	public FitType getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		return this.type.s;
	}

	public static class LinearFit extends Fit {

		private double[] coefs;

		public LinearFit(double[][] dataset) {
			super(FitType.LINEAR);
			this.coefs = Regression.getOLSRegression(dataset);
		}

		@Override
		public double extrapolateX(double valueY) {
			double res = valueY - coefs[0] / coefs[1];
			System.out.println("Value X for " + valueY + " with Linear interpolation -> " + res);
			return res;
		}

		@Override
		public double extrapolateY(double valueX) {
			double res = coefs[1] * valueX + coefs[0];
			System.out.println("Value Y for " + valueX + " with Linear interpolation -> " + res);
			return res;
		}

	}

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
			System.out.println("Value X for " + valueY + " with Exponential interpolation -> " + res);
			return res;
		}

		@Override
		public double extrapolateY(double valueX) {
			double res = Math.exp(coefs[0]) * Math.exp(coefs[1] * valueX);
			System.out.println("Value Y for " + valueX + " with Exponential interpolation -> " + res);
			return res;
		}

	}

	public static class PolynomialFit extends Fit {

		private double[] coefs;

		public PolynomialFit(double[][] dataset) {
			super(FitType.POLYNOMIAL);
			DefaultXYDataset ds = new DefaultXYDataset();
			ds.addSeries("Polynomial", Library_JFreeChart.invertArray(dataset));
			this.coefs = Regression.getPolynomialRegression(ds, 0, 3);
		}

		@Override
		public double extrapolateX(double valueY) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double extrapolateY(double valueX) {
			double res = coefs[0];
			for (int i = 1; i < coefs.length - 1; i++)
				res += Math.pow(valueX, i) * coefs[i];
			System.out.println("Value Y for " + valueX + " with Polynomial interpolation -> " + res);
			return res;
		}

	}

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