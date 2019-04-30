package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;

public class DrawLoopInstruction extends DefaultGenerator {

	private int indexRoiToDisplay;

	public DrawLoopInstruction(Workflow workflow) {
		this(workflow, null);
	}

	protected DrawLoopInstruction(Workflow workflow, GeneratorInstruction parent) {
		super(workflow, parent);
		this.indexRoiToDisplay = -1;
	}

	@Override
	public Instruction generate() {
		if (!this.isStopped) {
			this.stop();
			return new DrawLoopInstruction(this.workflow, this);
		}
		return null;
	}

	@Override
	public String getMessage() {
		return "Draw your ROI";
	}

	@Override
	public String getRoiName() {
		if (!this.workflow.getController().isOver()) {
			return this.workflow.getController().getVue().getTextfield_instructions().getText();
		}
		System.out.println(this.workflow.getController().getModel().getRoiManager().getRoi(this.roiToDisplay()).getName());
		return this.workflow.getController().getModel().getRoiManager().getRoi(this.roiToDisplay()).getName();
	}

	@Override
	public boolean saveRoi() {
		return true;
	}

	@Override
	public boolean isRoiVisible() {
		return true;
	}

	@Override
	public int roiToDisplay() {
		return this.indexRoiToDisplay;
	}

	@Override
	public void setRoi(int index) {
		this.indexRoiToDisplay = index;
	}
	

}
