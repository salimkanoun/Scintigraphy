package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.instructions.messages.MessageInstruction;

public class EndInstruction extends MessageInstruction implements LastInstruction {

	public EndInstruction() {
		super("End!");
	}

}
