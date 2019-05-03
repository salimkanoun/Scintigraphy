package org.petctviewer.scintigraphy.scin.instructions.execution;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

/**
 * Instruction to check if two ROIs are intersecting.
 * 
 * @author Titouan QUÉMA
 *
 */
public class CheckIntersectionInstruction extends ExecutionInstruction {

	private Instruction previous_1, previous_2;
	private ControleurScin controller;
	private String nameIntersection;

	/**
	 * @param intersection1    First instruction from which the intersection must be
	 *                         computed
	 * @param intersection2    Second instruction from which the intersection must
	 *                         be computed
	 * @param nameIntersection Name of the intersection (for display purposes)
	 */
	public CheckIntersectionInstruction(ControleurScin controller, Instruction intersection1, Instruction intersection2,
			String nameIntersection) {
		this.controller = controller;
		this.previous_1 = intersection1;
		this.previous_2 = intersection2;
		this.nameIntersection = nameIntersection;
	}

	/**
	 * Checks that there is an intersection between the previous ROI and the current
	 * one.
	 */
	private boolean checkIntersectionBetweenRois() {
		this.controller.getRoiManager()
				.setSelectedIndexes(new int[] { this.previous_1.roiToDisplay(), this.previous_2.roiToDisplay() });
		this.controller.getRoiManager().runCommand("AND");
		this.controller.getRoiManager().runCommand("Deselect");
		this.controller.getRoiManager().deselect();
		if (this.controller.getVue().getImagePlus().getRoi() == null) {
			JOptionPane.showMessageDialog(
					this.controller.getVue(), "Please adjust the " + previous_2.getRoiName()
							+ " in order to create an intersection with the " + previous_1.getRoiName() + ".",
					"Intersection missing", JOptionPane.PLAIN_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public boolean isCancelled() {
		return !this.checkIntersectionBetweenRois();
	}

	@Override
	public boolean saveRoi() {
		return true;
	}

	@Override
	public boolean isRoiVisible() {
		return false;
	}

	@Override
	public String getRoiName() {
		return this.nameIntersection;
	}
}