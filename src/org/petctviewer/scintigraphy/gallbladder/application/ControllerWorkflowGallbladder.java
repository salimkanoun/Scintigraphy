package org.petctviewer.scintigraphy.gallbladder.application;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.gallbladder.resultats.FenResultats_Gallbladder;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.awt.image.BufferedImage;
import java.util.List;

public class ControllerWorkflowGallbladder extends ControllerWorkflow{

    private List<ImagePlus> captures;

    public ControllerWorkflowGallbladder(FenApplicationWorkflow vue, ModelScin model){
        super(vue, model);
        this.generateInstructions();
        this.start();
    }


    @Override
    public void end(){
        super.end();
        ModelGallbladder modele = (ModelGallbladder) this.model;
        modele.setLocked(true);
        // capture de l'imageplus ainsi que de l'overlay
        BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 512, 0).getBufferedImage();

        modele.calculateResults();



        FenResults fen = new FenResultats_Gallbladder(capture, this);
        fen.setVisible(true);
    }

    @Override
    protected void generateInstructions(){
        this.workflows = new Workflow[this.model.getImageSelection().length];

        ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);

        ImageSelection dynAvg = Library_Dicom.project(this.model.getImageSelection()[0],
                1, this.model.getImageSelection()[0].getImagePlus().getNSlices(), "avg");
        state.specifieImage(dynAvg);
        this.workflows[0] = new Workflow(this, dynAvg);
        DrawRoiInstruction dri_1, dri_2;

        dri_1 = new DrawRoiInstruction("Gallbladder", state);
        dri_2 = new DrawRoiInstruction("Liver", state);
        this.workflows[0].addInstruction(dri_1);
        this.workflows[0].addInstruction(dri_2);

        this.workflows[0].addInstruction(new EndInstruction());
    }
}