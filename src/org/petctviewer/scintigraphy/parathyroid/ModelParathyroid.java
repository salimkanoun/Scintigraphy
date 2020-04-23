package org.petctviewer.scintigraphy.parathyroid;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.model.Data;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;
import org.petctviewer.scintigraphy.scin.model.Result;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

public class ModelParathyroid extends ModelWorkflow {

    public static final String REGION_THYRO_PARA = "Thyroid and Parathyroid", REGION_THYRO = "Thyroid only";

    public static final Result RES_RATIO_THYRO_PARA = new Result("Thyroid+Para Ratio"), RES_RATIO_THYRO= new Result("Thyroid ratio"); 

    public static final int IMAGE_THYROIDPARA = 0, IMAGE_THYROID = 1;

    private List<Data> datas;
    private Map<Integer, Double> results;
    
    /**
	 * @param selectedImages Images needed for this study (generally those images are used in the workflows)
	 * @param studyName      Name of the study (used for display)
	 */

    public ModelParathyroid(ImageSelection[] selectedImages, String studyName) {
        super(selectedImages, studyName);
        this.datas = new LinkedList<>();
        this.results = new HashMap<>();
    }

    @Override
    public ResultValue getResult(ResultRequest request) {
        Double value = this.results.get(request.getResultOn().hashCode());
        if (value == null) return null;
		// Convert result to requested unit
		Unit conversion = (request.getUnit() == null ? Unit.PERCENTAGE : request.getUnit());
		value = Unit.PERCENTAGE.convertTo(value, conversion);
		return new ResultValue(request, value, conversion);
    }

    
    private void calculateResult() {
        
    }

    @Override
    public void calculateResults() {
        this.calculateResult();
    }   
    

}