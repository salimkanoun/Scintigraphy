package org.petctviewer.scintigraphy.scin.gui;

import java.util.Arrays;

public enum DisplayState {
    RIGHT_LEFT("Label ANT as RIGHT", "P", "A", "Right-Left"),
    LEFT_RIGHT("Label ANT as LEFT", "A", "P", "Left-Right"),
    ANT_POST("Label ANT as ANT", "R", "L", "Ant-Post");

    public String label, textL, textR;
    private final String title;

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
