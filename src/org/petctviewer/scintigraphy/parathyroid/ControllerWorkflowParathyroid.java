package org.petctviewer.scintigraphy.parathyroid;

import ij.ImagePlus;
import ij.ImageStack;

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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerWorkflowParathyroid extends ControllerWorkflow implements ItemListener {

	private static final int SLICE_ANT = 1;
	private DisplayState display;
	private List<ImagePlus> captures;


    public ControllerWorkflowParathyroid(FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelParathyroid(selectedImages, vue.getStudyName()));
		this.display = DisplayState.ANT_POST;

		this.generateInstructions();
		this.start();
	}

	public ModelParathyroid getModel() {
		return (ModelParathyroid) super.getModel();
	}

	// TODO: remove this method and do this in the instructions
	private void computeModel() {
		ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ModelParathyroid.IMAGE_THYROID);
		final int NB_ROI_PER_IMAGE = 1;
		// Just Ant
		for (int i = 0; i < 2; i++) {
			ImageState state;
			state = stateAnt;
			// - Thyroid Only
			getModel().addData(ModelParathyroid.REGION_THYRO, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i]);
			// - Thyroid and parathyroid
			getModel().addData(ModelParathyroid.REGION_THYRO_PARA, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 1]);
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

    @Override
    protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1, dri_2;
		this.captures = new ArrayList<>(2);

		// Image Thyroid
		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction(ModelParathyroid.REGION_THYRO, stateAnt);
		this.workflows[0].addInstruction(dri_1);		
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));

		//Image Parathyroid
		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);

		ImageState stateAnt1 = new ImageState(Orientation.ANT, 2, true, ImageState.ID_NONE);

		dri_2 = new DrawRoiInstruction(ModelParathyroid.REGION_THYRO_PARA, stateAnt1, dri_1);
		this.workflows[1].addInstruction(dri_2);
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));

	}
	
	@Override
	protected void end() {
		super.end();

		this.computeModel();
		this.model.calculateResults();

		// Save captures
		ImagePlus[] impCapture = new ImagePlus[2];
		impCapture[0] = this.captures.get(0);
		impCapture[1] = this.captures.get(1);
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(impCapture);
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		FenResults fenResults = new FenResults(this);
		fenResults.setMainTab(new MainResult(fenResults, montage));
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