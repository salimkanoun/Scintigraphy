package org.petctviewer.scintigraphy.scin.instructions.messages;

/**
 * This instruction shows a simple message to the user to start the exam.
 *
 * @author Titouan QUÉMA
 */
public class StartInstruction extends MessageInstruction {
	private static final long serialVersionUID = 1L;

	public StartInstruction() {
		super("Click on 'Next' to start the exam");
	}

}
