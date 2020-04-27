package org.petctviewer.scintigraphy.parathyroid;

import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

public class ControllerWorkflowParathyroid extends ControllerWorkflow implements ItemListener {

	private static final int SLICE_ANT = 1;
	private DisplayState display;

    public ControllerWorkflowParathyroid(FenApplicationWorkflow vue, ModelScin model) {
		super(vue, model);
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

		// - Brain Post
		statePost.setIdImage(ModelShunpo.IMAGE_BRAIN);
		getModel().addData(ModelShunpo.REGION_BRAIN, statePost,
						   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * 2]);
		// - Brain Ant
		stateAnt.setIdImage(ModelShunpo.IMAGE_BRAIN);
		getModel().addData(ModelShunpo.REGION_BRAIN, stateAnt,
						   getRoiManager().getRoisAsArray()[NB_ROI_PER_IMAGE * 2 + 1]);
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
        // TODO Auto-generated method stub

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