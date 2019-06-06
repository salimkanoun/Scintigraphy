package org.petctviewer.scintigraphy.shunpo;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;

import javax.swing.*;
import java.awt.*;

public class MainResult extends TabResult {

	private final ImagePlus montage;

	public MainResult(FenResults parent, ImagePlus montage) {
		super(parent, "Result", true);
		this.montage = montage;
		this.reloadDisplay();
	}

	private ModelShunpo_refactored getModel() {
		return (ModelShunpo_refactored) this.parent.getModel();
	}

	private void displayResult(ResultValue result, Container container) {
		JLabel label = new JLabel(result.toString());
		container.add(label);
	}

	@Override
	public Component getSidePanelContent() {
		JPanel res = new JPanel(new GridLayout(0, 1));

		// Lung ratio
		ResultRequest request = new ResultRequest(ModelShunpo_refactored.RES_RATIO_RIGHT_LUNG);
		this.displayResult(getModel().getResult(request), res);
		request.changeResultOn(ModelShunpo_refactored.RES_RATIO_LEFT_LUNG);
		this.displayResult(getModel().getResult(request), res);
		// Shunt systemic
		request.changeResultOn(ModelShunpo_refactored.RES_SHUNT_SYST);
		this.displayResult(getModel().getResult(request), res);
		// Pulmonary shunt
		request.changeResultOn(ModelShunpo_refactored.RES_PULMONARY_SHUNT);
		this.displayResult(getModel().getResult(request), res);

		return res;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(montage.getImage());
	}

}
