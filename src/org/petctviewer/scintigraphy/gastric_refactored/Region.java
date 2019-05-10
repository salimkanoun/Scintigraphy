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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Region other = (Region) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
