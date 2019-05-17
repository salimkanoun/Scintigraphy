package org.petctviewer.scintigraphy.gastric_refactored;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;

/**
 * This class represents a result returned by a model.<br>
 * A result is divided in 3 parts:<br>
 * <code>Mass_sun = 1.98E30 kg</code>
 * <ul>
 * <li><b><code>Mass_sun</code></b>: the result type</li>
 * <li><b><code>1.98E30</code></b>: the value</li>
 * <li><b><code>kg</code></b>: the unit</li>
 * </ul>
 * In almost all cases, the value is linearly interpolated between two known
 * points of the graph. If one of the points is missing, the the value is
 * extrapolated (see {@link #isExtrapolated()}).
 * 
 * @author Titouan QUÉMA
 *
 */
public class ResultValue {
	private Result type;
	private double value;
	private Unit unit;
	private FitType extrapolation;

	/**
	 * Instantiates a new result.<br>
	 * This constructor should be used when the extrapolation of the value needs to
	 * be specified.
	 *
	 * @see #ResultValue(Result, double, Unit)
	 * @param type          Result type
	 * @param value         Value of the result
	 * @param unit          Unit of the value
	 * @param extrapolation type of the extrapolation (null means no extrapolation)
	 */
	public ResultValue(Result type, double value, Unit unit, FitType extrapolation) {
		if (type == null)
			throw new IllegalArgumentException("Result type cannot be null");
		if (unit == null)
			throw new IllegalArgumentException("Unit cannot be null");
		this.type = type;
		this.value = value;
		this.extrapolation = extrapolation;
		this.unit = unit;
	}

	/**
	 * Instantiates a new result with no extrapolation.
	 * 
	 * @param type  Result type
	 * @param value Value of the result
	 * @param unit  Unit of the value
	 */
	public ResultValue(Result type, double value, Unit unit) {
		this(type, value, unit, null);
	}

	/**
	 * Converts this result to a new unit.
	 * 
	 * @param newUnit Unit to convert this result to
	 */
	public void convert(Unit newUnit) {
		this.value = this.unit.convertTo(this.value, newUnit);
		this.unit = newUnit;
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
		return this.type;
	}

	/**
	 * If no extrapolation is defined, then this method returns null.
	 * 
	 * @return type of the extrapolation of this value (null if none)
	 */
	public FitType getExtrapolation() {
		return this.extrapolation;
	}

	/**
	 * @return TRUE if the value is extrapolated from a fit (see
	 *         {@link #getExtrapolation()} to know which fit is used) and FALSE if
	 *         the value is linearly extrapolated between two known points
	 */
	public boolean isExtrapolated() {
		return this.extrapolation != null;
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
	public String value() {
		if (this.unit == Unit.TIME)
			return this.displayAsTime();
		return this.notNegative();
	}

	/**
	 * Returns the value of this result rounded at 2 decimals and set to 0 if
	 * negative.<br>
	 * If this result is extrapolated, then a star '(*)' is added at the end of the
	 * result.
	 * 
	 * @return rounded value for this result (2 decimals) restrained to 0 if
	 *         negative
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

		if (this.isExtrapolated())
			s.append("(*)");

		return s.toString();
	}
}