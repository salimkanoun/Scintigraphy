package org.petctviewer.scintigraphy.gastric;

public enum Unit {
	PERCENTAGE("%"),
	TIME("h:m:s"),
	COUNTS("counts"),
	COUNTS_PER_SECOND("counts/sec"),
	COUNTS_PER_MINUTE("counts/min"),
	KCOUNTS("kcounts"),
	KCOUNTS_PER_SECOND("kcounts/sec"),
	KCOUNTS_PER_MINUTE("kcounts/min"),
	COUNTS_PER_PIXEL("counts/pixel"),
	KCOUNTS_PER_PIXEL("kcounts/pixel");

	private String s;

	private Unit(String s) {
		this.s = s;
	}

	public String abrev() {
		return s;
	}

	@SuppressWarnings("incomplete-switch")
	public double convertTo(double value, Unit unit) {
		if (this == unit)
			return value;

		switch (this) {
		case COUNTS:
			switch (unit) {
			case KCOUNTS:
				return value / 1000.;
			}
			break;
		case KCOUNTS:
			switch (unit) {
			case COUNTS:
				return value * 1000.;
			}
			break;
		case COUNTS_PER_MINUTE:
			switch (unit) {
			case COUNTS_PER_SECOND:
				return value / 60.;
			case KCOUNTS_PER_MINUTE:
				return value / 1000.;
			case KCOUNTS_PER_SECOND:
				return value / 1000. / 60.;
			}
			break;
		case COUNTS_PER_SECOND:
			switch (unit) {
			case COUNTS_PER_MINUTE:
				return value * 60.;
			case KCOUNTS_PER_MINUTE:
				return value / 1000. * 60.;
			case KCOUNTS_PER_SECOND:
				return value / 1000.;
			}
			break;
		case KCOUNTS_PER_MINUTE:
			switch (unit) {
			case COUNTS_PER_MINUTE:
				return value * 1000.;
			case COUNTS_PER_SECOND:
				return value * 1000. * 60.;
			case KCOUNTS_PER_SECOND:
				return value * 60.;
			}
			break;
		case KCOUNTS_PER_SECOND:
			switch (unit) {
			case COUNTS_PER_MINUTE:
				return value * 1000. / 60.;
			case COUNTS_PER_SECOND:
				return value * 1000.;
			case KCOUNTS_PER_MINUTE:
				return value / 60.;
			}
			break;
		case COUNTS_PER_PIXEL:
			switch (unit) {
			case KCOUNTS_PER_PIXEL:
				return value / 1000.;
			}
			break;
		case KCOUNTS_PER_PIXEL:
			switch (unit) {
			case COUNTS_PER_PIXEL:
				return value * 1000.;
			}
			break;
		}
		throw new UnsupportedOperationException("This unit (" + this + ") cannot be converted to " + unit);
	}

	@Override
	public String toString() {
		return this.s;
	}
}