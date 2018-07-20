package org.petctviewer.scintigraphy.calibration;

import java.util.ArrayList;

public class ControleurCalibration {

	ModeleCalibration modele ;
	
	public ControleurCalibration(ArrayList<String[]> examList) {
		this.modele = new ModeleCalibration(examList);
		
	}
	
}
