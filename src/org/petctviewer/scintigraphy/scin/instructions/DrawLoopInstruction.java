package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.generic.statics.ControllerWorkflow_ScinStatic;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;

public class DrawLoopInstruction extends DefaultGenerator {

	private int indexRoiToDisplay;

	private ImageState state;

	private String RoiName;

	private String suffixe;

	public DrawLoopInstruction(Workflow workflow, ImageState state) {
		this(workflow, null, state);
	}

	protected DrawLoopInstruction(Workflow workflow, GeneratorInstruction parent, ImageState state) {
		super(workflow, parent);
		this.indexRoiToDisplay = -1;
		
		this.suffixe="";
		
		if (state == null)
			this.state = parent.getImageState();
		else
			this.state = state;
		
	}

	@Override
	public Instruction generate() {
		if (!this.isStopped) {
			this.stop();
			return new DrawLoopInstruction(this.workflow, this, null);
		}
		return null;
	}

	@Override
	public String getMessage() {
		return this.workflow.getController().getModel().getRoiManager().getRoi(this.roiToDisplay()) != null
				? this.RoiName
				: "Draw your Roi";

	}

	@Override
	public String getRoiName() {
		if (!this.workflow.getController().isOver()) {
			int i = 0;
			boolean depasse = false;
			for (Instruction instruction : this.workflow.getInstructions()) {
				if (instruction instanceof DrawLoopInstruction) {
					if(this == instruction)
						depasse = true;
					if (this.workflow.getController().getVue().getTextfield_instructions().getText()
							.equals(((DrawLoopInstruction) instruction).getInstructionRoiName()) && !depasse)
						i++;
				}
			}
			this.RoiName = this.workflow.getController().getVue().getTextfield_instructions().getText();
			if (i != 0)
				this.suffixe = "_" + i;
			return this.RoiName + this.suffixe;
		}
		return this.RoiName;
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

	@Override
	public ImageState getImageState() {
		return this.state;
	}

	public String getInstructionRoiName() {
		return this.RoiName;
	}

}