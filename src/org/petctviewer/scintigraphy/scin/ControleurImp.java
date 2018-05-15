package org.petctviewer.scintigraphy.scin;

import ij.ImageListener;
import ij.ImagePlus;

public class ControleurImp implements ImageListener {
	
	private ControleurScin ctrlScin;
	private int lastSlice = 1;
	private boolean lockUpdate = false;
	
	/**
	 * met a jour l'overlay de l'imp si elle est modifiee
	 * @param ctrlScin
	 */
	public ControleurImp(ControleurScin ctrlScin) {
		this.ctrlScin = ctrlScin;
	}
	
	@Override
	public void imageUpdated(ImagePlus imp) {
		if(!this.lockUpdate) {
			int currentSlice = this.ctrlScin.getVue().getImp().getCurrentSlice();
			if(currentSlice != lastSlice) {
				this.lastSlice = currentSlice;
				this.lockUpdate = true;
				this.ctrlScin.setSlice(currentSlice);
				this.lockUpdate = false;
			}
		}
	}
	
	@Override
	public void imageOpened(ImagePlus imp) {
		
	}
	
	@Override
	public void imageClosed(ImagePlus imp) {
		
	}
}
