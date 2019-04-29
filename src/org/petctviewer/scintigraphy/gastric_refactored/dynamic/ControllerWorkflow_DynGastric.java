package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;

public class ControllerWorkflow_DynGastric extends ControllerWorkflow {

	public ControllerWorkflow_DynGastric(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
	}

	@Override
	protected void generateInstructions() {
		DrawRoiInstruction dri_1 = null, dri_2 = null;
		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow();

			dri_1 = new DrawRoiInstruction("Stomach", new ImageState(ImageState.ID_NONE, 1), dri_1);
			dri_2 = new DrawRoiInstruction("Intestine", new ImageState(ImageState.ID_NONE, 1), dri_2);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2, "Antre"));
			this.workflows[i].addInstruction(new PromptInstruction(new PromptBkgNoise(this)));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

}
