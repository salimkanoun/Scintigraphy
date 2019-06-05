package org.petctviewer.scintigraphy.scin.json;

import java.util.List;

import org.petctviewer.scintigraphy.scin.instructions.Workflow;

/**
 * This class represent a {@link Workflow}, as saved in a Json file. <br/>
 * Contains a list of {@link InstructionFromGson}.
 *
 */
public class WorkflowFromGson {
	List<InstructionFromGson> Intructions;

	public List<InstructionFromGson> getInstructions() {
		return this.Intructions;
	}

	public InstructionFromGson getInstructionAt(int index) {
		return this.Intructions.get(index);
	}
}
