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
	public boolean isDisplayable() {
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

}
