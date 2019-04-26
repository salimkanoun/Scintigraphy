package org.petctviewer.scintigraphy.scin.instructions.prompts;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

public class PromptInstruction implements Instruction {
	
	private PromptDialog dialog;
	
	public PromptInstruction(PromptDialog dialog) throws IllegalArgumentException {
		if(dialog == null)
			throw new IllegalArgumentException("The dialog cannot be null");
		
		this.dialog = dialog;
		this.dialog.setModal(true);
	}
	
	private void after() {
		this.dialog.setVisible(true);
	}

	@Override
	public void prepareAsNext() {
	}

	@Override
	public void prepareAsPrevious() {
	}

	@Override
	public String getMessage() {
		return "Answer the question";
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
		return !this.dialog.isCompleted();
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
		this.after();
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
		this.after();
	}

	@Override
	public int roiToDisplay() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

}
