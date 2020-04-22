package org.petctviewer.scintigraphy.thyroid;

import java.awt.Color;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.hepatic.radioEmbolization.ModelLiver;
import org.petctviewer.scintigraphy.hepatic.radioEmbolization.ControllerWorkflowLiver.DisplayState;
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

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

import java.awt.event.ItemEvent;
import java.util.Arrays;



public class ControllerWorkflowThyroid extends ControllerWorkflow implements ItemListener{

    private static final int SLICE_ANT = 1, SLICE_POST = 2;
    private List<ImagePlus> captures;
    private DisplayState display;

    public ControllerWorkflowThyroid(FenApplicationWorkflow vue, ImageSelection[] selectedImages){
        super(vue, new ModelThyroid(selectedImages, vue.getStudyName()));

        this.display = DisplayState.ANT_POST;
        this.generateInstructions();
        this.start();
    }

    private void computeModel(){
        ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ModelThyroid.IMAGE_THYROID),
        statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_RL, ModelThyroid.IMAGE_THYROID);
        final int NB_ROI_PER_IMAGE = 3;
        //Post then Ant
        for(int i = 0; i<2; i++){
            ImageState state;
            if( i==0 )state = statePost;
            else state = stateAnt;

            // - Right lobe
            getModel().addData(ModelThyroid.REGION_RIGHT_LOBE, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i];

            // - Left lobe
            getModel().addData(ModelThyroid.REGION_LEFT_LOBE, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i+1]);

            // - Background
            getModel().addData(ModelThyroid.REGION_BACKGROUND, state, getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 2]);
        }
    }

    /**
	 * This method manage the creation of the ROIs: how many they'll be, the state of them (ANT/POST),
	 * generate them from the previous ones, and add them to the list.
	 * Finally, the EndInstruction() launches the following step, the end() method.
	 */

     protected void generateInstructions(){
         this.workflows = new Workflow[this.model.getImageSelection().length];

         DrawRoiInstruction dri_1, dri_2, dri_3, dri_4;
         this.captures = new ArrayList<>(4);

         this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

         ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
         ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);

         //POST
         dri_1 = new DrawRoiInstruction(ModelThyroid.REGION_RIGHT_LOBE, statePost);
         dri_2 = new DrawRoiInstruction(ModelThyroid.REGION_LEFT_LOBE, statePost);

         //ANT
         dri_3 = new DrawRoiInstruction(ModelThyroid.REGION_RIGHT_LOBE, stateAnt, dri_1);
         dri_4 = new DrawRoiInstruction(ModelThyroid.REGION_LEFT_LOBE, stateAnt, dri_2);

         //Image Thyroid
         this.workflows[0].addInstruction(dri_1);
         this.workflows[0].addInstruction(dri_2);
         this.workflows[0].addInstruction(new DrawRoiInMiddle(ModelThyroid.REGION_BACKGROUND, statePost, dri_1, dri_2));
         this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));
         
         //Creation of the ANT based on the POST
         this.workflows[0].addInstruction(dri_3);
         this.workflows[0].addInstruction(dri_4);
         this.workflows[0].addInstruction(new DrawRoiInMiddle(ModelThyroid.REGION_BACKGROUND, stateAnt, dri_3, dri_4));
         this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));
         this.workflows[0].addInstruction(new EndInstruction());
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
     protected void end(){
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
      public void setOverlay(ImageState state) throws IllegalArgumentException{
          if(state == null) throw new IllegalArgumentException("The state cannot be null");
          if(state.getFacingOrientation() == null) throw new IllegalArgumentException(
              "The state misses the required data: -facingOrientation=" + state.getFacingOrientation() + "; "
              + state.getSlice());
          if(state.getSlice() <= ImageState.SLICE_PREVIOUS) throw new IllegalArgumentException("The slice is invalid");
          
          if(state.isLateralisationLR()){
              if(state.getFacingOrientation() == Orientation.ANT){
                  Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
                  state.getSlice());

                  Library_Gui.setOverlayTitle(display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
                  state.getSlice());
              } else {
                  Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
                  state.getSlice());
                  Library_Gui.setOverlayTitle("Inverted " + display.getTitlePost(), this.vue.getImagePlus(), Color.YELLOW,
                  state.getSlice());
              }
          } else {
			if (state.getFacingOrientation() == Orientation.ANT) {
				Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textR, display.textL,
											state.getSlice());
				Library_Gui.setOverlayTitle("Inverted " + display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
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
        if(e.getStateChange() == ItemEvent.SELECTED){
            this.display = DisplayState.stateFromLabel((String) e.getItem());
            this.getVue().getImagePlus().getOverlay().clear();
            this.setOverlay(this.currentState);
            this.getVue().getImagePlus().updateAndDraw();
        }
    }

    //TODO This enum must change from a local state to a more general one and then be called

    public enum DisplayState{
        RIGHT_LEFT("Label ANT as RIGHT", "P", "A", "Right-Left"),
        LEFT_RIGHT("Label ANT as LEFT", "A", "P", "Left-Right"),
        ANT_POST("Label ANT as ANT", "R", "L", "Ant-Post");

        public String label, textL, textR;
        private String title;

        DisplayState(String label, String textL, String textR, String titleAP){
            this.label = label;
            this.textL = textL;
            this.textR = textR;
            this.title = titleAP;
        }

        /**
         * Finds the state associated with the specified label. If not state matches this label, then the ANT_POST
         * state is returned.
         * @param label Label of the state to retrieve
         * @return state corresponding to the specified label or ANT_POST if no state matches
         */
        public static DisplayState stateFromLabel(String label){
            return Arrays.stream(values()).filter(state -> state.label.equals(label)).findFirst().orElse(ANT_POST);
        }

        public String getTitleAnt(){
            return this.title.split("-")[0];
        }

        public String getTitlePost(){
            return this.title.split("-")[1];
        }
    }

    private class DrawRoiInMiddle extends DrawRoiInstruction{
        private static final long serialVersionUID = 1L;

        private final transient DrawRoiInstruction dri_1;
        private final transient DrawRoiInstruction dri_2;

        public DrawRoiInMiddle(String organToDelimit, ImageState state, DrawRoiInstruction roi1, DrawRoiInstruction roi2){
            super(organToDelimit, state);
            this.dri_1 = roi1;
            this.dri_2 = roi2;
        }

        @Override
        public void afterNext(ControllerWorkflow controller){
            super.afterNext(controller);
            Roi r1 = getRoiManager().getRoi(this.dri_1.getRoiIndex());
            Roi r2 = getRoiManager().getRoi(this.dri_2.getRoiIndex());
            controller.getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus().setRoi(
                roiBetween(r1, r2));
        }
    }
}