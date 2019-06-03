package org.petctviewer.scintigraphy.scin.instructions.messages;

import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;

public class EndInstruction extends MessageInstruction implements LastInstruction {
	
	private static final long serialVersionUID = 1L;

	public EndInstruction() {
		super("End instruction!");
	}

}
