package org.petctviewer.scintigraphy.shunpo;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.*;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabShunpo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.petctviewer.scintigraphy.scin.library.Library_Roi.roiBetween;

public class ControllerWorkflowShunpo extends ControllerWorkflow implements ItemListener {
	private static final int SLICE_ANT = 1, SLICE_POST = 2;
	private final boolean WITH_KIDNEYS;
	private List<ImagePlus> captures;
	private DisplayState display;

	public ControllerWorkflowShunpo(FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelShunpo(selectedImages, vue.getStudyName()));

		// Initialize variables
		this.WITH_KIDNEYS = Prefs.get(PrefTabShunpo.PREF_WITH_KIDNEYS, true);
		this.display = DisplayState.ANT_POST;

		this.generateInstructions();
		this.start();
	}

	// TODO: remove this method and do this in the instructions
	private void computeModel() {
		ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL,ModelShunpo.IMAGE_KIDNEY_LUNG),
					statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_LR, ModelShunpo.IMAGE_KIDNEY_LUNG);
		final int NB_ROI_PER_IMAGE = WITH_KIDNEYS ? 5 : 2;
		// Post then Ant
		for (int i = 0; i < 2; i++) {
			ImageState state;
			if (i == 0) state = statePost;
			else state = stateAnt;
			// - Right lung
			getModel().addData(ModelShunpo.REGION_RIGHT_LUNG, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i]);
			// - Left lung
			getModel().addData(ModelShunpo.REGION_LEFT_LUNG, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 1]);

			if (WITH_KIDNEYS) {
				// - Right Kidney
				getModel().addData(ModelShunpo.REGION_RIGHT_KIDNEY, state,
								   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 2]);
				// - Left Kidney
				getModel().addData(ModelShunpo.REGION_LEFT_KIDNEY, state,
								   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 3]);
				// - Background
				getModel().addData(ModelShunpo.REGION_BACKGROUND, state,
								   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 4]);
			}
		}

		// - Brain Post
		statePost.setIdImage(ModelShunpo.IMAGE_BRAIN);
		getModel().addData(ModelShunpo.REGION_BRAIN, statePost,
						   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * 2]);
		// - Brain Ant
		stateAnt.setIdImage(ModelShunpo.IMAGE_BRAIN);
		getModel().addData(ModelShunpo.REGION_BRAIN, stateAnt,
						   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * 2 + 1]);
	}

	private void generateInstructionsWithKidneys() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_6, dri_7, dri_8, dri_9, dri_11;
		this.captures = new ArrayList<>(6);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ImageState.ID_NONE);
		ImageState statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_LR, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction(ModelShunpo.REGION_RIGHT_LUNG, stateAnt);
		dri_2 = new DrawRoiInstruction(ModelShunpo.REGION_LEFT_LUNG, stateAnt);
		dri_3 = new DrawRoiInstruction(ModelShunpo.REGION_RIGHT_KIDNEY, stateAnt);
		dri_4 = new DrawRoiInstruction(ModelShunpo.REGION_LEFT_KIDNEY, stateAnt);
		dri_6 = new DrawRoiInstruction(ModelShunpo.REGION_RIGHT_LUNG, statePost);
		dri_7 = new DrawRoiInstruction(ModelShunpo.REGION_LEFT_LUNG, statePost);
		dri_8 = new DrawRoiInstruction(ModelShunpo.REGION_RIGHT_KIDNEY, statePost);
		dri_9 = new DrawRoiInstruction(ModelShunpo.REGION_LEFT_KIDNEY, statePost);
		dri_11 = new DrawRoiInstruction(ModelShunpo.REGION_BRAIN, stateAnt);

		// Image Lung-Kidney
		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(new DrawRoiInMiddle(ModelShunpo.REGION_BACKGROUND, stateAnt, dri_3, dri_4));
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));
		this.workflows[0].addInstruction(dri_6);
		this.workflows[0].addInstruction(dri_7);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 2));
		this.workflows[0].addInstruction(dri_8);
		this.workflows[0].addInstruction(dri_9);
		this.workflows[0].addInstruction(new DrawRoiInMiddle(ModelShunpo.REGION_BACKGROUND, statePost, dri_8, dri_9));
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 3));


		// Image Brain
		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
		this.workflows[1].addInstruction(dri_11);
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 4));
		this.workflows[1].addInstruction(new DrawRoiInstruction(ModelShunpo.REGION_BRAIN, statePost, dri_11));
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 5));
		this.workflows[1].addInstruction(new EndInstruction());
	}

	private void generateInstructionsWithoutKidneys() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_11;
		this.captures = new ArrayList<>(6);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, ImageState.ID_NONE);
		ImageState statePost = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_LR, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction(ModelShunpo.REGION_RIGHT_LUNG, stateAnt);
		dri_2 = new DrawRoiInstruction(ModelShunpo.REGION_LEFT_LUNG, stateAnt);
		dri_3 = new DrawRoiInstruction(ModelShunpo.REGION_RIGHT_LUNG, statePost);
		dri_4 = new DrawRoiInstruction(ModelShunpo.REGION_LEFT_LUNG, statePost);
		dri_11 = new DrawRoiInstruction(ModelShunpo.REGION_BRAIN, stateAnt);

		// Image Lung-Kidney
		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 2));
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 3));


		// Image Brain
		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
		this.workflows[1].addInstruction(dri_11);
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 4));
		this.workflows[1].addInstruction(new DrawRoiInstruction(ModelShunpo.REGION_BRAIN, statePost, dri_11));
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 5));
		this.workflows[1].addInstruction(new EndInstruction());
	}

	public ModelShunpo getModel() {
		return (ModelShunpo) super.getModel();
	}

	@Override
	protected void end() {
		super.end();

		this.computeModel();
		this.model.calculateResults();

		// Save captures
		ImagePlus[] impCapture = new ImagePlus[4];
		impCapture[0] = this.captures.get(1);
		impCapture[1] = this.captures.get(3);
		impCapture[2] = this.captures.get(4);
		impCapture[3] = this.captures.get(5);
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(impCapture);
		ImagePlus montage1 = this.montage(stackCapture);

		impCapture[0] = this.captures.get(0);
		impCapture[1] = this.captures.get(2);
		stackCapture = Library_Capture_CSV.captureToStack(impCapture);
		ImagePlus montage2 = this.montage(stackCapture);

		// Display result
		FenResults fenResults = new FenResults(this);
		if (WITH_KIDNEYS) fenResults.setMainTab(new MainResult(fenResults, montage1));
		else {
			fenResults.setMainTab(new TabResult(fenResults, "Without Kidneys", true) {
				@Override
				public Component getSidePanelContent() {
					JPanel panel = new JPanel(new GridLayout(0, 1));
					ResultRequest request = new ResultRequest(ModelShunpo.RES_PULMONARY_SHUNT_2);
					ResultValue result = getModel().getResult(request);

					JLabel label = new JLabel(result.toString());
					// Color result
					if (result.getValue() < 6.) label.setForeground(Color.GREEN);
					else label.setForeground(Color.RED);
					panel.add(label);

					return panel;
				}

				@Override
				public Container getResultContent() {
					return new DynamicImage(montage2.getBufferedImage());
				}
			});
			fenResults.getMainTab().reloadDisplay();
		}
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
				Library_Gui.setOverlayTitle(display.getTitlePost(), this.vue.getImagePlus(),
											Color.YELLOW, state.getSlice());
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
		if (WITH_KIDNEYS) {
			this.generateInstructionsWithKidneys();
		} else {
			this.generateInstructionsWithoutKidneys();
		}
	}

	private class DrawRoiInMiddle extends DrawRoiInstruction {
		private static final long serialVersionUID = 1L;

		private final transient DrawRoiInstruction dri_1;
		private final transient DrawRoiInstruction dri_2;

		public DrawRoiInMiddle(String organToDelimit, ImageState state, DrawRoiInstruction roi1,
							   DrawRoiInstruction roi2) {
			super(organToDelimit, state);
			this.dri_1 = roi1;
			this.dri_2 = roi2;
		}

		@Override
		public void afterNext(ControllerWorkflow controller) {
			super.afterNext(controller);
			Roi r1 = getRoiManager().getRoi(this.dri_1.getRoiIndex());
			Roi r2 = getRoiManager().getRoi(this.dri_2.getRoiIndex());
			controller.getModel().getImageSelection()[controller.getCurrentImageState().getIdImage()].getImagePlus().setRoi(
					roiBetween(r1, r2));
		}

	}

}
