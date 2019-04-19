package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ExecutionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;

public class ControllerWorkflow_Gastric extends ControllerWorkflow {

	public ControllerWorkflow_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));
	}

	@Override
	protected void generateInstructions() {
		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow();
			this.workflows[i].addInstruction(new DrawRoiInstruction("Stomach", Orientation.ANT,
					(DrawRoiInstruction) (i - 1 >= 0 ? this.workflows[i - 1].getInstructionAt(3) : null)));
			this.workflows[i].addInstruction(new DrawRoiInstruction("Intestine", Orientation.ANT,
					(DrawRoiInstruction) (i - 1 >= 0 ? this.workflows[i - 1].getInstructionAt(4) : null)));
			this.workflows[i].addInstruction(new ExecutionInstruction() {
				@Override
				public void afterNext(ControllerWorkflow controller) {
					controller.resetOverlay();
				}

				@Override
				public void afterPrevious(ControllerWorkflow controller) {
					controller.resetOverlay();
				}
			});
			this.workflows[i].addInstruction(new DrawRoiInstruction("Stomach", Orientation.POST,
					(DrawRoiInstruction) this.workflows[i].getInstructionAt(0)));
			this.workflows[i].addInstruction(new DrawRoiInstruction("Intestine", Orientation.POST,
					(DrawRoiInstruction) this.workflows[i].getInstructionAt(1)));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

}
