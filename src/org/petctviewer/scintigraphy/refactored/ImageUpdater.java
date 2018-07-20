package org.petctviewer.scintigraphy.refactored;

import ij.ImageListener;
import ij.ImagePlus;

public class ImageUpdater implements ImageListener {
	
	private int lastSlice = 1;
	private boolean lockUpdate = false;
	private Modele modele;
	
	/**
	 * met a jour l'overlay de l'imp si elle est modifiee
	 * @param ctrlScin
	 */
	public ImageUpdater(Modele modele) {
		this.modele = modele;
	}
	
	@Override
	public void imageUpdated(ImagePlus imp) {
		if(!this.lockUpdate) {
			int currentSlice = this.modele.getImp().getCurrentSlice();
			if(currentSlice != this.lastSlice) {
				this.lastSlice = currentSlice;
				this.lockUpdate = true;
				this.modele.setSlice(currentSlice);
				this.lockUpdate = false;
			}
		}
	}
	
	@Override
	public void imageOpened(ImagePlus imp) {
		//inutile
	}
	
	@Override
	public void imageClosed(ImagePlus imp) {
		//inutile
	}
}
