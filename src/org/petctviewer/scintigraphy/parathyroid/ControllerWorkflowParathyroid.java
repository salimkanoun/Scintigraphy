package org.petctviewer.scintigraphy.parathyroid;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerWorkflowParathyroid extends ControllerWorkflow implements ItemListener {
	private static final int SLICE_ANT = 1, SLICE_ANT1 = 2;
	private List<ImagePlus> captures;
	private DisplayState display;

	public ControllerWorkflowParathyroid(FenApplicationWorkflow vue, ModelParathyroid model) {
		super(vue, model);

		// Initialize variables
		this.display = DisplayState.ANT_POST;

		this.generateInstructions();
		this.start();
	}

	// TODO: remove this method and do this in the instructions
	private void computeModel() {
		ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL,ModelParathyroid.IMAGE_PARATHYROID),
					stateAnt1 = new ImageState(Orientation.ANT, SLICE_ANT1, ImageState.LAT_RL, ModelParathyroid.IMAGE_THYROIDPARA);
		final int NB_ROI_PER_IMAGE = 1;
		// Post then Ant
		for (int i = 0; i < 1; i++) {
			ImageState state;
			if (i == 0) state = stateAnt1;
			else state = stateAnt;
			// Thyroid Only
			getModel().addData(ModelParathyroid.REGION_PARATHYROID, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i]);
			// Thyroid and Parathyroid
			getModel().addData(ModelParathyroid.REGION_THYRO_PARA, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 1]);
		}
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1, dri_2;
		this.captures = new ArrayList<>(2);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
		ImageState stateAnt1 = new ImageState(Orientation.ANT, 2, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction(ModelParathyroid.REGION_THYRO_PARA, stateAnt1);
		dri_2 = new DrawRoiInstruction(ModelParathyroid.REGION_PARATHYROID, stateAnt, dri_1);
		// Image Thyro
		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));

		// Image ThyroPara
		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
		this.workflows[1].addInstruction(dri_2);
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));
		this.workflows[1].addInstruction(new EndInstruction());
	}

	public ModelParathyroid getModel() {
		return (ModelParathyroid) super.getModel();
	}
	

	public void captureZoom() {
		//Capture des ROIs
		ImagePlus captureR1 = this.model.getImageSelection()[0].getImagePlus();
		captureR1.setRoi(getModel().getRoi(0).getBounds());
		captureR1 = captureR1.crop();
		this.captures.add(captureR1);
		

		ImagePlus captureR2 = this.model.getImageSelection()[1].getImagePlus();
		captureR2.setRoi(getModel().getRoi(1).getBounds());
		captureR2 = captureR2.crop();
		captureR2 = setCompleteDimensions(captureR1, captureR2);
		this.captures.add(captureR2);

		ImagePlus captureSubtr = this.getModel().calculateResult();
		captureSubtr.setRoi(getModel().getRoi(0).getBounds());
		captureSubtr = captureSubtr.crop();
		captureSubtr = setCompleteDimensions(captureR1, captureSubtr);
		this.captures.add(captureSubtr);
	}

	public ImagePlus setCompleteDimensions(ImagePlus model, ImagePlus toModify) {
		toModify.setDimensions(model.getDimensions()[2], 
							   model.getDimensions()[3], 
							   model.getDimensions()[4]);
		ImageProcessor process = toModify.getProcessor();
		process = process.resize(model.getDimensions()[0], model.getDimensions()[1], false);
		toModify.setProcessor(process);
		return toModify;
	}

	public ImagePlus setOverlayZooms(ImagePlus toOverlay){
		ImagePlus temp = Library_Dicom.resize(toOverlay, 512, 512);
					toOverlay.close();
					toOverlay.setImage(temp);
					Library_Gui.setCustomLut(toOverlay);
					
					Library_Gui.initOverlay(toOverlay);
					Library_Gui.setOverlayTitle("Ant", toOverlay, Color.YELLOW, 0);
					Library_Gui.setOverlayGD(toOverlay, Color.YELLOW);
		return toOverlay;
	}

	@Override
	protected void end() {
		super.end();

		this.computeModel();

		this.captureZoom();

		// Save captures ROI
		ImagePlus montageCaptures = null;
		ImageStack stackCapture = null;
		ImagePlus[] impCapture = new ImagePlus[2];
		
		impCapture[0] = this.captures.get(0);
		impCapture[1] = this.captures.get(1);
		stackCapture = Library_Capture_CSV.captureToStack(impCapture);
		montageCaptures = this.montageForTwo(stackCapture);
		
		//set the overlays
		ImagePlus temp = this.captures.get(2);
		this.captures.get(2).setImage(setOverlayZooms(temp));
		temp = this.captures.get(3);
		this.captures.get(3).setImage(setOverlayZooms(temp));
		temp = this.captures.get(4);
		this.captures.get(4).setImage(setOverlayZooms(temp));

		// Save captures bounds and result
		ImagePlus[] impCapture1 = new ImagePlus[3];
		impCapture1[0] = this.captures.get(2);
		impCapture1[1] = this.captures.get(3);
		impCapture1[2] = this.captures.get(4);
		stackCapture = Library_Capture_CSV.captureToStack(impCapture1);
		ImagePlus montageResults = this.montageForThree(stackCapture);
		
		// Display result
		ImagePlus result = this.getModel().calculateResult();
		new FenResultatsParathyroid(this, montageCaptures, montageResults,this.captures, result);

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
				Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
											state.getSlice());
				Library_Gui.setOverlayTitle(display.getTitleAnt(), this.vue.getImagePlus(), Color.YELLOW,
											state.getSlice());
			} else {
				Library_Gui.setOverlaySides(this.vue.getImagePlus(), Color.YELLOW, display.textL, display.textR,
											state.getSlice());
				Library_Gui.setOverlayTitle("Inverted " + display.getTitlePost(), this.vue.getImagePlus(),
											Color.YELLOW,
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

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			this.display = DisplayState.stateFromLabel((String) e.getItem());
			this.getVue().getImagePlus().getOverlay().clear();
			this.setOverlay(this.currentState);
			this.getVue().getImagePlus().updateAndDraw();
		}
	}


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
		 * state
		 * is returned.
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

		public String getTitlePost() {
			return this.title.split("-")[1];
		}
	}

	

}
