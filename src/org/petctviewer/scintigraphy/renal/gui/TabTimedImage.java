package org.petctviewer.scintigraphy.renal.gui;

import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.PanelImpContrastSlider;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.process.ImageProcessor;

class TabTimedImage extends PanelImpContrastSlider {

	public TabTimedImage(RenalScintigraphy vue, int rows, int columns, FenResults parent) {
		super("Renal scintigraphy", vue, "timed", parent);
	
		ImagePlus montage = Library_Capture_CSV.creerMontage(vue.getFrameDurations(), vue.getImpPost(), 200, rows, columns);
		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		
		this.setImp(montage);
	}
}
