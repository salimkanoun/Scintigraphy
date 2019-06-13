package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class represents a flow of instructions used by the {@link ControllerWorkflow}.<br> Only the last workflow of a
 * controller MUST end with the {@link LastInstruction}.
 *
 * @author Titouan QUÉMA
 */
public class Workflow implements Serializable {

	private static final long serialVersionUID = 1L;
	private final List<Instruction> instructions;
	private final transient ControllerWorkflow controller;
	private final transient ImageSelection imageAssociated;
	private transient ListIterator<Instruction> iterator;
	private transient Instruction current;

	// TODO: allow only 1 workflow per controller (this mean to decouple the image from the workflow and only use
	//  ImageState)

	/**
	 * Creates a new workflow. A workflow is based on a ImageSelection and is linked to only 1 controller.
	 */
	public Workflow(ControllerWorkflow controller, ImageSelection imageAssociated) {
		this.instructions = new LinkedList<>();
		this.controller = controller;
		this.imageAssociated = imageAssociated;
		this.restart();
	}

	/**
	 * @return image associated with this workflow
	 */
	public ImageSelection getImageAssociated() {
		return this.imageAssociated;
	}

	/**
	 * @return controller associated with this workflow
	 */
	public ControllerWorkflow getController() {
		return this.controller;
	}

	/**
	 * @return current instruction on this workflow or null if it hasn't been started
	 */
	public Instruction getCurrentInstruction() {
		return this.current;
	}

	/**
	 * Adds an instruction in this workflow. Adding an instruction restart the workflow, so a call to {@link #next()}
	 * will return the first instruction.<br> Adding a null instruction restart this workflow but the instruction is
	 * ignored.
	 *
	 * @param instruction Instruction to add in the workflow (null accepted)
	 */
	public void addInstruction(Instruction instruction) {
		if (instruction != null) this.instructions.add(instruction);
		this.restart();
	}

	/**
	 * Adds an instruction in this workflow. Adding an instruction on the fly adds the specified instruction
	 * <b>after</b> the instruction returned by {@link #getCurrentInstruction()} and do NOT restart this workflow.<br>
	 * Adding a null instruction does nothing.<br> If the current instruction is null (meaning the workflow has not
	 * been
	 * started) then this function is equivalent to {@link #addInstruction(Instruction)} (and though the workflow is
	 * restarted).<br> This method is meant to be used by the {@link GeneratorInstruction} class only.
	 * <p>
	 * Visualization when adding an instruction:
	 * <pre>
	 *     	                          CURRENT
	 *                                  vvv
	 * +-------------+            +-------------+            +-------------+
	 * | Instruction |   - - ->   | Instruction |   - - ->   | Instruction |
	 * +-------------+            +-------------+            +-------------+
	 *
	 *     	                          CURRENT
	 *                                  vvv
	 * +-------------+            +-------------+            +- - - - - - -+            +-------------+
	 * | Instruction |   - - ->   | Instruction |   - - ->   |  Inserted   |   - - ->   | Instruction |
	 * +-------------+            +-------------+            +- - - - - - -+            +-------------+
	 * </pre>
	 * </p>
	 *
	 * @param instruction Instruction to add after the current instruction.
	 */
	public void addInstructionOnTheFly(Instruction instruction) {
		if (this.current == null) this.addInstruction(instruction);
		else if (instruction != null) {
			this.iterator.add(instruction);
			this.iterator.previous();
		}
	}

	/**
	 * Retrieves the next instruction of this workflow and move on to this instruction.<br> After a call to this
	 * method,
	 * a call to the {@link #getCurrentInstruction()} will return the same instruction.
	 *
	 * @return next instruction of this workflow or null if none
	 */
	public Instruction next() {
		if (this.iterator.hasNext()) {
			Instruction next = this.iterator.next();
			next.prepareAsNext();
			this.current = next;
			return next;
		}
		return null;
	}

