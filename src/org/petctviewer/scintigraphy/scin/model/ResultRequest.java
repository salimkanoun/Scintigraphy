package org.petctviewer.scintigraphy.scin.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a request for a result in a model. A request for a value will contain at least the Result on
 * which the request is made for.<br> All other field embedded in the request are optional and therefor, you should not
 * assume that they will be not null.<br> As a structure strategy, if any non-mandatory field is missing, a result
 * <b>must</b> still be returned . The returned result will have to guess the missing fields if necessary, and will
 * inform the applicant by inflating the ResultValue.
 *
 * @author Titouan QUÉMA
 */
public class ResultRequest {

	private Result resultOn;
	private Fit fit;
	private int indexImage;
	private Unit unit;

	// Values from 0 -> 1000 are reserved for this class. Any other value can be used by programs
	private Map<Integer, Object> payload;

	public ResultRequest(Result resultOn) {
		this.changeResultOn(resultOn);
		this.payload = new HashMap<>();
	}

	/**
	 * @param resultOn Result for which this request asks for
	 */
	public void changeResultOn(Result resultOn) {
		this.resultOn = resultOn;
	}

	/**
	 * @return the result asked by this request
	 */
	public Result getResultOn() {
		return this.resultOn;
	}

	/**
	 * @return fit used to extrapolate the result value if needed
	 */
	public Fit getFit() {
		return fit;
	}

	/**
	 * @param fit Fit to be used to extrapolate the result value returned
	 */
	public void setFit(Fit fit) {
		this.fit = fit;
	}

	/**
	 * @return index of the image the request deals with
	 */
	public int getIndexImage() {
		return indexImage;
	}

	/**
	 * @param indexImage Index of the image the request deals with
	 */
	public void setIndexImage(int indexImage) {
		this.indexImage = indexImage;
	}

	/**
	 * @return unit the value should be returned
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * @param unit Unit the value should be returned
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * Adds some data to this request. Data can be of any type. Keys from 0 to 1000 are reserved for this class, but
	 * any
	 * other value can be used freely by any program.
	 *
	 * @param key   Key of the value to embed
	 * @param value Value to embed
	 */
	public void addEmbeddedData(int key, Object value) {
		this.payload.put(key, value);
	}

	/**
	 * Retrieves the embedded data with the specified key. If no data is present for this key, then null is returned.
	 *
	 * @param key Key of the value to retrieve
	 * @return value previously embedded in this request or null if not found
	 * @see #retrieveData(int, Object)
	 */
	public Object retrieveData(int key) {
		return this.payload.get(key);
	}

	/**
	 * Retrives the embedded data with the specified key. If no data is present for this key, then the default value is
	 * returned.
	 *
	 * @param key          Key of the value to retrieve
	 * @param defaultValue Default value to return if no value was found
	 * @return value previously embedded in this request or default value if not found
	 * @see #retrieveData(int)
	 */
	public Object retrieveData(int key, Object defaultValue) {
		return this.payload.getOrDefault(key, defaultValue);
	}

	@Override
	public String toString() {
		return "Request for " + this.resultOn + "\nFit: " + fit + "\nIndex image: " + this.indexImage + "\nUnit: " +
				this.unit;
	}

}
