package org.petctviewer.scintigraphy.thyroid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;

import ij.ImagePlus;

public class MainResult extends TabResult{

    private final ImagePlus montage;

    public MainResult(FenResults parent, ImagePlus montage){
        super(parent, "Results", true);
        this.montage = montage;
        this.reloadDisplay();
    }

    	
	/** 
	 * @return ModelThyroid
	 */
	private ModelThyroid getModel() {
		return (ModelThyroid) this.parent.getModel();
    }
    
    /**
     * @param result
     * @param container
     */
    private void displayResult(ResultValue result, Container container){
        JLabel label = new JLabel(result.toString());

        //Color
        if(result.getResultType() == ModelThyroid.RES_THYROID_SHUNT){
            if(result.getValue() < 2.) label.setForeground(Color.GREEN);
            else if (result.getValue() < 5.) label.setForeground(Color.ORANGE);
            else label.setForeground(Color.RED);
        }
        container.add(label);
    }

    /**
     * @return Component
     */
    @Override
    public Component getSidePanelContent(){
        JPanel res = new JPanel(new GridLayout(0,1));

        //Thyroid shunt
        ResultRequest request = new ResultRequest(ModelThyroid.RES_THYROID_SHUNT);
        this.displayResult(this.getModel().getResult(request), res);

        return res;
    }

    /**
     * @return JPanel
     */
    @Override
    public JPanel getResultContent(){
        return new DynamicImage(montage.getImage());
    }
}