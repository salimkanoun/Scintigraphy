package org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabOtherMethod;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

import ij.ImagePlus;

public class ControllerWorkflowHepaticDyn extends ControllerWorkflow {

	private List<ImagePlus> captures;

	private TabResult resultTab;

	public ControllerWorkflowHepaticDyn(Scintigraphy main, FenApplication vue, ModeleScin model, TabResult resultTab) {
		super(main, vue, model);
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

		dri_1 = new DrawRoiInstruction("Right Liver", stateAnt, null);
		dri_2 = new DrawRoiInstruction("Left Liver", stateAnt, null);
		dri_3 = new DrawRoiInstruction("Hilium", stateAnt, null);
		dri_4 = new DrawRoiInstruction("CBD", stateAnt, null);
		dri_5 = new DrawRoiInstruction("Duodenom", stateAnt, null);
		dri_6 = new DrawRoiInstruction("Blood pool", stateAnt, null);
		dri_capture = new ScreenShotInstruction(captures, this.getVue());

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
		((TabOtherMethod) this.resultTab).setExamDone(true);
		this.resultTab.reloadDisplay();
		this.main.getFenApplication().dispose();
	}

	public ModeleScin getModel() {
		return this.model;
	}

}
