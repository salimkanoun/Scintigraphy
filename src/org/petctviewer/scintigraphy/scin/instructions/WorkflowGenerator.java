package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

import ij.ImagePlus;

public class WorkflowGenerator {

	/**
	 * Generates a simple workflow for 1 image. A simple workflow is a succession of
	 * instructions asking the user to draw a ROI for each organ.<br>
	 * The instructions are in the following order:<br>
	 * <ol>
	 * <li>All organs for Ant orientation</li>
	 * <li>All organs for Post orientation</li>
	 * </ol>
	 * When asking for the ROI in Post orientation, the ROI of the Ant orientation
	 * is auto-filled.
	 * 
	 * @return Workflow generated
	 */
	public static Workflow oneImageSimpleWorkflow(ControllerWorkflow controller, ImagePlus imp, String[] organs) {
		Workflow w = new Workflow(controller, imp);
		for (String organ : organs) {
			w.addInstruction(new DrawRoiInstruction(organ, Orientation.ANT));
		}
		for (int i = 0; i < organs.length; i++) {
			w.addInstruction(
					new DrawRoiInstruction(organs[i], Orientation.POST, (DrawRoiInstruction) w.getInstructionAt(i)));
		}
		w.addInstruction(new EndInstruction());
		return w;
	}

	/**
	 * Generates a simple workflow for each image.<br>
	 * 
	 * @see #oneImageSimpleWorkflow(String[])
	 * 
	 * @param organs array of organs for each image
	 * @return workflow for each image
	 */
	public static Workflow[] multipleImagesSimpleWorkflow(ControllerWorkflow controller, ImagePlus[] imps, String[][] organs) {
		Workflow[] w = new Workflow[organs.length];
		for (int i = 0; i < organs.length; i++) {
			w[i] = oneImageSimpleWorkflow(controller, imps[i], organs[i]);
		}
		w[organs.length - 1].addInstruction(new EndInstruction());
		return w;
	}

}
