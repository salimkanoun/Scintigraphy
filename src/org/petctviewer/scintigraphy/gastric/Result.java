package org.petctviewer.scintigraphy.gastric;

import java.util.Objects;

/**
 * This class represents a result returned by a model.<br> Each model should present to the controller a group of
 * results that are available and can be provided.<br> Instances of result should only be in models.
 *
 * @author Titouan QUÉMA
 * @see ResultValue
 * @see ResultRequest
 */
public class Result {

	private final String name;
	private final int methodNumber;

	/**
	 * Instantiates a new result with the specified name.<br> Note that names are not unique.<br> This is a convenience
	 * method for {@link #Result(String, int)}
	 *
	 * @param name Name of the result (used for display)
	 */
	public Result(String name) {
		this(name, 1);
	}

	/**
	 * Instantiates a new result with the specified name. The method number represent is used if a model has several
	 * results of the same thing, but computed with different methods.<br> Note that names are not uniques.
	 *
	 * @param name         Name of the result (used for display)
	 * @param methodNumber Number to identify the method of this result (generally starting with 1)
	 */
	public Result(String name, int methodNumber) {
		this.name = name;
		this.methodNumber = 1;
	}

	/**
	 * @return name of this result
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Result result = (Result) o;
		return methodNumber == result.methodNumber && Objects.equals(name, result.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, methodNumber);
	}

	/**
	 * @return number of the method for this result
	 */
	public int getMethodNumber() {
		return this.methodNumber;
	}

	@Override
	public String toString() {
		return getName();
	}

}
