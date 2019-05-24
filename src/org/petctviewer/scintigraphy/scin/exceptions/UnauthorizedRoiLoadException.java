package org.petctviewer.scintigraphy.scin.exceptions;

import ij.gui.Roi;

public class UnauthorizedRoiLoadException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Throw this exception when you load a Roi that is not contained in the workflow.json
	 */
	public UnauthorizedRoiLoadException(Roi unauthorizedRoi, String unauthorizedRoiFileName) {
		super("You're trying to load a Roi that is not recognized by the workflow.json : "+unauthorizedRoi.getName()+" / "+unauthorizedRoiFileName
				+ "\n Try to modify manually the workflow.json to accept it.");
	}

}
