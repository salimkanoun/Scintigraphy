package org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabCurves;
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

import ij.ImagePlus;

public class ControllerWorkflowHepaticDyn extends ControllerWorkflow {

	private List<ImagePlus> captures;

	private TabResult resultTab;

	public ControllerWorkflowHepaticDyn(FenApplicationWorkflow vue, ModelScin model, TabResult resultTab) {
		super(null, vue, model);

		// TODO Auto-generated constructor stub
		this.resultTab = resultTab;

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null;
		ScreenShotInstruction dri_capture = null;
		this.captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, ((ModelSecondMethodHepaticDynamic) this.model).getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction("Right Liver", stateAnt);
		dri_2 = new DrawRoiInstruction("Left Liver", stateAnt);
		dri_3 = new DrawRoiInstruction("Hilium", stateAnt);
		dri_4 = new DrawRoiInstruction("CBD", stateAnt);
		dri_5 = new DrawRoiInstruction("Duodenom", stateAnt);
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

		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) this.model;

		modele.setCapture(this.vue.getImagePlus());

		modele.setLocked(false);

		// on copie les roi sur toutes les slices
		modele.saveValues();

		// remove finish
		((TabCurves) this.resultTab).setExamDone(true);

		this.resultTab.reloadDisplay();

		this.vue.dispose();
	}

	public ModelScin getModel() {
		return this.model;
	}

}
