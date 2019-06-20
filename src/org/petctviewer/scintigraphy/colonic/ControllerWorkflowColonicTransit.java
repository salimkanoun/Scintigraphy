package org.petctviewer.scintigraphy.colonic;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.IsotopeDialog;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflowColonicTransit extends ControllerWorkflow {

	private final List<ImagePlus> captures;

	public ControllerWorkflowColonicTransit(FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelColonicTransit(selectedImages, vue.getStudyName()));

		this.captures = new ArrayList<>();

		Isotope isotope = Library_Dicom.findIsotope(selectedImages[0].getImagePlus());
		if (isotope == null) {
			IsotopeDialog dialog = new IsotopeDialog(this.vue);
			isotope = dialog.getIsotope();
		}

		((ModelColonicTransit) this.model).setIsotope(isotope);

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length - 1];

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null;
		ImageState state_1;
		ScreenShotInstruction dri_capture_1;

		for (int i = 0; i < this.workflows.length; i++) {
			// The first image (index = 0) is used to calculated, but without interactions.
			this.workflows[i] = new Workflow(this, this.model.getImageSelection()[i + 1]);

			state_1 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state_1.specifieImage(this.workflows[i].getImageAssociated());

			dri_1 = new DrawRoiInstruction("Ascending colon & cecum", state_1, dri_1);
			dri_2 = new DrawRoiInstruction("Hepatic flexure", state_1, dri_2);
			dri_3 = new DrawRoiInstruction("Transverse colon", state_1, dri_3);
			dri_4 = new DrawRoiInstruction("Splenic flexure", state_1, dri_4);
			dri_5 = new DrawRoiInstruction("Descinding colon", state_1, dri_5);
			dri_6 = new DrawRoiInstruction("Rectosigmoid colon", state_1, dri_6);
			dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), i);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(dri_5);
			this.workflows[i].addInstruction(dri_6);
			this.workflows[i].addInstruction(dri_capture_1);
		}
		
		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());
	}

	@Override
	public void end() {
		super.end();

		((ModelColonicTransit) this.model).getResults();
		this.model.calculateResults();

		int[] times = new int[this.getModel().getImageSelection().length];
		for (int index = 1; index < this.getModel().getImageSelection().length; index++) {
			times[index] = Math.round((Library_Dicom
					.getDateAcquisition(this.getModel().getImageSelection()[index].getImagePlus()).getTime()
					- Library_Dicom.getDateAcquisition(this.getModel().getImageSelection()[0].getImagePlus()).getTime())
					/ 1000 / 3600);
		}

		FenResults fenResults = new FenResultsColonicTransit(this, this.captures, times);
		fenResults.setVisible(true);
	}

	@Override
	public void clickNext() {
		super.clickNext();
		int currentSlice = this.vue.getImagePlus().getCurrentSlice();
		this.vue.getImagePlus().setSlice(((currentSlice % 2) + 1));
		this.displayRoi(this.workflows[this.indexCurrentWorkflow].getCurrentInstruction().getRoiIndex());
		this.vue.getImagePlus().setSlice((currentSlice));
	}
}
