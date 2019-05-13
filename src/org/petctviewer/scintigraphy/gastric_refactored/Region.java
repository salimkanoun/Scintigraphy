package org.petctviewer.scintigraphy.gastric_refactored;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.petctviewer.scintigraphy.scin.instructions.ImageState;

import ij.gui.Roi;

public class Region {

	private ImageState state;
	private String name;
	private Roi roi;

	private Map<Integer, Double> data;

	private Model_Gastric model;

	public Region(String name, Model_Gastric model) {
		this.name = name;
		this.data = new HashMap<>();
		this.state = null;
		this.roi = null;
		this.model = model;
	}

	public void inflate(ImageState state, Roi roi) {
		this.roi = roi;
		this.state = state;
	}

	public void setValue(int key, double value) {
		this.data.put(key, value);
	}

	public Double getValue(int key) {
		return this.data.get(key);
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
		StringBuilder s = new StringBuilder();

		s.append("Region [" + this.name + "]\n");
		s.append("| ImageState: " + this.state + " |\n");
		s.append("| Stored values:\n");
		for (Entry<Integer, Double> entry : this.data.entrySet())
			s.append("\t- " + this.model.nameOfDataField(entry.getKey()) + " => " + entry.getValue() + "\n");

		return s.toString();
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
	
	@Override
	public Region clone() {
		Region clone = new Region(name, model);
		clone.inflate(state.clone(), roi);
		clone.data = new HashMap<>();
		for(Entry<Integer, Double> entry : this.data.entrySet())
			clone.data.put(entry.getKey(), entry.getValue());
		return clone;
	}

}
