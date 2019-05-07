package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;

import ij.gui.Roi;

public class Region {
	
	private ImageSelection ims;
	private ImageState state;
	private String name;
	private Roi roi;
	
	public Region(String name) {
		this.name = name;
	}
	
	public void inflate(ImageSelection ims, ImageState state, Roi roi) {
		this.ims = ims;
		this.roi = roi;
		this.state = state;
	}
	
	public Roi getRoi() {
		return this.roi;
	}
	
	public ImageSelection getImage() {
		return this.ims;
	}
	
	public ImageState getState() {
		return this.state;
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

}
