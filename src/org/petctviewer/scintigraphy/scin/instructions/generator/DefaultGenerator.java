package org.petctviewer.scintigraphy.scin.instructions.generator;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;

import java.io.Serializable;

/**
 * This class is a default implementation of the {@link GeneratorInstruction} interface.
 *
 * @author Titouan QUÉMA
 */
public abstract class DefaultGenerator implements GeneratorInstruction, Serializable {
	private static final long serialVersionUID = 1L;

	protected final transient Workflow workflow;

	protected final transient GeneratorInstruction parent;
	protected int indexLoop;

	protected boolean isStopped;

	/**
	 * This constructor can only be used if this is the first instruction of the generator.
	 *
	 * @param workflow Workflow where this instruction is inserted
	 */
	public DefaultGenerator(Workflow workflow) {
		this(workflow, null);
	}

	/**
	 * @param workflow Workflow where this instruction is inserted
	 * @param parent   Parent of this generated instruction (or null if this is the first instruction)
	 */
	public DefaultGenerator(Workflow workflow, GeneratorInstruction parent) {
		this.workflow = workflow;
		this.isStopped = false;

		if (parent != null) this.indexLoop = parent.getIndex() + 1;
		this.parent = parent;
	}

	@Override
	public void prepareAsNext() {
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
		return true;
	}

	@Override
	public boolean saveRoi() {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
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
	public int getRoiIndex() {
		return -1;
	}

	@Override
	public void setRoi(int index) {
	}

	@Override
	public int getIndex() {
		return this.indexLoop;
	}

	@Override
	public GeneratorInstruction getParent() {
		return this.parent;
	}

	@Override
	public void stop() {
		this.isStopped = true;
	}

	@Override
	public void activate() {
		this.isStopped = false;
	}
	
	public boolean isStopped() {
		return this.isStopped;
	}

}
