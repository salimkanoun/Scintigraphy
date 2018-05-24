package org.petctviewer.scintigraphy.dynamic;

import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

public class Modele_GeneralDyn extends ModeleScinDyn {

	public Modele_GeneralDyn(int[] frameDuration) {
		super(frameDuration);
	}

	@Override
	public void calculerResultats() {
		for (String k : this.getData().keySet()) {
			List<Double> data = this.getData().get(k);
			this.getData().put(k, this.adjustValues(data));
		}
	}

	@Override
	public HashMap<String, String> getResultsHashMap() {
		return null;
	}

}
