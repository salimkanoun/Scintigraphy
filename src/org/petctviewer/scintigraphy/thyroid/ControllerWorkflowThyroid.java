package org.petctviewer.scintigraphy.thyroid;

import java.awt.Color;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
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
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.ContrastEnhancer;

import java.awt.event.ItemEvent;
import java.util.Arrays;


public class ControllerWorkflowThyroid extends ControllerWorkflow implements ItemListener {

    private static final int SLICE_ANT = 1;
    private List<ImagePlus> captures;
    private DisplayState display;

    public ControllerWorkflowThyroid(FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
        super(vue, new ModelThyroid(selectedImages, vue.getStudyName()));

        this.display = DisplayState.ANT_POST;
        this.generateInstructions();
        this.start();
    }

    private void computeModel() {
        ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ModelThyroid.IMAGE_FULL_SYRINGE);
        final int NB_ROI_PER_IMAGE = 0;

        this.getModel().addData(ModelThyroid.REGION_FULL_SYRINGE, stateAnt,
                getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE]);

        stateAnt.setIdImage(ModelThyroid.IMAGE_EMPTY_SYRINGE);
        this.getModel().addData(ModelThyroid.REGION_EMPTY_SYRINGE, stateAnt,
                getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE + 1]);
        ImageState state = stateAnt;
        state.setIdImage(ModelThyroid.IMAGE_THYROID);
        // - Right lobe
        getModel().addData(ModelThyroid.REGION_RIGHT_LOBE, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE + 2]);

        // - Left lobe
        getModel().addData(ModelThyroid.REGION_LEFT_LOBE, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE + 3]);

        // - Background left
        getModel().addData(ModelThyroid.REGION_BACKGROUND_LEFT, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE + 4]);

        // - Background right
        getModel().addData(ModelThyroid.REGION_BACKGROUND_RIGHT, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE + 5]);
    }

    /**
     * This method manage the creation of the ROIs: how many they'll be, the state of them (ANT/POST),
     * generate them from the previous ones, and add them to the list.
     * Finally, the EndInstruction() launches the following step, the end() method.
     */

    protected void generateInstructions() {
        this.workflows = new Workflow[this.model.getImageSelection().length];

        DrawRoiInstruction dri_1, dri_2, dri_3, dri_4;
        this.captures = new ArrayList<>(5);

        ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ImageState.ID_NONE);

        //Syringes
        dri_1 = new DrawRoiInstructionContrast(ModelThyroid.REGION_FULL_SYRINGE, stateAnt, 0.5f);
        dri_2 = new DrawRoiInstructionContrast(ModelThyroid.REGION_EMPTY_SYRINGE, stateAnt, 0.5f);

        //Thyroid
        dri_3 = new DrawRoiInstructionContrast(ModelThyroid.REGION_RIGHT_LOBE, stateAnt, 0.5f);
        dri_4 = new DrawRoiInstructionContrast(ModelThyroid.REGION_LEFT_LOBE, stateAnt, 0.5f);


        //Image Full Syringe
        this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);
        this.workflows[0].addInstruction(dri_1);
        this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));

        //Image Empty Syringe
        this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
        this.workflows[1].addInstruction(dri_2);
        this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));


        //Image Thyroid Ant
        this.workflows[2] = new Workflow(this, this.model.getImageSelection()[2]);
        this.workflows[2].addInstruction(dri_3);
        this.workflows[2].addInstruction(dri_4);
        this.workflows[2].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 2));
        this.workflows[2].addInstruction(new DrawRoiOnSideRight(ModelThyroid.REGION_BACKGROUND_RIGHT, stateAnt, dri_3));
        this.workflows[2].addInstruction(new DrawRoiOnSideLeft(ModelThyroid.REGION_BACKGROUND_LEFT, stateAnt, dri_4));
        this.workflows[2].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 3));

        this.workflows[2].addInstruction(new EndInstruction());
    }

    /**
     * @return ModelThyroid
     */
    public ModelThyroid getModel() {
        return (ModelThyroid) super.getModel();
    }

    /**
     * This method is called just after finishing the drawing of the ROIs.
     * It calculates the results, and generates a "montage", where the ROIs are placed.
     * Finally, it launches the window where we can see the results and the ROIs.
     */
    @Override
    protected void end() {
        super.end();

        this.computeModel();
        this.model.calculateResults();

        //Save captures
        ImagePlus[] impCapture = new ImagePlus[4];
        impCapture[0] = this.captures.get(0);
        impCapture[1] = this.captures.get(1);
        impCapture[2] = this.captures.get(2);
        impCapture[3] = this.captures.get(3);
        ImageStack stackCapture = Library_Capture_CSV.captureToStack(impCapture);
        ImagePlus montage = this.montage(stackCapture);

        //Display result
        FenResults fenResults = new FenResults(this);
        fenResults.setMainTab(new MainResult(fenResults, montage));

        fenResults.pack();
        fenResults.setVisible(true);

    }

    /**
     * @param state
     * @throws IllegalArgumentException
     */
    @Override
    public void setOverlay(ImageState state) throws IllegalArgumentException {
        if (state == null) throw new IllegalArgumentException("The state cannot be null");
        if (state.getFacingOrientation() == null) throw new IllegalArgumentException(
                "The state misses the required data: -facingOrientation=" + state.getFacingOrientation() + "; "
                        + state.getSlice());
        if (state.getSlice() <= ImageState.SLICE_PREVIOUS) throw new IllegalArgumentException("The slice is invalid");

        if (!state.isLateralisationLR()) {
            if (state.getFacingOrientation() == Orientation.ANT) {
                Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
                        state.getSlice());

                Library_Gui.setOverlayTitle(display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
                        state.getSlice());
            }
        } else {
            if (state.getFacingOrientation() == Orientation.ANT) {
                Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textR, display.textL,
                        state.getSlice());
                Library_Gui.setOverlayTitle("Inverted " + display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
                        state.getSlice());
            }
        }
    }

    /**
     * @param e
     */
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            this.display = DisplayState.stateFromLabel((String) e.getItem());
            this.getVue().getImagePlus().getOverlay().clear();
            this.setOverlay(this.currentState);
            this.getVue().getImagePlus().updateAndDraw();
        }
    }

    //TODO This enum must change from a local state to a more general one and then be called

    public enum DisplayState {
        RIGHT_LEFT("Label ANT as RIGHT", "P", "A", "Right-Left"),
        LEFT_RIGHT("Label ANT as LEFT", "A", "P", "Left-Right"),
        ANT_POST("Label ANT as ANT", "R", "L", "Ant-Post");

        public String label, textL, textR;
        private String title;

        DisplayState(String label, String textL, String textR, String titleAP) {
            this.label = label;
            this.textL = textL;
            this.textR = textR;
            this.title = titleAP;
        }

        /**
         * Finds the state associated with the specified label. If not state matches this label, then the ANT_POST
         * state is returned.
         *
         * @param label Label of the state to retrieve
         * @return state corresponding to the specified label or ANT_POST if no state matches
         */
        public static DisplayState stateFromLabel(String label) {
            return Arrays.stream(values()).filter(state -> state.label.equals(label)).findFirst().orElse(ANT_POST);
        }

        public String getTitleAnt() {
            return this.title.split("-")[0];
        }
    }

    private class DrawRoiOnSideRight extends DrawRoiInstruction {
        private static final long serialVersionUID = 1L;

        private final transient DrawRoiInstruction dri;

        public DrawRoiOnSideRight(String organToDelimit, ImageState state, DrawRoiInstruction roi) {
            super(organToDelimit, state);
            this.dri = roi;
        }

        @Override
        public void afterNext(ControllerWorkflow controller) {
            super.afterNext(controller);
            Roi r1 = getRoiManager().getRoi(this.dri.getRoiIndex());
            ImagePlus ip = getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus();
            Roi roiBackground = Library_Roi.createBkgRoi(r1, ip, Library_Roi.INFLATGAUCHE);
            ip.setRoi(roiBackground);
        }
    }

    private class DrawRoiOnSideLeft extends DrawRoiInstruction {
        private static final long serialVersionUID = 1L;

        private final transient DrawRoiInstruction dri;

        public DrawRoiOnSideLeft(String organToDelimit, ImageState state, DrawRoiInstruction roi) {
            super(organToDelimit, state);
            this.dri = roi;
        }

        @Override
        public void afterNext(ControllerWorkflow controller) {
            super.afterNext(controller);
            Roi r1 = getRoiManager().getRoi(this.dri.getRoiIndex());
            ImagePlus ip = getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus();
            Roi roiBackground = Library_Roi.createBkgRoi(r1, ip, Library_Roi.INFLATDROIT);
            ip.setRoi(roiBackground);
        }
    }

    /**
     * This class set the default contrast at 0.5 at each new Instruction
     */
    private class DrawRoiInstructionContrast extends DrawRoiInstruction {

        private static final long serialVersionUID = 1L;

        public DrawRoiInstructionContrast(String organToDelimit, ImageState state, float contraste) {
            super(organToDelimit, state);
        }

        @Override
        public void afterNext(ControllerWorkflow controller) {
            super.afterNext(controller);
            ContrastEnhancer ce = new ContrastEnhancer();
            ImagePlus ip = getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus();
            ce.stretchHistogram(ip, 0.5f);
        }
    }

}