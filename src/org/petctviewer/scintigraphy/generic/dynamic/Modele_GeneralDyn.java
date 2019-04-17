 package org.petctviewer.scintigraphy.generic.dynamic;

import java.util.List;

import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

public class Modele_GeneralDyn extends ModeleScinDyn {

	public Modele_GeneralDyn(String studyName, int[] frameDuration) {
		super(null, studyName, frameDuration);
	}

	@Override
	public void calculerResultats() {
		for (String k : this.getData().keySet()) {
			List<Double> data = this.getData().get(k);
			this.getData().put(k, this.adjustValues(data));
		}
	}

	@Override
	public String toString() {
		String s = "\n";

		for (String k : this.getData().keySet()) {
			s += k;
			for (Double d : this.getData().get(k)) {
				s += "," + d;
			}
			s += "\n";
		}
		
		return s;
	}

}
