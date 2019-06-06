package org.petctviewer.scintigraphy.scin.model;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.platelet_refactored.Data;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a area in an image. Data resulting of calculations can
 * be stored in this region.
 * 
 * @author Titouan QUÉMA
 *
 */
public class Region {

	private ImageState state;
	private final String name;
	private Roi roi;

	private Map<Integer, Double> values;

	/**
	 * Instantiates a new region with the specified name and the associated model.
	 * 
	 * @param name  Name of the region (names can be identical though it is not
	 *              recommended)
	 */
	public Region(String name) {
		this.name = name;
		this.values = new HashMap<>();
		this.state = null;
		this.roi = null;
	}

	/**
	 * Adds a ROI along with the state the image should have to get informations.
	 * 
	 * @param state State the image should be in to get information from the ROI
	 * @param roi   ROI of the region
	 */
	public void inflate(ImageState state, Roi roi) {
		this.roi = roi;
		this.state = state;
	}

	/**
	 * Changes the value with the specified key. The keys are stored by the model.
	 * 
	 * @param key   Key of the value to change
	 * @param value Value to set
	 */
	public void setValue(int key, double value) {
		this.values.put(key, value);
	}

	/**
	 * Removes the value associated with the specified key.<br>
	 * If no value was associated with this key, nothing happens.
	 * 
	 * @param key Key of the value to remove
	 */
	public void removeValue(int key) {
		this.values.remove(key);
	}

	/**
	 * Gets the value associated with the specified key.F
	 * 
	 * @param key Key of the value to get
	 * @return value associated with the key or null if none
	 */
	public Double getValue(int key) {
		return this.values.get(key);
	}

	/**
	 * @return ROI corresponding to this region or null if none
	 */
	public Roi getRoi() {
		return this.roi;
	}

	/**
	 * Returns the state the image should be in in order to get informations about
	 * the ROI associated.
	 * 
	 * @return state the image or null if none
	 */
	public ImageState getState() {
		return this.state;
	}

	/**
	 * @return name of this region
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("Region [").append(this.name).append("]\n");
		s.append("| ImageState: ").append(this.state).append(" |\n");
		s.append("| Stored values:\n");
		for (Entry<Integer, Double> entry : this.values.entrySet())
			s.append("\t- ").append(Data.nameOfDataField(entry.getKey())).append(" => ").append(entry.getValue())
					.append("\n");

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
			return other.name == null;
		} else return name.equals(other.name);
	}

	@Override
	public Region clone() {
		Region clone = new Region(name);
		clone.inflate(state.clone(), roi);
		clone.values = new HashMap<>();
		for (Entry<Integer, Double> entry : this.values.entrySet())
			clone.values.put(entry.getKey(), entry.getValue());
		return clone;
	}

}
