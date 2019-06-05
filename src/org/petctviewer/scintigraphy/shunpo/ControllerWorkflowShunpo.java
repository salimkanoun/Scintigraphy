package org.petctviewer.scintigraphy.shunpo;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import java.util.ArrayList;
import java.util.List;

public class ControllerWorkflowShunpo extends ControllerWorkflow {

	private static final int STEP_KIDNEY_LUNG = 0, STEP_BRAIN = 1;
	private static final int SLICE_ANT = 1, SLICE_POST = 2;
	private final FenResults fenResults;
	private final boolean FIRST_ORIENTATION_POST;
	private final int[] NBORGANE = {5, 1};
	private List<ImagePlus> captures;

	public ControllerWorkflowShunpo(Scintigraphy main, FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(main, vue, new ModelShunpo_refactored(selectedImages, main.getStudyName()));

		this.generateInstructions();
		this.start();

		this.FIRST_ORIENTATION_POST = true;
		this.fenResults = new FenResults(this);
	}

	// TODO: remove this method and do this in the instructions
	private void computeModel() {
		ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, 0), statePost = new ImageState(
				Orientation.POST, 2, ImageState.LAT_RL, 0);
		final int NB_ROI_PER_IMAGE = 7;
		// Post then Ant
		for (int i = 0; i < 2; i++) {
			ImageState state;
			if (i == 0) state = statePost;
			else state = stateAnt;
			// - Right lung
			getModel().addData(ModelShunpo_refactored.REGION_RIGHT_LUNG, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i]);
			// - Left lung
			getModel().addData(ModelShunpo_refactored.REGION_LEFT_LUNG, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 1]);
			// - Right Kidney
			getModel().addData(ModelShunpo_refactored.REGION_RIGHT_KIDNEY, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 2]);
			// - Left Kidney
			getModel().addData(ModelShunpo_refactored.REGION_LEFT_KIDNEY, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 3]);
			// - Background
			getModel().addData(ModelShunpo_refactored.REGION_BACKGROUND, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 4]);
			// - Right Lung
			getModel().addData(ModelShunpo_refactored.REGION_RIGHT_LUNG, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 5]);
			// - Left Lung
			getModel().addData(ModelShunpo_refactored.REGION_LEFT_LUNG, state,
							   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * i + 6]);
		}

		// - Brain Post
		statePost.setIdImage(1);
		getModel().addData(ModelShunpo_refactored.REGION_BRAIN, statePost,
						   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * 2]);
		// - Brain Ant
		stateAnt.setIdImage(1);
		getModel().addData(ModelShunpo_refactored.REGION_BRAIN, stateAnt,
						   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * 2 + 1]);
	}

	public ModelShunpo_refactored getModel() {
		return (ModelShunpo_refactored) super.getModel();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_8, dri_9, dri_11;
		this.captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
		ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_RIGHT_LUNG, statePost);
		dri_2 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_LEFT_LUNG, statePost);
		dri_3 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_RIGHT_KIDNEY, statePost);
		dri_4 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_LEFT_KIDNEY, statePost);
		dri_8 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_RIGHT_KIDNEY, stateAnt, dri_3);
		dri_9 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_LEFT_KIDNEY, stateAnt, dri_4);
		dri_11 = new DrawRoiInstruction(ModelShunpo_refactored.REGION_BRAIN, statePost);

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(
				new DrawRoiInMiddle(ModelShunpo_refactored.REGION_BACKGROUND, statePost, dri_3, dri_4));
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 0));
		this.workflows[0].addInstruction(
				new DrawRoiInstruction(ModelShunpo_refactored.REGION_RIGHT_LUNG, stateAnt, dri_1));
		this.workflows[0].addInstruction(
				new DrawRoiInstruction(ModelShunpo_refactored.REGION_LEFT_LUNG, stateAnt, dri_2));
		this.workflows[0].addInstruction(dri_8);
		this.workflows[0].addInstruction(dri_9);
		this.workflows[0].addInstruction(
				new DrawRoiInMiddle(ModelShunpo_refactored.REGION_BACKGROUND, stateAnt, dri_8, dri_9));
		this.workflows[0].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 1));


		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[1]);
		this.workflows[1].addInstruction(dri_11);
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 2));
		this.workflows[1].addInstruction(new DrawRoiInstruction(ModelShunpo_refactored.REGION_BRAIN, stateAnt,
																dri_11));
		this.workflows[1].addInstruction(new ScreenShotInstruction(captures, this.getVue(), 3));
		this.workflows[1].addInstruction(new EndInstruction());
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
//		int firstSlice = (this.FIRST_ORIENTATION_POST ? SLICE_POST : SLICE_ANT);
//		int secondSlice = firstSlice % 2 + 1;
//		ImagePlus img;
//		this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus().setSlice(firstSlice);
//		this.model.getImageSelection()[STEP_BRAIN].getImagePlus().setSlice(firstSlice);
//		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {
//			Roi r = this.model.getRoiManager().getRoisAsArray()[i];
//			int organ;
//
//			if (i < this.NBORGANE[STEP_KIDNEY_LUNG]) {
//				img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
//				img.setSlice(firstSlice);
//				if (this.FIRST_ORIENTATION_POST)
//					organ = i * 2 + 1;
//				else
//					organ = i * 2;
//			} else if (i < 2 * this.NBORGANE[STEP_KIDNEY_LUNG]) {
//				img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
//				img.setSlice(secondSlice);
//				if (this.FIRST_ORIENTATION_POST)
//					organ = (i - this.NBORGANE[STEP_KIDNEY_LUNG]) * 2;
//				else
//					organ = (i - this.NBORGANE[STEP_KIDNEY_LUNG]) * 2 + 1;
//			} else if (i - 2 * this.NBORGANE[STEP_KIDNEY_LUNG] < this.NBORGANE[STEP_BRAIN]) {
//				img = this.model.getImageSelection()[STEP_BRAIN].getImagePlus();
//				img.setSlice(firstSlice);
//				if (this.FIRST_ORIENTATION_POST)
//					organ = i + 1;
//				else
//					organ = i;
//			} else {
//				img = this.model.getImageSelection()[STEP_BRAIN].getImagePlus();
//				img.setSlice(secondSlice);
//				if (this.FIRST_ORIENTATION_POST)
//					organ = i - 1;
//				else
//					organ = i;
//			}
//
//			img.setRoi(r);
//			((ModelShunpo) this.model).calculerCoups(organ, img);
//		}
		this.computeModel();
		this.model.calculateResults();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures.toArray(new ImagePlus[0]));
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		this.fenResults.setMainTab(new MainResult(this.fenResults, montage));
		this.fenResults.pack();
		this.fenResults.setVisible(true);

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
