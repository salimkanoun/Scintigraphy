package org.petctviewer.scintigraphy.renal.gui;

import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.PanelResultatImp;

import ij.ImagePlus;
import ij.process.ImageProcessor;

class TabTimedImage extends PanelResultatImp{

	private static final long serialVersionUID = 8125367912250906052L;

	public TabTimedImage(RenalScintigraphy vue, int rows, int columns) {
		super("Renal scintigraphy", vue, "timed");
	
		ImagePlus montage = Scintigraphy.creerMontage(vue.getFrameDurations(), vue.getImp(), 200, rows, columns);
		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		
		this.setImp(montage);
	}
}
