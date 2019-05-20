package org.petctviewer.scintigraphy.gastric;

public enum Unit {
		PERCENTAGE("%"),
		TIME("h:m:s"),
		COUNTS("counts"),
		COUNTS_PER_SECOND("counts/sec"),
		COUNTS_PER_MINUTE("counts/min"),
		KCOUNTS("kcounts"),
		KCOUNTS_PER_SECOND("kcounts/sec"),
		KCOUNTS_PER_MINUTE("kcounts/min");

		private String s;

		private Unit(String s) {
			this.s = s;
		}
		
		public String abrev() {
			return s;
		}

		public double convertTo(double value, Unit unit) {
			if (this == unit)
				return value;

			switch (this) {
			case COUNTS:
				if (unit == KCOUNTS)
					return value / 1000.;
				else if (unit == COUNTS)
					return value;
				else
					throw new UnsupportedOperationException("This unit cannot be converted to " + unit);
			case KCOUNTS:
				if (unit == KCOUNTS)
					return value;
				else if (unit == COUNTS)
					return value * 1000.;
				else
					throw new UnsupportedOperationException("This unit cannot be converted to " + unit);
			case COUNTS_PER_MINUTE:
				switch (unit) {
				case COUNTS_PER_MINUTE:
					return value;
				case COUNTS_PER_SECOND:
					return value / 60.;
				case KCOUNTS_PER_MINUTE:
					return value / 1000.;
				case KCOUNTS_PER_SECOND:
					return value / 1000. / 60.;
				default:
					throw new UnsupportedOperationException("This unit cannot be converted to " + unit);
				}
			case COUNTS_PER_SECOND:
				switch (unit) {
				case COUNTS_PER_MINUTE:
					return value * 60.;
				case COUNTS_PER_SECOND:
					return value;
				case KCOUNTS_PER_MINUTE:
					return value / 1000. * 60.;
				case KCOUNTS_PER_SECOND:
					return value / 1000.;
				default:
					throw new UnsupportedOperationException("This unit cannot be converted to " + unit);
				}
			case KCOUNTS_PER_MINUTE:
				switch (unit) {
				case COUNTS_PER_MINUTE:
					return value * 1000.;
				case COUNTS_PER_SECOND:
					return value * 1000. * 60.;
				case KCOUNTS_PER_MINUTE:
					return value;
				case KCOUNTS_PER_SECOND:
					return value * 60.;
				default:
					throw new UnsupportedOperationException("This unit cannot be converted to " + unit);
				}
			case KCOUNTS_PER_SECOND:
				switch (unit) {
				case COUNTS_PER_MINUTE:
					return value * 1000. / 60.;
				case COUNTS_PER_SECOND:
					return value * 1000.;
				case KCOUNTS_PER_MINUTE:
					return value / 60.;
				case KCOUNTS_PER_SECOND:
					return value;
				default:
					throw new UnsupportedOperationException("This unit cannot be converted to " + unit);
				}
			default:
				throw new UnsupportedOperationException("This unit (" + this + ") cannot be converted");
			}
		}

		@Override
		public String toString() {
			return this.s;
		}
	}