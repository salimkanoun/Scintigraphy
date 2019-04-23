package org.petctviewer.scintigraphy.scin.instructions;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;

public class CheckIntersectionInstruction extends ExecutionInstruction {

	private Instruction previous_1, previous_2;
	private ControleurScin controller;

	public CheckIntersectionInstruction(ControleurScin controller, Instruction intersection1,
			Instruction intersection2) {
		this.controller = controller;
		this.previous_1 = intersection1;
		this.previous_2 = intersection2;
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
			JOptionPane.showMessageDialog(this.controller.getVue(),
					"Please adjust the intestine in order to create an intersection with the estomach.",
					"Intersection missing", JOptionPane.PLAIN_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public boolean isCancelled() {
		System.out.println("*-* Checking intersection...");
		boolean res = this.checkIntersectionBetweenRois();
		if (res) {
			System.out.println("\tIntersection!");
		} else {
			System.out.println("\tNo intersection");
		}
		return !res;
	}

	@Override
	public boolean saveRoi() {
		return true;
	}

	@Override
	public boolean isRoiVisible() {
		return false;
	}
}
