package org.petctviewer.scintigraphy.gastric;

import java.util.Objects;

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

	private final String name;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Result result = (Result) o;
		return Objects.equals(name, result.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
