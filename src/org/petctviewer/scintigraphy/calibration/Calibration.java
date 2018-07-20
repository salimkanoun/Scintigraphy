package org.petctviewer.scintigraphy.calibration;

import org.petctviewer.scintigraphy.calibration.chargement.FenChargementCalibration;

import ij.plugin.PlugIn;

public class Calibration implements PlugIn{

	@Override
	public void run(String arg0) {
		/*
		try {
		    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		 } catch (Exception e) {
		            e.printStackTrace();
		 }
		*/
		FenChargementCalibration fen = new FenChargementCalibration();
		fen.setVisible(true);
	}
}
