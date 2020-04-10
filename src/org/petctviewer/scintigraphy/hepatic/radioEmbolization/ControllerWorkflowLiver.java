package org.petctviewer.scintigraphy.hepatic.radioEmbolization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;

import ij.ImagePlus;
import ij.ImageStack;

public class ControllerWorkflowLiver extends ControllerWorkflow implements ItemListener{

	private static final int SLICE_ANT = 1, SLICE_POST = 2;
	private List<ImagePlus> captures;

	public ControllerWorkflowLiver (FenApplicationWorkflow vue, ImageSelection[] selectedImages) {
		super(vue, new ModelLiver(selectedImages, vue.getStudyName()));
		this.generateInstructions();
		this.start();	
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


}