package org.petctviewer.scintigraphy.scin.instructions.messages;

import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;

public class EndInstruction extends MessageInstruction implements LastInstruction {

	public EndInstruction() {
		super("End!");
	}

}
