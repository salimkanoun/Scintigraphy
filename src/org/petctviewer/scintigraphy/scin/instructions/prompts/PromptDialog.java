package org.petctviewer.scintigraphy.scin.instructions.prompts;

import javax.swing.JDialog;

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
	 * This method is called to checks whether the user has entered a valid input.
	 * This method can be called at any time so do not assume that the data entered
	 * is not null.
	 * 
	 * @return TRUE if the user input is correct and FALSE otherwise
	 */
	public abstract boolean isInputValid();

	/**
	 * This method is called when this dialog will be opened by a click on the
	 * 'Next' button.
	 */
	protected void prepareAsNext() {
		// By default do nothing
	}

	/**
	 * This method is called when this dialog will be opened by a click on the
	 * 'Previous' button.
	 */
	protected void prepareAsPrevious() {
		// By default do nothing
	}

}
