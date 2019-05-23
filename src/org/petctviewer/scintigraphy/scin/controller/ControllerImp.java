package org.petctviewer.scintigraphy.scin.controller;

import ij.ImageListener;
import ij.ImagePlus;

public class ControllerImp implements ImageListener {
	
	private Controller_OrganeFixe ctrlScin;
	private int lastSlice = 1;
	private boolean lockUpdate = false;
	
	/**
	 * met a jour l'overlay de l'imp si elle est modifiee
	 * @param ctrlScin
	 */
	public ControllerImp(Controller_OrganeFixe ctrlScin) {
		this.ctrlScin = ctrlScin;
	}
	
	@Override
	public void imageUpdated(ImagePlus imp) {
		if(!this.lockUpdate) {
			int currentSlice = this.ctrlScin.model.getImagePlus().getCurrentSlice();
			if(currentSlice != this.lastSlice) {
				this.lastSlice = currentSlice;
				this.lockUpdate = true;
				this.ctrlScin.setSlice(currentSlice);
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