	/**
	 * Retrieves the previous instruction of this workflow and move on to this instruction.<br> After a call to this
	 * method, a call to the {@link #getCurrentInstruction()} will return the same instruction.
	 *
	 * @return previous instruction of this workflow or null if none
	 */
	public Instruction previous() {
		if (this.iterator.hasPrevious()) {
			this.iterator.previous();
			if (this.iterator.hasPrevious()) {
				Instruction previous = this.iterator.previous();
				previous.prepareAsPrevious();
				this.iterator.next();
				this.current = previous;
				return previous;
			}
		}
		return null;
	}

	/**
	 * Restarts the workflow.<br> After a call to this method, a call to the {@link #next()} method will return the
	 * next
	 * instruction (if any) and a call to the {@link #previous()} instruction will return null.
	 */
	public void restart() {
		this.iterator = this.instructions.listIterator();
		this.current = null;
	}

	/**
	 * Checks if this workflow is at the last instruction.
	 *
	 * @return TRUE if this workflow no longer have a next instruction and FALSE if there is an next instruction
	 */
	public boolean isOver() {
		return !this.iterator.hasNext();
	}

	/**
	 * Counts the number of ROIs that will be created by this workflow. This is a planned value.<br> Be careful when
	 * using this method, it cannot predict the number of ROI created by generated instructions.<br> This method should
	 * only be used when this workflow doesn't contain any instruction generator.
	 *
	 * @return number of ROIs created
	 */
	public int countRoisCreated() {
		int count = 0;
		for (Instruction i : this.instructions)
			if (i.saveRoi()) count++;
		return count;
	}

	/**
	 * Retrieves the instruction at the specified index.<br> This method should not be abused since it violates the
	 * encapsulation.<br> The specified index must be in range of 0 to the max number of instructions. If the index is
	 * incorrect, an exception will be thrown.
	 *
	 * @param index Index of the instruction to retrieve
	 * @return Instruction at the index position of this workflow
	 */
	public Instruction getInstructionAt(int index) {
		return this.instructions.get(index);
	}

	/**
	 * This method will look through all instructions to search for any instruction matching the same facingOrientation
	 * specified. If an instruction has no state, or its facingOrientation's state is null ; this instruction will be
	 * returned.<br> This method only gets the instructions with a <u>visible</u> ROI to save.
	 *
	 * @param facingOrientation Facing orientation to search (null accepted)
	 * @return all the instructions with a ROI to display matching the specified orientation, including the
	 * instructions
	 * with no state
	 */
	public Instruction[] getInstructionsWithOrientation(Orientation facingOrientation) {
		List<Instruction> instructions = new LinkedList<>();
		for (Instruction i : this.instructions)
			// Checking instruction has a ROI to display, the facingOrientation is the same
			// or is null
			if (i.saveRoi() && i.isRoiVisible() && (facingOrientation == null || i.getImageState() == null ||
					i.getImageState().getFacingOrientation() == facingOrientation)) instructions.add(i);
		return instructions.toArray(new Instruction[0]);
	}

	/**
	 * Used for debug only.
	 *
	 * @return debug string of this workflow
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("--| WORKFLOW: ");
		s.append(this.imageAssociated.getImagePlus().getShortTitle());
		s.append(" |--\n");

		for (int i = 0; i < this.instructions.size(); i++) {
			Instruction ins = this.instructions.get(i);
			if (ins == this.current) s.append(">> ");
			s.append("[");
			s.append(i);
			s.append("] (");
			s.append(ins.getClass().getSimpleName());
			s.append(") ");
			s.append(ins.getMessage());

			if (ins instanceof GeneratorInstruction) {
				GeneratorInstruction gi = (GeneratorInstruction) ins;
				s.append(" -index:").append(gi.getIndex());
				if (gi.getParent() != null) s.append("- Parent: ").append(gi.getParent().getIndex());
				else s.append("- ROOT_INSTRUCTION");
			}

			if (ins == this.current) s.append(" <<");
			s.append("\n");
		}

		s.append("----------------------\n");

		return s.toString();
	}

	public List<Instruction> getInstructions() {
		return this.instructions;
	}

	public void removeInstructionWithIterator() {
		this.current = iterator.previous();
		iterator.remove();
		iterator.previous();
		this.current = iterator.next();
	}

}
