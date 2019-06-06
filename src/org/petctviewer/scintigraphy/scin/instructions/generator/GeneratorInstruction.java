package org.petctviewer.scintigraphy.scin.instructions.generator;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

/**
 * This interface allows instructions to generate other instructions while running the workflow.
 *
 * @author Titouan QUÉMA
 */
public interface GeneratorInstruction extends Instruction {

	/**
	 * Generates an instruction in the workflow after this instruction. Once this method has been called, it should
	 * never generate another child and <b>always</b> return null.
	 *
	 * @return Instruction to generate or null if no instruction should be generated
	 */
	Instruction generate();

	/**
	 * The root instruction that started the generation must have the index 0. Then, every child must return the index
	 * of their parent + 1.
	 *
	 * @return index of this instruction in the flow of the generated instruction
	 */
	int getIndex();

	/**
	 * This method returns the parent that created this instruction. The index of the parent must be 1 less than the
	 * index of this instruction.<br> If this instruction is the root instruction that started the generation, then
	 * this
	 * method should return null.
	 *
	 * @return parent of this instruction or null if this is the root instruction
	 */
	GeneratorInstruction getParent();

	/**
	 * This method must be called when a COMMAND_END is sent to the controller.<br> This method should prevent this
	 * instruction to generate another child instruction. (Unless the {@link #activate()} method is called)<br> If this
	 * method is called, it is
	 * <b>before</b>
	 * the {@link #afterNext(ControllerWorkflow)} method.
	 */
	void stop();

	/**
	 * This method allows this instruction to generate again a new instruction.
	 */
	void activate();

}
