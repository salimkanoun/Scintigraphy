package org.petctviewer.scintigraphy.scin.instructions.prompts;

import org.petctviewer.scintigraphy.scin.instructions.Instruction;

import javax.swing.*;

/**
 * This class represents a dialog prompting an information to the user.
 * 
 * @author Titouan QUÉMA
 *
 */
public abstract class PromptDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * This method is called to get the result entered by the user. When overridden,
	 * this method should have its return type changed according to the return type
	 * expected.
	 * 
	 * @return result of the prompt dialog
	 */
	public abstract Object getResult();

	/**
	 * This method is called to check whether the user has entered a valid input.
	 * This method can be called at any time so do not assume that the data entered
	 * is not null.
	 * 
	 * @return TRUE if the user input is correct and FALSE otherwise
	 */
	public abstract boolean isInputValid();

	/**
	 * This method is called to check whether this dialog should be displayed to the
	 * user. If not, then nothing happens.
	 * 
	 * @return TRUE if the dialog should be visible and FALSE if not
	 */
	public boolean shouldBeDisplayed() {
		// By default, always displayed
		return true;
	}

	/**
	 * This method is called when this dialog will be opened by a click on the
	 * 'Next' button.
	 * 
	 * @see Instruction#prepareAsNext
	 */
	protected void prepareAsNext() {
		// By default do nothing
	}

	/**
	 * This method is called when this dialog will be opened by a click on the
	 * 'Previous' button.
	 * 
	 * @see Instruction#prepareAsPrevious
	 */
	protected void prepareAsPrevious() {
		// By default do nothing
	}

	/**
	 * This method is called after the 'Next' button has been clicked.<br>
	 * At this stage, all of the operations for the next action are done.
	 * 
	 * @see Instruction#afterNext
	 */
	protected void afterNext() {
		// By default do nothing
	}

	/**
	 * This method is called after the 'Previous' button has been clicked.<br>
	 * At this stage, all of the operations for the previous action are done.
	 * 
	 * @see Instruction#afterPrevious
	 */
	protected void afterPrevious() {
		// By default do nothing
	}

}
