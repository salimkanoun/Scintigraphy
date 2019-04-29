package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.messages.MessageInstruction;

/**
 * This class represents a instruction that will generate other instructions
 * while running the workflow.<br>
 * This class uses the <i>Factory Method</i> of the <a href=
 * "https://sophia.javeriana.edu.co/~cbustaca/docencia/DSBP-2018-01/recursos/Erich%20Gamma,%20Richard%20Helm,%20Ralph%20Johnson,%20John%20M.%20Vlissides-Design%20Patterns_%20Elements%20of%20Reusable%20Object-Oriented%20Software%20%20-Addison-Wesley%20Professional%20%281994%29.pdf">Creational
 * Patterns</a>
 * 
 * @author Titouan QUÉMA
 *
 */
public class GeneratorInstruction implements Instruction {

	public enum InstructionType {
		MESSAGE_INSTRUCTION, DRAW_ROI_INSTRUCTION;
	}

	private InstructionType type;
	private Workflow workflow;

	private int instructionCount;
	private boolean isStopped;

	public GeneratorInstruction(Workflow workflow, InstructionType type) {
		this.type = type;
		this.workflow = workflow;
		this.instructionCount = 0;
		this.isStopped = false;
	}

	@Override
	public void prepareAsNext() {
		// Creates new instruction in the workflow
		switch (this.type) {
		case DRAW_ROI_INSTRUCTION:
			this.workflow
					.addInstructionOnTheFly(new DrawRoiInstruction("NewInstruction#" + (++instructionCount), null));
			break;
		case MESSAGE_INSTRUCTION:
			this.workflow.addInstructionOnTheFly(new MessageInstruction("NewMessage#" + (++instructionCount)));
			break;
		}
	}

	@Override
	public void prepareAsPrevious() {
	}

	@Override
	public String getMessage() {
		return null;
	}

	@Override
	public String getRoiName() {
		return null;
	}

	@Override
	public boolean isExpectingUserInput() {
		return false;
	}

	@Override
	public boolean saveRoi() {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return !this.isStopped;
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
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
	}

	@Override
	public int roiToDisplay() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

}
