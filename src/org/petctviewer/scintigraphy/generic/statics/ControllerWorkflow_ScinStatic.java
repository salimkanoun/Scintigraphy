package org.petctviewer.scintigraphy.generic.statics;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

public class ControllerWorkflow_ScinStatic extends ControllerWorkflow {

	public ControllerWorkflow_ScinStatic(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new ModeleScinStatic(selectedImages, studyName));
	}

	public String getNomOrgane(int index) {
		return this.vue.getTextfield_instructions().getText();
	}

	@Override
	protected void generateInstructions() {
		
	}

}
