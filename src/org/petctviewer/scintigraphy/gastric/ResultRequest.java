package org.petctviewer.scintigraphy.gastric;

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

	public ResultRequest(Result resultOn) {
		this.changeResultOn(resultOn);
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

	@Override
	public String toString() {
		return "Request for " + this.resultOn + "\nFit: " + fit + "\nIndex image: " + this.indexImage + "\nUnit: " +
				this.unit;
	}

}
