package org.petctviewer.scintigraphy.hepatic.SecondExam;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.hepatic.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.hepatic.tab.TabCurves;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflowHepaticDyn extends ControllerWorkflow {

	private final TabResult resultTab;

	public ControllerWorkflowHepaticDyn(FenApplicationWorkflow vue, ModelScin model, TabResult resultTab) {
		super(null, vue, model);

		this.resultTab = resultTab;

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];

		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_5, dri_6;
		ScreenShotInstruction dri_capture;
		List<ImagePlus> captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[1]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction("Right Liver", stateAnt);
		dri_2 = new DrawRoiInstruction("Left Liver", stateAnt);
		dri_3 = new DrawRoiInstruction("Hilium", stateAnt);
		dri_4 = new DrawRoiInstruction("CBD", stateAnt);
		dri_5 = new DrawRoiInstruction("Duodenum", stateAnt);
		dri_6 = new DrawRoiInstruction("Blood pool", stateAnt);
		dri_capture = new ScreenShotInstruction(captures, this.getVue(), 0);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(dri_5);
		this.workflows[0].addInstruction(dri_6);
		this.workflows[0].addInstruction(dri_capture);

		this.workflows[0].addInstruction(new EndInstruction());

	}

	@Override
	public void end() {

		ModelHepaticDynamic modele = (ModelHepaticDynamic) this.model;

		modele.setCapture(this.vue.getImagePlus());

		modele.setLocked(false);

		// on copie les roi sur toutes les slices
		modele.saveValues();

		// remove finish
		((TabCurves) this.resultTab).setExamDone(true);

		this.resultTab.reloadDisplay();
		this.resultTab.getParent().pack();

		this.vue.dispose();
		
//		for(Roi roi : modele.getRoiManager().getRoisAsArray())
//			this.resultTab.getParent().getModel().getRoiManager().addRoi(roi);
		this.resultTab.getParent().setNewControllerForCaptureButton(this);
	}

	public ModelScin getModel() {
		return this.model;
	}

}
