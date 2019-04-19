package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;

public abstract class ExecutionInstruction implements Instruction {

	@Override
	public void prepareAsNext() {
	}

	@Override
	public void prepareAsPrevious() {
	}

	@Override
	public String getMessage() {
		return null;
	}

	@Override
	public boolean isDisplayable() {
		return false;
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
