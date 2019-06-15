package org.petctviewer.scintigraphy.scin.model;


import ij.gui.Roi;
import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores the data measured or calculated for each region of this model. It is associated with an
 * ImageSelection.
 *
 * @author Titouan QUÉMA
 */
public class Data {

	// Keys from 0 -> 1000 are reserved for this class, keys from 1000 are available for each program
	public static final int DATA_COUNTS = 0;
	public static final int DATA_COUNTS_CORRECTED = 1;
	public static final int DATA_GEO_AVG = 2;

	public static final int DATA_MEAN_COUNTS = 3;
	public static final int DATA_MEAN_GEO_AVG = 4;

	public static final int DATA_BKG_NOISE = 5;

	public static final int DATA_PIXEL_COUNTS = 6;
	public static final int DATA_DECAY_CORRECTED = 7;

	public static final int DATA_PERCENTAGE = 8;
	public static final int DATA_DERIVATIVE = 9;
	public static final int DATA_CORRELATION = 10;

	public static final int DATA_MAX_VALUE = 1000;

	private final Map<String, Region> regionsAnt;
	private final Map<String, Region> regionsPost;

	private ImageState state;
	private double time;

	public Data(ImageState state, double time) {
		this.state = state;
		this.time = time;
		this.regionsAnt = new HashMap<>();
		this.regionsPost = new HashMap<>();
	}

	/**
	 * Converts the key of the data field into a readable name.
	 *
	 * @param key Key of the data field
	 * @return readable string of the key
	 */
	public static String nameOfDataField(int key) {
		switch (key) {
			case Data.DATA_COUNTS:
				return "Nb counts";
			case DATA_COUNTS_CORRECTED:
				return "Nb counts (corrected with background noise)";
			case DATA_MEAN_COUNTS:
				return "Mean counts";
			case Data.DATA_GEO_AVG:
				return "Geo-avg";
			case Data.DATA_PERCENTAGE:
				return "Percentage";
			case Data.DATA_DERIVATIVE:
				return "Derivative";
			case Data.DATA_CORRELATION:
				return "Correlation";
			case Data.DATA_PIXEL_COUNTS:
				return "Pixel counts";
			case Data.DATA_BKG_NOISE:
				return "Background Noise";
			default:
				return "???";
		}
	}

	/**
	 * Associates a value to the specified key for the region defined.<br> If the region doesn't exist, then an
	 * exception is thrown.
	 *
	 * @param regionName Name of the region to insert the value into
	 * @param key        Key to store the value
	 * @param value      Value to store
	 * @throws IllegalArgumentException if no region exists with this region name
	 */
	public void setAntValue(String regionName, int key, double value) {
		Region region = this.regionsAnt.get(regionName);
		if (region == null) throw new IllegalArgumentException("The region (" + regionName + ") doesn't exist");

		// Set value
		region.setValue(key, value);
	}

	/**
	 * Associates a value to the specified key for the region defined.<br> If the region doesn't exist, then it is
	 * created.
	 *
	 * @param regionName Name of the region to insert the value into
	 * @param key        Key to store the value
	 * @param value      Value to store
	 * @param state      State of the image (used to create region)
	 * @param roi        Roi of the region (used to create region)
	 */
	public void setAntValue(String regionName, int key, double value, ImageState state, Roi roi) {
		Region region = this.regionsAnt.get(regionName);
		if (region == null) {
			// Create region
			region = new Region(regionName);
			region.inflate(state, roi);
			this.regionsAnt.put(regionName, region);
		}

		// Set value
		region.setValue(key, value);
	}

	/**
	 * Associates a value to the specified key for the region defined.<br> If the region doesn't exist, then an
	 * exception is thrown.
	 *
	 * @param regionName Name of the region to insert the value into
	 * @param key        Key to store the value
	 * @param value      Value to store
	 * @throws IllegalArgumentException if no region exists with this region name
	 */
	public void setPostValue(String regionName, int key, double value) {
		Region region = this.regionsPost.get(regionName);
		if (region == null) throw new IllegalArgumentException("The region (" + regionName + ") doesn't exist");

		// Set value
		region.setValue(key, value);
	}

