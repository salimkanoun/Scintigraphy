package org.petctviewer.scintigraphy.scin.instructions.messages;

import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;

/**
 * This instruction shows a simple message to the user.
 *
 * @author Titouan QUÉMA
 */
public class EndInstruction extends MessageInstruction implements LastInstruction {
	private static final long serialVersionUID = 1L;

	public EndInstruction() {
		super("End instruction!");
	}

}
