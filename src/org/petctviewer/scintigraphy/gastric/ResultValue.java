package org.petctviewer.scintigraphy.gastric;

import org.petctviewer.scintigraphy.gastric.Fit.FitType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class represents a result returned by a model.<br>
 * A result is divided in 3 parts:<br>
 * <code>Mass_sun = 1.98E30 kg</code>
 * <ul>
 * <li><b><code>Mass_sun</code></b>: the result type</li>
 * <li><b><code>1.98E30</code></b>: the value</li>
 * <li><b><code>kg</code></b>: the unit</li>
 * </ul>
 * In most cases, the value is linearly interpolated between two known points of
 * the graph. If one of the points is missing, the the value is extrapolated
 * (see {@link #isExtrapolated()}).
 *
 * @author Titouan QUÉMA
 */
public class ResultValue {
	private ResultRequest request;
	private double value;
	private boolean isExtrapolated;
	private Unit unit;

	/**
	 * Instantiates a new result.<br>
	 * This constructor should be used when the extrapolation of the value needs to
	 * be specified.
	 *
	 * @param request        Request this result answers to
	 * @param value          Value of the result
	 * @param isExtrapolated if TRUE then this result has an extrapolated value and
	 *                       if set to FALSE, this value is interpolated or exact
	 * @see #ResultValue(ResultRequest, double, Unit, boolean)
	 */
	public ResultValue(ResultRequest request, double value, Unit unit, boolean isExtrapolated) {
		if (request == null)
			throw new IllegalArgumentException("Associated request cannot be null");

		this.request = request;
		this.value = value;
		this.isExtrapolated = isExtrapolated;

		this.unit = unit;
	}

	/**
	 * Instantiates a new result not extrapolated.<br>
	 * This constructor is a convenience for
	 * {@link #ResultValue(ResultRequest, double, Unit, boolean)}.
	 *
	 * @param request Request this result answers to
	 * @param value   Value of the result
	 */
	public ResultValue(ResultRequest request, double value, Unit unit) {
		this(request, value, unit, false);
	}

	/**
	 * Converts this result to a new unit.
	 *
	 * @param newUnit Unit to convert this result to
	 */
	public void convert(Unit newUnit) {
		try {
			this.value = this.unit.convertTo(this.value, newUnit);
			this.unit = newUnit;
		} catch (UnsupportedOperationException e) {
			throw new UnsupportedOperationException("For result " + this.request.getResultOn().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * @return unit of this result
	 */
	public Unit getUnit() {
		return this.unit;
	}

	/**
	 * @return type of the result
	 */
	public Result getResultType() {
		return this.request.getResultOn();
	}

	/**
	 * If no extrapolation is defined, then this method returns null.
	 *
	 * @return type of the extrapolation of this value (null if none)
	 */
	public FitType getExtrapolation() {
		if (this.isExtrapolated)
			return this.request.getFit().getType();
		return null;
	}

	/**
	 * @return TRUE if the value is extrapolated from a fit (see
	 * {@link #getExtrapolation()} to know which fit is used) and FALSE if
	 * the value is linearly extrapolated between two known points or exact
	 */
	public boolean isExtrapolated() {
		return this.isExtrapolated;
	}

	/**
	 * Formats the value of this result into a readable one.<br>
	 * This method will display this value according to its unit. For instance, if
	 * this unit is {@link Unit#TIME}, then the value will be represented with the
	 * method {@link #displayAsTime()}. If the unit is not time, then this method is
	 * equivalent to {@link #notNegative()}.
	 * <p>
	 * This method displays only the value of this result (and not the unit nor the
	 * type).
	 * <p>
	 * If this result is extrapolated, then a star '(*)' is added at the end of the
	 * result.
	 *
	 * @return formatted value for this result
	 */
	public String formatValue() {
		if (this.unit == Unit.TIME)
			return this.displayAsTime();
		return this.notNegative();
	}

	public double getValue() {
		return this.value;
	}

	/**
	 * Returns the value of this result rounded at 2 decimals and set to 0 if
	 * negative.<br>
	 * If this result is extrapolated, then a star '(*)' is added at the end of the
	 * result.
	 *
	 * @return rounded value for this result (2 decimals) restrained to 0 if
	 * negative
	 */
	public String notNegative() {
		return BigDecimal.valueOf(Math.max(0, value)).setScale(2, RoundingMode.HALF_UP).toString()
				+ (isExtrapolated() ? "(*)" : "");
	}

	/**
	 * Returns the value of this result rounded at 2 decimals.<br>
	 * If this result is extrapolated, then a star '(*)' is added at the end of the
	 * result.
	 *
	 * @return rounded value for this result (2 decimals)
	 */
	public String roundedValue() {
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toString() + (isExtrapolated() ? "(*)" : "");
	}

	// TODO: do not assume the time is in minutes

	/**
	 * Returns the value of this result as a time considering the value in
	 * minutes.<br>
	 * If this result is extrapolated, then a star '(*)' is added at the end of the
	 * result.
	 *
	 * @return value of this result formatted as time: <code>HH:mm:ss</code>
	 */
	public String displayAsTime() {
		StringBuilder s = new StringBuilder(displayAsTime(this.value));

		if (this.isExtrapolated())
			s.append("(*)");

		return s.toString();
	}

	public static String displayAsTime(double value) {
		int seconds = (int) ((value - (double) ((int) value)) * 60.);
		int minutes = (int) (value % 60.);
		int hours = (int) (value / 60.);

		StringBuilder s = new StringBuilder();
		// Hours
		if (hours < 10)
			s.append(0);
		s.append(hours);
		s.append(':');

		// Minutes
		if (minutes < 10)
			s.append(0);
		s.append(minutes);
		s.append(':');

		// Seconds
		if (seconds < 10)
			s.append(0);
		s.append(seconds);

		return s.toString();
	}
}