package org.petctviewer.scintigraphy.parathyroid;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MainResult extends TabResult {

	private final ImagePlus montage;
	private final ImagePlus result;
	List<ImagePlus> captures;

	public MainResult(FenResults parent, ImagePlus montage, String tabName) {
		super(parent, tabName, true);
		this.montage = montage;
		this.result = null;
		this.captures = null;
		this.reloadDisplay();
	}

	public MainResult(FenResults parent, ImagePlus montage, String tabName, ImagePlus result) {
		super(parent, tabName, true);
		this.montage = montage;
		this.result = result;
		this.captures = null;
		this.reloadDisplay();
	}

	/*private ModelParathyroid getModel() {
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
	}*/

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		if (this.result!=null && this.captures!=null){

			JPanel gridCaptures = new JPanel(new GridLayout(1,3));
			/*DynamicImage imgBefore = new DynamicImage(this.captures.get(2).getImage());
			gridCaptures.add(imgBefore);
			gridCaptures.add(new DynamicImage(this.captures.get(3).getImage()));
			gridCaptures.add(new DynamicImage(this.captures.get(4).getImage()));*/

			return gridCaptures;
		}else {
			return new DynamicImage(montage.getImage());
		}
	}

}
