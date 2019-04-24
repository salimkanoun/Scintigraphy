package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;

public abstract class MessageInstruction implements Instruction {

	private String message;

	public MessageInstruction(String message) {
		this.message = message;
	}

	@Override
	public void prepareAsNext() {

	}

	@Override
	public void prepareAsPrevious() {

	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public boolean isExpectingUserInput() {
		return true;
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
	public int roiToDisplay() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean saveRoi() {
		return false;
	}

	@Override
	public boolean isRoiVisible() {
		return false;
	}

	@Override
	public String getRoiName() {
		return null;
	}

}
