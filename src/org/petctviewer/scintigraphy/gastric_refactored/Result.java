package org.petctviewer.scintigraphy.gastric_refactored;

/**
 * This class represents a result a model can provide.<br>
 * Each model should instantiate their own results and make them available.<br>
 * 
 * @author Titouan QUÉMA
 *
 */
public class Result {

	private String name;

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
		return this.name;
	}

}
