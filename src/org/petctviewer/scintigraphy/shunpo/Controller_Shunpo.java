package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.WorkflowGenerator;

public class Controller_Shunpo extends ControllerWorkflow {

	public Controller_Shunpo(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages, String studyName) {
		super(main, vue, new ModeleShunpo(selectedImages, studyName));
	}

	@Override
	protected void generateInstructions() {
		this.workflows = WorkflowGenerator.multipleImagesSimpleWorkflow(new String[][] {
				{ "Right lung", "Left lung", "Right kidney", "Left kidney", "Background" }, { "Brain" } });
	}

}
