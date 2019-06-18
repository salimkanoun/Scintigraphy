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
	public void end() {
		super.end();
		model.calculateResults();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(
				((Model_EsophagealTransit) model).getExamenMean(), ((Model_EsophagealTransit) model).getDicomRoi(),
				((Model_EsophagealTransit) model), "Esophageal Transit", this);
		fen.setVisible(true);
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];
		this.workflows[0] = new Workflow(this, ((EsophagealTransit) this.main).getImgPrjtAllAcqui());

		for (int indexInstruction = 0; indexInstruction < this.getModel().getImageSelection().length;
			 indexInstruction++) {


			ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state.specifieImage(this.getModel().getImageSelection()[indexInstruction]);
			DrawRoiInstruction dri_1 = null;
			DrawRoiInstruction dri_previous =
					indexInstruction != 0 ? (DrawRoiInstruction) this.workflows[0].getInstructions().get(
							indexInstruction - 1) : null;
			dri_1 = new DrawRoiInstruction("Esophageal", state, dri_previous);
			this.workflows[0].addInstruction(dri_1);
		}
		this.workflows[0].addInstruction(new EndInstruction());
	}

}
