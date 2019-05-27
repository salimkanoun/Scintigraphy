package org.petctviewer.scintigraphy.gastric;

public enum Unit {
	PERCENTAGE("%"),
	TIME("h:m:s"),
	MINUTES("min"),
	COUNTS("counts"),
	COUNTS_PER_SECOND("counts/sec"),
	COUNTS_PER_MINUTE("counts/min"),
	KCOUNTS("kcounts"),
	KCOUNTS_PER_SECOND("kcounts/sec"),
	KCOUNTS_PER_MINUTE("kcounts/min"),
	COUNTS_PER_PIXEL("counts/pixel"),
	KCOUNTS_PER_PIXEL("kcounts/pixel");

	private final String abbreviation;

	Unit(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String abrev() {
		return abbreviation;
	}

	public double convertTo(double value, Unit unit) {
		if (this == unit) return value;

		switch (this) {
			case COUNTS:
				if (unit == Unit.KCOUNTS) {
					return value / 1000.;
				}
				break;
			case KCOUNTS:
				if (unit == Unit.COUNTS) {
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
				if (unit == Unit.KCOUNTS_PER_PIXEL) {
					return value / 1000.;
				}
				break;
			case KCOUNTS_PER_PIXEL:
				if (unit == Unit.COUNTS_PER_PIXEL) {
					return value * 1000.;
				}
				break;
			case MINUTES:
				if (unit == TIME) return value;
			case TIME:
				if (unit == MINUTES) return value;
		}
		throw new UnsupportedOperationException("This unit (" + this + ") cannot be converted to " + unit);
	}

	@Override
	public String toString() {
		return this.abbreviation;
	}
}