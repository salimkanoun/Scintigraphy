package org.petctviewer.scintigraphy.scin;

import ij.ImageListener;
import ij.ImagePlus;

public class ControleurImp implements ImageListener {
	
	private Controleur_OrganeFixe ctrlScin;
	private int lastSlice = 1;
	private boolean lockUpdate = false;
	
	/**
	 * met a jour l'overlay de l'imp si elle est modifiee
	 * @param ctrlScin
	 */
	public ControleurImp(Controleur_OrganeFixe ctrlScin) {
		this.ctrlScin = ctrlScin;
	}
	
	@Override
	public void imageUpdated(ImagePlus imp) {
		if(!this.lockUpdate) {
			int currentSlice = this.ctrlScin.getScin().getImp().getCurrentSlice();
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
