package org.petctviewer.scintigraphy.scin.instructions.messages;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

import java.io.Serializable;

/**
 * This instruction displays a message to the user, but does nothing else (meaning no computing).
 *
 * @author Titouan QUÉMA
 */
public class MessageInstruction implements Instruction, Serializable {
	private static final long serialVersionUID = 1L;

	private final String message;

	/**
	 * @param message Message to show to the user
	 */
	public MessageInstruction(String message) {
		this.message = message;
	}

	@Override
	public void prepareAsNext() {
		// No computing
	}

	@Override
	public void prepareAsPrevious() {
		// No computing
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String getRoiName() {
		return null;
	}

	@Override
	public boolean isExpectingUserInput() {
		return true;
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
		return false;
	}

	@Override
	public ImageState getImageState() {
		return null;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		// No computing
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
		// No computing
	}

	@Override
	public int getRoiIndex() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

}
