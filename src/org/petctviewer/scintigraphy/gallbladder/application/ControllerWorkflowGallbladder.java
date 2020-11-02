package org.petctviewer.scintigraphy.gallbladder.application;

import org.petctviewer.scintigraphy.gallbladder.resultats.FenResultats_Gallbladder;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

public class ControllerWorkflowGallbladder extends ControllerWorkflow{
    public ControllerWorkflowGallbladder(FenApplicationWorkflow vue, ModelScin model){
        super(vue, model);
        this.generateInstructions();
        this.start();
    }


    @Override
    public void end(){
        super.end();
        model.calculateResults();
        FenResultats_Gallbladder fen = new FenResultats_Gallbladder(((Model_Gallbladder) model).getExamenMean(), ((Model_Gallbladder) model).getDicomRoi(),
        ((Model_Gallbladder) model), "Gallbladder", this);
        fen.setVisible(true);
    }

    @Override
    protected void generateInstructions(){
        this.workflows = new Workflow[this.model.getImageSelection().length];
        this.workflows[0] = new Workflow(this, ((Model_Gallbladder) this.getModel()).getImgPrjtAllAcqui());

        for(int indexInstruction = 0; indexInstruction < this.getModel().getImageSelection().length; indexInstruction++){
            ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
            state.specifieImage(this.getModel().getImageSelection()[indexInstruction]);
            DrawRoiInstruction dri_1 = null;
            DrawRoiInstruction dri_2 = null;
            DrawRoiInstruction dri_previous = indexInstruction != 0 ? (DrawRoiInstruction) this.workflows[0].getInstructions().get(indexInstruction - 1) : null;
            dri_1 = new DrawRoiInstruction("Gallbladder", state, dri_previous);
            dri_2 = new DrawRoiInstruction("Liver", state, dri_previous);
            this.workflows[0].addInstruction(dri_1);
            this.workflows[0].addInstruction(dri_2);
        }
        this.workflows[0].addInstruction(new EndInstruction());
    }
}