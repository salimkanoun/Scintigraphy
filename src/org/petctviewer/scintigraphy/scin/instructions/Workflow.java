package org.petctviewer.scintigraphy.scin.instructions;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.petctviewer.scintigraphy.scin.Orientation;

public class Workflow {

	private List<Instruction> instructions;
	private ListIterator<Instruction> iterator;
	private Instruction current;

	/**
	 * Creates a new workflow. A workflow is based on a ImageSelection.
	 */
	public Workflow() {
		this.instructions = new LinkedList<>();
		this.addInstruction(null);
	}

	/**
	 * @return current instruction on this workflow or null if it hasn't been
	 *         started
	 */
	public Instruction getCurrentInstruction() {
		return this.current;
	}

	/**
	 * Adds an instruction in this workflow. Adding an instruction restart the
	 * workflow, so a call to {@link #next()} will return the first instruction.<br>
	 * Adding a null instruction restart this workflow but the instruction is
	 * ignored.
	 * 
	 * @param instruction Instruction to add in the workflow
	 */
	public void addInstruction(Instruction instruction) {
		if (instruction != null)
			this.instructions.add(instruction);
		this.restart();
	}

	/**
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
	 * @return previous instruction of this workflow or null if none
	 */
	public Instruction previous() {
		if (this.iterator.hasPrevious()) {
			Instruction previous = this.iterator.previous();
			if (this.iterator.hasPrevious()) {
				previous = this.iterator.previous();
				previous.prepareAsPrevious();
				this.iterator.next();
				this.current = previous;
				return previous;
			}
		}
		return null;
	}

	/**
	 * Restarts the workflow. This method reset this workflow's state for the one
	 * that it had at its initialization.
	 */
	public void restart() {
		this.iterator = this.instructions.listIterator();
		this.current = null;
	}

	/**
	 * @return TRUE if this workflow no longer have a next instruction and FALSE if
	 *         there is an next instruction
	 */
	public boolean isOver() {
		return !this.iterator.hasNext();
	}

	/**
	 * Counts the number of ROIs that will be created in this workflow. This is a
	 * planned value.
	 * 
	 * @return number of ROIs created
	 */
	public int countRoisCreated() {
		int count = 0;
		for (Instruction i : this.instructions)
			if (i instanceof DrawRoiInstruction)
				count++;
		return count;
	}

	/**
	 * This method should not be used since it violates the encapsulation.
	 * 
	 * @return Instruction at the index position of this workflow
	 */
	public Instruction getInstructionAt(int index) {
		return this.instructions.get(index);
	}

	/**
	 * @return all the instructions with a ROI to display matching the specified
	 *         orientation
	 */
	public Instruction[] getInstructionsWithOrientation(Orientation orientation) {
		List<Instruction> instructions = new LinkedList<>();
		for (Instruction i : this.instructions)
			if (i.getImageState() != null && i.getImageState().orientation == orientation && i.saveRoi()
					&& i.isRoiVisible())
				instructions.add(i);
		return instructions.toArray(new Instruction[instructions.size()]);
	}

}
