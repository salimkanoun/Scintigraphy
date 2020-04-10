package org.petctviewer.scintigraphy.hepatic.radioEmbolization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.model.Result;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.shunpo.ModelShunpo;

import ij.ImagePlus;

public class MainResult extends TabResult {

	private final ImagePlus montage;
	
	public MainResult(FenResults parent, ImagePlus montage) {
		super(parent, "Results", true);
		this.montage = montage;
		this.reloadDisplay();
	}
	
	private ModelRadioEmbol getModel() {
		return (ModelRadioEmbol) this.parent.getModel();
	}
	
	private void displayResult (ResultValue result, Container container) {
		JLabel label = new JLabel(result.toString());
		
		//Color
		if (result.getResultType()== ModelRadioEmbol.RES_PULMONARY_SHUNT) {
			if (result.getValue() < 2.) label.setForeground(Color.GREEN);
			else if (result.getValue() < 5.) label.setForeground(Color.ORANGE);
			else label.setForeground(Color.RED);
		}
		container.add(label);
	}
	
	@Override
	public Component getSidePanelContent() {
		JPanel res = new JPanel(new GridLayout(0, 1));

		// Lung ratio
		ResultRequest request = new ResultRequest(ModelLiver.RES_RATIO_RIGHT_LUNG);
		this.displayResult(getModel().getResult(request), res);
		request.changeResultOn(ModelLiver.RES_RATIO_LEFT_LUNG);
		this.displayResult(getModel().getResult(request), res);
		// Shunt systemic
		request.changeResultOn(ModelLiver.RES_RATIO_LIVER);
		this.displayResult(getModel().getResult(request), res);
		// Pulmonary shunt
		request.changeResultOn(ModelLiver.RES_PULMONARY_SHUNT);
		this.displayResult(getModel().getResult(request), res);

		return res;
	}
	

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(montage.getImage());
	}
	
}
