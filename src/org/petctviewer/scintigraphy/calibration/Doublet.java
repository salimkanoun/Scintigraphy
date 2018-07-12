package org.petctviewer.scintigraphy.calibration;

public class Doublet {
		private Double a;
		private Double b;
		
		public Doublet(Double a, Double b) {
			this.a = a;
			this.b = b;
		}
		
		public Double getA() {
			return a;
		}
		
		public Double getB() {
			return b;
		}
		
		public void setA(Double a) {
			this.a = a;
		}
		
		public void setB(Double b) {
			this.b = b;
		}
	}