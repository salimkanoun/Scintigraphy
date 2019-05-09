package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.instructions.ImageState;

import ij.gui.Roi;

public class Region {
	
	private ImageState state;
	private String name;
	private Roi roi;
	
	public Region(String name) {
		this.name = name;
	}
	
	public void inflate(ImageState state, Roi roi) {
		this.roi = roi;
		this.state = state;
	}
	
	public Roi getRoi() {
		return this.roi;
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
