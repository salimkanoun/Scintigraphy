package org.petctviewer.scintigraphy.hepatic.radioEmbolization;

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
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.ImageStack;
import java.awt.event.ItemEvent;


public class ControllerWorkflowLiver extends ControllerWorkflow implements ItemListener{

<<<<<<< HEAD
	private static final int SLICE_ANT = 1, SLICE_POST = 2;
	private List<ImagePlus> captures;
	//private DisplayState display;

	public ControllerWorkflowLiver (FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelLiver(selectedImages, vue.getStudyName()));
		
		this.generateInstructions();
		this.start();	
=======
    private static final int SLICE_ANT = 1, SLICE_POST = 2;
    private List<ImagePlus> captures;
    //private DisplayState display;

    public ControllerWorkflowLiver (FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
        super(vue, new ModelLiver(selectedImages, vue.getStudyName()));
        
        this.generateInstructions();
        this.start();    
>>>>>>> 51ab4d140f7d578af5d3b048cd04f7905b52214f
	}
	
	private void computeModel() {
        ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ModelLiver.IMAGE_LIVER_LUNG),
                statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_RL, ModelLiver.IMAGE_LIVER_LUNG);
        final int NB_ROI_PER_IMAGE = 3;
        // Ant then Post
        for (int i=0; i<2; i++) {
            ImageState state;
            if (i==0) state = statePost;
            else state = stateAnt;
            // - Right lung
            getModel().addData(ModelLiver.REGION_RIGHT_LUNG, state,
                    getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i]);
            // - Left lung
            getModel().addData(ModelLiver.REGION_LEFT_LUNG, state,
                    getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i+1]);
            // - Liver
            getModel().addData(ModelLiver.REGION_LIVER, state,
                    getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i+1]);
        }
	}
	
	protected void generateInstructions() {

        this.workflows = new Workflow[this.model.getImageSelection().length];

        DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_5, dri_6;
        this.captures = new ArrayList<>(5);
        this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

        ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
        ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);

        // le POST
        dri_1 = new DrawRoiInstruction(ModelLiver.REGION_LEFT_LUNG, statePost);
        dri_2 = new DrawRoiInstruction(ModelLiver.REGION_RIGHT_LUNG, statePost);
        dri_3 = new DrawRoiInstruction(ModelLiver.REGION_LIVER, statePost);
        //le ANT
        dri_4 = new DrawRoiInstruction(ModelLiver.REGION_LEFT_LUNG, stateAnt, dri_1);
        dri_5 = new DrawRoiInstruction(ModelLiver.REGION_RIGHT_LUNG, stateAnt, dri_2);
        dri_6 = new DrawRoiInstruction(ModelLiver.REGION_LIVER, stateAnt, dri_3);


        // Image Lung-Liver
        this.workflows[0].addInstruction(dri_1);
        this.workflows[0].addInstruction(dri_2);
        this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));
        this.workflows[0].addInstruction(dri_3);
        this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));
        this.workflows[0].addInstruction(dri_4);
        this.workflows[0].addInstruction(dri_5);
        this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 2));
        this.workflows[0].addInstruction(dri_6);
        this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 3));
	}
	
	public ModelLiver getModel() {
        return (ModelLiver) super.getModel();
    }

    @Override
    protected void end(){
        super.end();

        this.computeModel();
        this.model.calculateResults();

        //Save captures
        ImagePlus[] impCapture = new ImagePlus[4];
        impCapture[0] = this.captures.get(1);
        impCapture[1] = this.captures.get(3);
        impCapture[2] = this.captures.get(4);
        impCapture[3] = this.captures.get(5);
        ImageStack stackCapture = Library_Capture_CSV.captureToStack(impCapture);
        ImagePlus montage1 = this.montage(stackCapture);

        // Display result
        FenResults fenResults = new FenResults(this);
        fenResults.setMainTab(new MainResult(fenResults, montage1));

        fenResults.pack();
        fenResults.setVisible(true);
	}
	
	@Override
    public void setOverlay(ImageState state) throws IllegalArgumentException {
        if (state == null) throw new IllegalArgumentException("The state cannot be null");
        if (state.getFacingOrientation() == null) throw new IllegalArgumentException(
                "The state misses the required data: -facingOrientation=" + state.getFacingOrientation() + "; " +
                        state.getSlice());
        if (state.getSlice() <= ImageState.SLICE_PREVIOUS) throw new IllegalArgumentException("The slice is invalid");

        if (state.isLateralisationRL()) {
            if (state.getFacingOrientation() == Orientation.ANT) {
                Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
                Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW,
                                            state.getSlice());
            } else {
                Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
                Library_Gui.setOverlayTitle("Post", this.vue.getImagePlus(), Color.YELLOW,
                                            state.getSlice());
            }
        }
    }

    public void itemStateChanged(ItemEvent e){
        if (e.getStateChange() == ItemEvent.SELECTED){
            this.getVue().getImagePlus().getOverlay().clear();
            this.setOverlay(this.currentState);
            this.getVue().getImagePlus().updateAndDraw();
        }
	}
<<<<<<< HEAD

	@Override
	protected void end(){
		super.end();

		this.computeModel();
		this.model.calculateResults();

		//Save captures
		ImagePlus[] impCapture = new ImagePlus[4];
		impCapture[0] = this.captures.get(1);
		impCapture[1] = this.captures.get(3);
		impCapture[2] = this.captures.get(4);
		impCapture[3] = this.captures.get(5);
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(impCapture);
		ImagePlus montage1 = this.montage(stackCapture);

		// Display result
		FenResults fenResults = new FenResults(this);
		fenResults.setMainTab(new MainResult(fenResults, montage1));

		fenResults.pack();
		fenResults.setVisible(true);
	}

	@Override
	public void setOverlay(ImageState state) throws IllegalArgumentException {
		if (state == null) throw new IllegalArgumentException("The state cannot be null");
		if (state.getFacingOrientation() == null) throw new IllegalArgumentException(
				"The state misses the required data: -facingOrientation=" + state.getFacingOrientation() + "; " +
						state.getSlice());
		if (state.getSlice() <= ImageState.SLICE_PREVIOUS) throw new IllegalArgumentException("The slice is invalid");

		if (state.isLateralisationRL()) {
			if (state.getFacingOrientation() == Orientation.ANT) {
				Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
				Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW,
											state.getSlice());
			} else {
				Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
				Library_Gui.setOverlayTitle("Post", this.vue.getImagePlus(), Color.YELLOW,
											state.getSlice());
			}
		}
	}

	public void itemStateChanged(ItemEvent e){
		if (e.getStateChange() == ItemEvent.SELECTED){
			this.getVue().getImagePlus().getOverlay().clear();
			this.setOverlay(this.currentState);
			this.getVue().getImagePlus().updateAndDraw();
		}
	}



	/*public enum DisplayState {
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
		}*/


=======
>>>>>>> 51ab4d140f7d578af5d3b048cd04f7905b52214f
}