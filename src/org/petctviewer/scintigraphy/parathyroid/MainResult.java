package org.petctviewer.scintigraphy.parathyroid;

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
		super(parent, "Result Parathyroid", true);
		this.montage = montage;
		this.reloadDisplay();
	}

	private ModelParathyroid getModel() {
		return (ModelParathyroid) this.parent.getModel();
	}

	private void displayResult(ResultValue result, Container container) {
		JLabel label = new JLabel(result.toString());

		// Color for pulmonary shunt
		if (result.getResultType() == ModelParathyroid.RES_RATIO_THYRO_PARA) {
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
		ResultRequest request = new ResultRequest(ModelParathyroid.RES_RATIO_THYRO);
		this.displayResult(getModel().getResult(request), res);
		request.changeResultOn(ModelParathyroid.RES_RATIO_THYRO_PARA);
		this.displayResult(getModel().getResult(request), res);

		return res;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(montage.getImage());
	}

}
