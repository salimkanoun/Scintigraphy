package org.petctviewer.scintigraphy.colonic;

import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.IsotopeDialog;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

public class ControllerWorkflowColonicTransit extends ControllerWorkflow {

	public ControllerWorkflowColonicTransit(Scintigraphy main, FenApplicationWorkflow vue,
			ImageSelection[] selectedImages) {
		super(main, vue, new ModelColonicTransit(selectedImages, main.getStudyName()));
		
		Isotope isotope = Library_Dicom.findIsotope(selectedImages[0].getImagePlus());
		if (isotope == null) {
			IsotopeDialog dialog = new IsotopeDialog(this.vue);
			isotope = dialog.getIsotope();
		}
		
		((ModelColonicTransit)this.model).setIsotope(isotope);

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length - 1];

		for (int i = 0; i < this.workflows.length; i++) {
			this.workflows[i] = new Workflow(this, this.model.getImageSelection()[i + 1]);

			DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null;
			ImageState state_1;

			state_1 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state_1.specifieImage(this.workflows[i].getImageAssociated());

			dri_1 = new DrawRoiInstruction("Ascending colon & cecum", state_1);
			dri_2 = new DrawRoiInstruction("Hepatic flexure", state_1);
			dri_3 = new DrawRoiInstruction("Transverse colon", state_1);
			dri_4 = new DrawRoiInstruction("Splenic flexure", state_1);
			dri_5 = new DrawRoiInstruction("Descinding colon", state_1);
			dri_6 = new DrawRoiInstruction("Rectosigmoid colon", state_1);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(dri_5);
			this.workflows[i].addInstruction(dri_6);
		}
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());
	}

	@Override
	public void end() {

		((ModelColonicTransit) this.model).getResults();
		((ModelColonicTransit) this.model).calculerResultats();

		BufferedImage capture = Library_Capture_CSV.captureImage(this.main.getFenApplication().getImagePlus(), 512, 0)
				.getBufferedImage();

		new FenResultsColonicTransit(this);
	}

	
	@Override
	public void clicSuivant() {
		super.clicSuivant();
		int currentSlice = this.vue.getImagePlus().getCurrentSlice();
		this.vue.getImagePlus().setSlice(((currentSlice % 2) + 1));
		this.displayRoi(this.workflows[this.indexCurrentWorkflow].getCurrentInstruction().roiToDisplay());
		this.vue.getImagePlus().setSlice((currentSlice));
	}
}
