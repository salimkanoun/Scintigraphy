package org.petctviewer.scintigraphy.esophageus.application;

import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

public class ControllerWorkflowEsophagealTransit extends ControllerWorkflow {

	public ControllerWorkflowEsophagealTransit(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model) {
		super(main, vue, model);

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];
		this.workflows[0] = new Workflow(this, ((EsophagealTransit) this.main).getImgPrjtAllAcqui());

		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4;

		dri_1 = new DrawRoiInstruction("Esophageal", new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE));
		dri_2 = new DrawRoiInstruction("Esophageal", new ImageState(Orientation.ANT, 2, true, ImageState.ID_NONE), dri_1);
		dri_3 = new DrawRoiInstruction("Esophageal", new ImageState(Orientation.ANT, 3, true, ImageState.ID_NONE), dri_2);
		dri_4 = new DrawRoiInstruction("Esophageal", new ImageState(Orientation.ANT, 4, true, ImageState.ID_NONE), dri_3);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(new EndInstruction());

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());
	}

	@Override
	public void end() {
		model.calculateResults();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(
				((Model_EsophagealTransit) model).getExamenMean(), ((Model_EsophagealTransit) model).getDicomRoi(),
				((Model_EsophagealTransit) model), "Esophageal Transit", this);
		fen.setVisible(true);
	}

}