	/**
	 * Associates a value to the specified key for the region defined.<br> If the region doesn't exist, then it is
	 * created.
	 *
	 * @param regionName Name of the region to insert the value into
	 * @param key        Key to store the value
	 * @param value      Value to store
	 * @param state      State of the image (used to create region)
	 * @param roi        Roi of the region (used to create region)
	 */
	public void setPostValue(String regionName, int key, double value, ImageState state, Roi roi) {
		Region region = this.regionsPost.get(regionName);
		if (region == null) {
			// Create region
			region = new Region(regionName);
			region.inflate(state, roi);
			this.regionsPost.put(regionName, region);
		}

		// Set value
		region.setValue(key, value);
	}

	/**
	 * Gets the value associated with the specified key. The region will be searched in the Ant regions.<br> If the
	 * region could not be found, then returns null.
	 *
	 * @param regionName Region for which the value will be retrieved
	 * @param key        Key of the value to get
	 * @return value associated with the key for the region or null if not found
	 */
	public Double getAntValue(String regionName, int key) {
		Region region = this.regionsAnt.get(regionName);
		if (region == null) return null;
		return region.getValue(key);
	}

	/**
	 * Gets the value associated with the specified key. The region will be searched in the Post regions.<br> If the
	 * region could not be found, then returns null.
	 *
	 * @param regionName Region for which the value will be retrieved
	 * @param key        Key of the value to get
	 * @return value associated with the key for the region or null if not found
	 */
	public Double getPostValue(String regionName, int key) {
		Region region = this.regionsPost.get(regionName);
		if (region == null) return null;
		return region.getValue(key);
	}

	public ImageSelection getAssociatedImage() {
		return this.state.getImage();
	}

	/**
	 * Generates a string with the regions contained in this data for the specified orientation.
	 *
	 * @param orientation Ant or Post orientation to get the regions
	 * @return string with the list of the region
	 */
	private String listRegions(Orientation orientation) {
		StringBuilder res = new StringBuilder();
		if (orientation == Orientation.ANT) {
			res.append(Library_Debug.subtitle("ANT REGIONS"));
			res.append('\n');

			if (this.regionsAnt.size() == 0) res.append("// NO REGION //\n");
			else for (Region region : this.regionsAnt.values()) {
				res.append(region);
				res.append('\n');
			}
		} else {
			res.append(Library_Debug.subtitle("POST REGIONS"));
			res.append('\n');

			if (this.regionsPost.size() == 0) res.append("// NO REGION //\n");
			else for (Region region : this.regionsPost.values()) {
				res.append(region);
				res.append('\n');
			}
		}
		return res.toString();
	}

	@Override
	public String toString() {
		String s = Library_Debug.separator();
		String imageTitle = (this.getAssociatedImage() == null ? "// NO-IMAGE //" :
				this.getAssociatedImage().getImagePlus().getTitle());
		s += Library_Debug.title("Data");
		s += "\n";
		s += Library_Debug.title(imageTitle);
		s += "\n";
		s += this.listRegions(Orientation.ANT);
		s += this.listRegions(Orientation.POST);
		s += Library_Debug.separator();
		return s;
	}

	/**
	 * @return time in minutes for this data
	 */
	public double getMinutes() {
		return this.time;
	}

	public boolean hasAntData() {
		return !this.regionsAnt.isEmpty();
	}

	/**
	 * @return all regions stored by this data
	 */
	public Region[] getRegions() {
		return (Region[]) ArrayUtils.addAll(this.regionsAnt.values().toArray(new Region[0]),
											this.regionsPost.values().toArray(new Region[0]));
	}

	/**
	 * Sets the time for this data. The times in minutes represents the duration since the ingestion of the food.
	 *
	 * @param time Time elapsed in minutes since the ingestion
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * Gets the unit used to store the values of the specified key.
	 *
	 * @param key Key to know the unit
	 * @return unit of the key
	 */
	public static Unit unitForKey(int key) {
		switch (key) {
			case DATA_COUNTS:
			case DATA_COUNTS_CORRECTED:
			case DATA_GEO_AVG:
			case DATA_BKG_NOISE:
			case DATA_PIXEL_COUNTS:
			case DATA_DECAY_CORRECTED:
			case DATA_MEAN_COUNTS:
			case DATA_MEAN_GEO_AVG:
				return Unit.COUNTS;
			case DATA_PERCENTAGE:
			case DATA_DERIVATIVE:
			case DATA_CORRELATION:
				return Unit.PERCENTAGE;
			default:
				return null;
		}
	}

	public boolean hasRegion(String region) {
		return this.regionsAnt.containsKey(region) || this.regionsPost.containsKey(region);
	}
}
