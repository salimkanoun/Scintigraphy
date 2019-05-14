package org.petctviewer.scintigraphy.scin.instructions.prompts;

import java.awt.Component;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

/**
 * This class represents a instruction opening a prompt dialog. This instruction
 * requires the user to enter data on this dialog.
 * 
 * @author Titouan QUÉMA
 *
 */
public class PromptInstruction implements Instruction {

	protected PromptDialog dialog;

	/**
	 * Instantiates an instruction with the specified dialog as a prompt for the
	 * user.
	 * 
	 * @param dialog Dialog the user will have to answer (it cannot be null)
	 * @throws IllegalArgumentException if the dialog is null
	 */
	public PromptInstruction(PromptDialog dialog) throws IllegalArgumentException {
		if (dialog == null)
			throw new IllegalArgumentException("The dialog cannot be null");

		this.dialog = dialog;
		this.dialog.setModal(true);
	}

	protected void displayDialog(Component parent) {
		this.dialog.setLocationRelativeTo(parent);
		this.dialog.setVisible(true);
	}

	@Override
	public void prepareAsNext() {
		this.dialog.prepareAsNext();
	}

	@Override
	public void prepareAsPrevious() {
		this.dialog.prepareAsPrevious();
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
		return !this.dialog.isInputValid();
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
		this.dialog.afterNext();
		if (this.dialog.shouldBeDisplayed())
			this.displayDialog(controller.getVue());
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
		this.dialog.afterPrevious();
		if (this.dialog.shouldBeDisplayed())
			this.displayDialog(controller.getVue());
	}

	@Override
	public int roiToDisplay() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

}
