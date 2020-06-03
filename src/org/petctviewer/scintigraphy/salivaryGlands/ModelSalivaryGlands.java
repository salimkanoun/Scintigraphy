package org.petctviewer.scintigraphy.salivaryGlands;

import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.gui.Roi;

public class ModelSalivaryGlands extends ModelScinDyn {

    private final HashMap<String, Roi> organRois;
    private HashMap<Comparable, Double> adjustedValues;
    private ImageSelection impAnt;
	private final ImageSelection impPost;
	private int[] frameDurations;
	private final HashMap<String, Integer> pixelCounts;

    public ModelSalivaryGlands(ImageSelection[] selectedImages, String studyName, int[] frameDuration) {
        super(selectedImages, studyName, frameDuration);
        this.organRois = new HashMap<>();
		this.impPost = selectedImages[1];
		if(selectedImages.length > 2)
			this.impAnt = selectedImages[2];

		this.frameDurations = frameDuration;
		
		this.pixelCounts = new HashMap<>();    
    }

    @SuppressWarnings("rawtypes")
	public void setAdjustedValues(HashMap<Comparable, Double> hashMap) {
		this.adjustedValues = hashMap;
    }
    
    

    @Override
    public void calculateResults() {
        // TODO Auto-generated method stub

    }

    
}