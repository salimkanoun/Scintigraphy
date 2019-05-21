package org.petctviewer.scintigraphy.colonic;

import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.cardiac.FenResultat_Cardiac;
import org.petctviewer.scintigraphy.cardiac.Modele_Cardiac;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

public class ControllerWorkflowColonicTransit extends ControllerWorkflow {

	public ControllerWorkflowColonicTransit(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);



		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[3];
		
		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);
		
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null, dri_7 = null;
		ImageState state_1;
		
		state_1 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
		state_1.specifieImage(this.workflows[0].getImageAssociated());
		
		dri_1 = new DrawRoiInstruction("Zone 1", state_1);
		dri_2 = new DrawRoiInstruction("Zone 2", state_1);
		dri_3 = new DrawRoiInstruction("Zone 3", state_1);
		dri_4 = new DrawRoiInstruction("Zone 4", state_1);
		dri_5 = new DrawRoiInstruction("Zone 5", state_1);
		dri_6 = new DrawRoiInstruction("Zone 6", state_1);
		dri_7 = new DrawRoiInstruction("Zone 7", state_1);
	
		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(dri_5);
		this.workflows[0].addInstruction(dri_6);
		this.workflows[0].addInstruction(dri_7);

		this.workflows[0].addInstruction(new EndInstruction());

	}
	
	@Override
	public void end() {

		((ModelColonicTransit) this.model).getResults();
		((ModelColonicTransit) this.model).calculerResultats();

		BufferedImage capture = Library_Capture_CSV.captureImage(this.main.getFenApplication().getImagePlus(), 512, 0)
				.getBufferedImage();
		
		new FenResultsColonicTransit(this);
	}

}
