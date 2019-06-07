package org.petctviewer.scintigraphy.scin.instructions.execution;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

import java.io.Serializable;

/**
 * This class is the default implementation of a instruction that will execute some code. This means that this
 * instruction doesn't expect any user input, and will just compute.
 *
 * @author Titouan QUÉMA
 */
public abstract class ExecutionInstruction implements Instruction, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public void prepareAsNext() {
	}

	@Override
	public void prepareAsPrevious() {
	}

	@Override
	public String getMessage() {
		return "";
	}

	@Override
	public String getRoiName() {
		return "Unknown ROI";
	}

	@Override
	public boolean isExpectingUserInput() {
		return false;
	}

	@Override
	public boolean saveRoi() {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isRoiVisible() {
		return this.saveRoi();
	}

	@Override
	public ImageState getImageState() {
		return null;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
	}

	@Override
	public int getRoiIndex() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

}
