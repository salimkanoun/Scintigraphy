package org.petctviewer.scintigraphy.hepatic.radioEmbolization;

import java.awt.Color;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.DisplayState;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.ImageStack;
import java.awt.event.ItemEvent;
import java.util.Arrays;



public class ControllerWorkflowLiver extends ControllerWorkflow implements ItemListener{

	private static final int SLICE_ANT = 1, SLICE_POST = 2;
	private List<ImagePlus> captures;
	private DisplayState display;

	public ControllerWorkflowLiver (FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelLiver(selectedImages, vue.getStudyName()));
        
        this.display = DisplayState.ANT_POST;

		this.generateInstructions();
		this.start();	
	}
	
	private void computeModel() {
        ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ModelLiver.IMAGE_LIVER_LUNG),
                statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_LR, ModelLiver.IMAGE_LIVER_LUNG);
        final int NB_ROI_PER_IMAGE = 3;
        // Ant then Post
        for (int i=0; i<2; i++) {
            ImageState state;
            if (i==0) state = stateAnt;
            else state = statePost;
            // - Right lung
            getModel().addData(ModelLiver.REGION_RIGHT_LUNG, state,
                    getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i]);
            // - Left lung
            getModel().addData(ModelLiver.REGION_LEFT_LUNG, state,
                    getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i+1]);
            // - Liver
            getModel().addData(ModelLiver.REGION_LIVER, state,
                    getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i+2]);
        }
	}

	/**
	 * This method manage the creation of the ROIs: how many they'll be, the state of them (ANT/POST),
	 * generate them from the previous ones, and add them to the list.
	 * Finally, the EndInstruction() launches the following step, the end() method.
	 */

	protected void generateInstructions() {

        this.workflows = new Workflow[this.model.getImageSelection().length];

        DrawRoiInstruction dri_1, dri_2, dri_3;
		this.captures = new ArrayList<>(4); 
		
        this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

        ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ImageState.ID_NONE);
        ImageState statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_LR, ImageState.ID_NONE);

        // le POST
        dri_1 = new DrawRoiInstruction(ModelLiver.REGION_RIGHT_LUNG, stateAnt);
        dri_2 = new DrawRoiInstruction(ModelLiver.REGION_LEFT_LUNG, stateAnt);
        dri_3 = new DrawRoiInstruction(ModelLiver.REGION_LIVER, stateAnt);
        

        // Image Lung-Liver
        this.workflows[0].addInstruction(dri_1);
        this.workflows[0].addInstruction(dri_2);
        this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));
		//creation of the ANT based on the POST
        this.workflows[0].addInstruction(new DrawRoiInstruction(ModelLiver.REGION_RIGHT_LUNG, statePost));
        this.workflows[0].addInstruction(new DrawRoiInstruction(ModelLiver.REGION_LEFT_LUNG, statePost));
        this.workflows[0].addInstruction(new DrawRoiInstruction(ModelLiver.REGION_LIVER, statePost));
        this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));
        this.workflows[0].addInstruction(new EndInstruction());
	}
	
	
	/** 
	 * @return ModelLiver
	 */
	public ModelLiver getModel() {
        return (ModelLiver) super.getModel();
	}
	
	/**
	 * This method is called just after finishing the drawing of the ROIs.
	 * It calculates the results, and generates a "montage", where the ROIs are placed.
	 * Finally, it launches the window where we can see the results and the ROIs.
	 */

    @Override
    protected void end(){
        super.end();

		this.computeModel();
        this.model.calculateResults();

        //Save captures
        ImagePlus[] impCapture = new ImagePlus[2];
        impCapture[0] = this.captures.get(0);
        impCapture[1] = this.captures.get(1);
        ImageStack stackCapture = Library_Capture_CSV.captureToStack(impCapture);
		ImagePlus montage = this.montageForTwo(stackCapture);
		

        // Display result
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
                "The state misses the required data: -facingOrientation=" + state.getFacingOrientation() + "; " +
                        state.getSlice());
		if (state.getSlice() <= ImageState.SLICE_PREVIOUS) throw new IllegalArgumentException("The slice is invalid");

        if (state.isLateralisationRL()) {
			System.out.println("Orientation "+state.getFacingOrientation().toString());
			if (state.getFacingOrientation() == Orientation.ANT) {
				Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
											state.getSlice());
				Library_Gui.setOverlayTitle(display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
											state.getSlice());
			} else {
				if (state.getFacingOrientation() == Orientation.POST){
					Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
							state.getSlice());
					Library_Gui.setOverlayTitle(display.getTitlePost(), this.vue.getImagePlus(),
											Color.YELLOW, state.getSlice());
				}
	
			}
		} else {
			if (state.getFacingOrientation() == Orientation.ANT) {
				Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textR, display.textL,
											state.getSlice());
				Library_Gui.setOverlayTitle(display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
											state.getSlice());
			} else {
				Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textR, display.textL,
						state.getSlice());
				Library_Gui.setOverlayTitle(display.getTitlePost(), this.vue.getImagePlus(), Color.YELLOW,
											state.getSlice());
			}
		}
    }

    
	/** 
	 * @param e
	 */
	public void itemStateChanged(ItemEvent e){
        if (e.getStateChange() == ItemEvent.SELECTED){
            this.display = DisplayState.stateFromLabel((String) e.getItem());
			this.getVue().getImagePlus().getOverlay().clear();
            this.setOverlay(this.currentState);
            this.getVue().getImagePlus().updateAndDraw();
        }
	}
}