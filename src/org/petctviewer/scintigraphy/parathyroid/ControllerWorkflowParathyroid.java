package org.petctviewer.scintigraphy.parathyroid;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

public class ControllerWorkflowParathyroid extends ControllerWorkflow implements ItemListener {

    public ControllerWorkflowParathyroid(FenApplicationWorkflow vue, ModelScin model) {
        super(vue, model);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        // TODO Auto-generated method stub

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