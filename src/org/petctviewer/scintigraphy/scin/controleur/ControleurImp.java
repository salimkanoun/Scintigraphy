package org.petctviewer.scintigraphy.scin.controleur;

import ij.ImageListener;
import ij.ImagePlus;

public class ControleurImp implements ImageListener {
	
	private ControleurScin ctrlScin;
	private int lastSlice = 1;
	private boolean lockUpdate = false;
	
	private int nbContamination;
	
	public ControleurImp(ControleurScin ctrlScin) {
		this.ctrlScin = ctrlScin;
	}
	
	@Override
	public void imageUpdated(ImagePlus imp) {
		if(!this.lockUpdate) {
			int currentSlice = this.ctrlScin.getCurrentSlice();
			if(currentSlice != lastSlice) {
				this.lastSlice = currentSlice;
				this.lockUpdate = true;
				this.ctrlScin.showSliceWithOverlay(currentSlice);
				this.lockUpdate = false;
			}
		}
	}
	
	@Override
	public void imageOpened(ImagePlus imp) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void imageClosed(ImagePlus imp) {
		// TODO Auto-generated method stub
		
	}
}
