package org.petctviewer.scintigraphy.calibration;

import javax.swing.UIManager;

import org.petctviewer.scintigraphy.calibration.chargement.FenChargementCalibration;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImagePlus;
import ij.io.Opener;
import ij.plugin.PlugIn;
import io.scif.img.ImgOpener;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.io.SCIFIO_Reader;

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
