package org.petctviewer.scintigraphy.esophageus.application;

import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

public class ControllerWorkflowEsophagealTransit extends ControllerWorkflow {

	public ControllerWorkflowEsophagealTransit(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1;

		ImageState stateAnt;

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			stateAnt = new ImageState(Orientation.ANT, (i+1), true, ImageState.ID_NONE);
			this.workflows[i] = new Workflow(this, ((EsophagealTransit) this.main).getImgPrjtAllAcqui());
			dri_1 = new DrawRoiInstruction("Esophageal", stateAnt);
			this.workflows[i].addInstruction(dri_1);
		}

		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());

	}
	
	@Override
	public void end() {
		model.calculerResultats();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(
				((Modele_EsophagealTransit) model).getExamenMean(), ((Modele_EsophagealTransit) model).getDicomRoi(),
				((Modele_EsophagealTransit) model), "Esophageal Transit", this);
		fen.pack();
		fen.setLocationRelativeTo(null);
		fen.setVisible(true);
	}

}
