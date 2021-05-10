package org.petctviewer.scintigraphy.hepatic.scintivol;

import ij.ImagePlus;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;

import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ControllerWorkflow_Scintivol extends ControllerWorkflow {

    public String[] organeListe;

    public ControllerWorkflow_Scintivol(FenApplicationWorkflow vue, ModelScin model) {
        super(vue, model);

        this.generateInstructions();
        this.start();
    }

    @Override
    public void end(){
        super.end();

        Model_Scintivol modele = (Model_Scintivol) this.model;

        modele.getData().clear();

        ImagePlus imp = modele.getImagePlus();

        modele.setLocked(false);

        // capture de l'imageplus ainsi que de l'overlay
        BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 512, 0).getBufferedImage();

        for (int indexSlice = 1; indexSlice <= imp.getStackSize(); indexSlice++) {
            imp.setSlice(indexSlice);
            for (int indexRoi = 0; indexRoi < this.organeListe.length; indexRoi++) {
                imp.setRoi(this.model.getRoiManager().getRoi(indexRoi));
                String nom = this.organeListe[indexRoi];
                modele.enregistrerMesure(nom, imp);

                if (indexSlice == 1) modele.enregistrerPixelRoi(nom, Library_Quantif.getPixelNumber(imp));
            }
        }

        // on recupere les chartPanels avec l'association
        List<XYSeries> series = modele.getSeries();
        String[][] asso = new String[][]{{"Liver", "Heart"}};
        ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);

        Fen_Time fan = new Fen_Time(cp[0], this.getVue(), modele);
        fan.setModal(true);
        fan.setVisible(true);
        fan.toFront();
        ((Model_Scintivol) model).setTimeChart(fan.getValueSetter());


        // on calcule les resultats
        modele.calculateResults();

        // SK On rebloque le modele pour la prochaine generation
        modele.setLocked(true);

        //affichage de la fenetre de résultats principale
        ((Model_Scintivol) model).setTimeChart(fan.getValueSetter());
        FenResults fenResults = new FenResultats_Scintivol(capture, this);
        fenResults.setVisible(true);
    }


    @Override
    protected void generateInstructions() {

        List<String> organes = new LinkedList<>();

        this.workflows = new Workflow[this.model.getImageSelection().length];
        DrawRoiInstruction dri_1, dri_2;
        ScreenShotInstruction dri_capture_1;
        List<ImagePlus> captures = new ArrayList<>();

        ImageSelection dyn1Avg = Library_Dicom.project(this.model.getImageSelection()[0],
                1, this.model.getImageSelection()[0].getImagePlus().getNSlices(), "avg");
        this.workflows[0] = new Workflow(this, dyn1Avg);

        ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_NONE);

        dri_1 = new DrawRoiInstruction("Liver", stateAnt);
        this.workflows[0].addInstruction(dri_1);
        organes.add("Liver");

        dri_2 = new DrawRoiInstruction("Heart", stateAnt);
        this.workflows[0].addInstruction(dri_2);
        organes.add("Heart");

        dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);
        this.workflows[0].addInstruction(dri_capture_1);

        if (this.getModel().getImageSelection().length == 2) {
            this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);

            DrawRoiInstruction dri_3 = new DrawRoiInstruction("Liver parenchyma", stateAnt);
            this.workflows[1].addInstruction(dri_3);
            organes.add("Liver parenchyma");

            ScreenShotInstruction dri_capture_2 = new ScreenShotInstruction(captures, this.getVue(), 1);
            this.workflows[1].addInstruction(dri_capture_2);
        }

        this.workflows[this.workflows.length-1].addInstruction(new EndInstruction());
        this.organeListe = organes.toArray(new String[0]);
    }
}
