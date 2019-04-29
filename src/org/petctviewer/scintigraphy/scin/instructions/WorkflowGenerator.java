package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

public class WorkflowGenerator {

	/**
	 * Generates a simple workflow for 1 image. A simple workflow is a succession of
	 * instructions asking the user to draw a ROI for each organ.<br>
	 * The instructions are in the following order:<br>
	 * <ol>
	 * <li>All organs for <code>firstOrientation</code> orientation</li>
	 * <li>All organs for <code>firstOrientation.invert()</code> orientation</li>
	 * </ol>
	 * 
	 * @param firstOrientation first orientation prompted to the user
	 * @return Workflow generated
	 * @throws IllegalArgumentException if the specified orientation doesn't have an
	 *                                  inverse
	 */
	public static Workflow oneImageSimpleWorkflow(String[] organs, Orientation firstOrientation)
			throws IllegalArgumentException {
		if (firstOrientation.invert() == Orientation.UNKNOWN)
			throw new IllegalArgumentException("The orientation " + firstOrientation + " doesn't have a inverse!");

		Workflow w = new Workflow();
		for (String organ : organs) {
			w.addInstruction(new DrawRoiInstruction(organ, firstOrientation));
		}
		for (int i = 0; i < organs.length; i++) {
			w.addInstruction(new DrawRoiInstruction(organs[i], firstOrientation.invert(),
					(DrawRoiInstruction) w.getInstructionAt(i)));
		}
		w.addInstruction(new EndInstruction());
		return w;
	}

	/**
	 * Generates a simple workflow for each image.<br>
	 * 
	 * @see #oneImageSimpleWorkflow(String[])
	 * 
	 * @param organs           array of organs for each image
	 * @param firstOrientation first orientation prompted to the user
	 * @return workflow for each image
	 * @throws IllegalArgumentException if the specified orientation doesn't have an
	 *                                  inverse
	 */
	public static Workflow[] multipleImagesSimpleWorkflow(String[][] organs, Orientation firstOrientation)
			throws IllegalArgumentException {
		Workflow[] w = new Workflow[organs.length];
		for (int i = 0; i < organs.length; i++) {
			w[i] = oneImageSimpleWorkflow(organs[i], firstOrientation);
		}
		w[organs.length - 1].addInstruction(new EndInstruction());
		return w;
	}

}
