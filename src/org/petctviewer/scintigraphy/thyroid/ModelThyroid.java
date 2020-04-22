package org.petctviewer.scintigraphy.thyroid;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabShunpo;

import java.util.*;

public class ModelThyroid extends ModelWorkflow{
    
    public static final String REGION_LEFT_LOBE = "Left lobe", REGION_RIGHT_LOBE = "Right Lobe",
    REGION_BACKGROUND = "Background";

    public static final Result RES_RATIO_RIGHT_LOBE = new Result("Right Lobe Ratio"), RES_RATIO_LEFT_LOBE =
    new Result("Left Lobe Ratio"), RES_THYROID_SHUNT = new Result("Thyroid shunt");

    public static final int IMAGE_THYROID = 1;

    private List<Data> datas;
    private Map<Integer,Double> results;

    /**
     * @param selectedImages Images needed for this study(generally those images are used in the workflow)
     * @param studyName Name of the study (used for display)
     */
    public ModelThyroid(ImageSelection[] selectedImages, String studyName){
        super(selectedImages, studyName);
        this.datas = new LinkedList<>();
        this.results = new HashMap<>();
    }
}