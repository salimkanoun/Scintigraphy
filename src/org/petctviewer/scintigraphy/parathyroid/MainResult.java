package org.petctviewer.scintigraphy.parathyroid;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.model.ResultValue;

import javax.swing.*;
import java.awt.*;

public class MainResult extends TabResult {

	private final ImagePlus montage;
	private final ImagePlus result;

	public MainResult(FenResults parent, ImagePlus montage, String tabName) {
		super(parent, tabName, true);
		this.montage = montage;
		this.result = null;
		this.reloadDisplay();
	}

	public MainResult(FenResults parent, ImagePlus montage, String tabName, ImagePlus result) {
		super(parent, tabName, true);
		this.montage = montage;
		this.result = result;
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
		return null;
	}

	@Override
	public JPanel getResultContent() {
		if (this.result!=null){
			JPanel grid = new JPanel(new GridLayout(2, 1));
			grid.add(new DynamicImage(this.montage.getImage()));
			grid.add(new DynamicImage(this.result.getImage()));
			return grid;
		}
		
		return new DynamicImage(montage.getImage());
	}

}
