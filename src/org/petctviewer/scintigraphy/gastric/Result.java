package org.petctviewer.scintigraphy.gastric;

/**
 * This class represents a result returned by a model.<br>
 * Each model should present to the controller a group of results that are
 * available and can be provided.<br>
 * Instances of result should only be in models.
 * 
 * @see ResultValue
 * @see ResultRequest
 * 
 * @author Titouan QUÉMA
 *
 */
public class Result {

	private String name;

	/**
	 * Instantiates a new result with the specified name.<br>
	 * Note that names are not unique.
	 * 
	 * @param name Name of the result (used for display)
	 */
	public Result(String name) {
		this.name = name;
	}

	/**
	 * @return name of this result
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return getName();
	}

}
