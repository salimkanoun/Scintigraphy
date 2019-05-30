package org.petctviewer.scintigraphy.scin.instructions.generator;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;

import java.io.Serializable;

public abstract class DefaultGenerator implements GeneratorInstruction, Serializable {

	private static final long serialVersionUID = 1L;

	protected final transient Workflow workflow;

	protected final transient GeneratorInstruction parent;
	protected int indexLoop;

	protected boolean isStopped;

	public DefaultGenerator(Workflow workflow) {
		this(workflow, null);
	}

	public DefaultGenerator(Workflow workflow, GeneratorInstruction parent) {
		this.workflow = workflow;
		this.isStopped = false;

		if (parent != null)
			this.indexLoop = parent.getIndex() + 1;
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

	@Override
	public int getIndex() {
		return this.indexLoop;
	}
	
	public boolean isStopped() {
		return this.isStopped;
	}

}
