package org.petctviewer.scintigraphy.liver;

import java.util.HashMap;
import org.petctviewer.scintigraphy.dynamic.Modele_Dynamic;

public class Modele_Liver extends Modele_Dynamic {

	public Modele_Liver(int[] frameDuration) {
		super(frameDuration);
	}

	@Override
	public void calculerResultats() {
		
	}

	@Override
	public HashMap<String, String> getResultsHashMap() {
		return null;
	}
	
	@Override
	public String toString() {
		String s = super.toString();
		return s;
	}

}
