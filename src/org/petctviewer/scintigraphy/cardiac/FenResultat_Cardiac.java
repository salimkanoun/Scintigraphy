package org.petctviewer.scintigraphy.cardiac;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.cardiac.tab.TabMainCardiac;
import org.petctviewer.scintigraphy.cardiac.tab.TabVisualGradationCardiac;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.util.HashMap;
import java.util.List;

public class FenResultat_Cardiac extends FenResults {

	private static final long serialVersionUID = -5261203439330504164L;

	public FenResultat_Cardiac(List<ImagePlus> capture, ControllerScin controller, int fullBodyImages,
							   int onlyThoraxImage) {
		super(controller);

		if (fullBodyImages != 0) {
			HashMap<String, String> resultats = ((Model_Cardiac) controller.getModel()).getResultsHashMap();
			this.addTab(new TabMainCardiac(this, "DPD Quant", resultats, capture.get(0).getBufferedImage(),
										   fullBodyImages));
		}
		if (onlyThoraxImage != 0) {
			HashMap<String, String> resultatsThorax =
					((Model_Cardiac) controller.getModel()).getResultsVisualGradationHashMap();
			this.addTab(new TabVisualGradationCardiac(this, "Visual Gradation", resultatsThorax,
													  capture.get(fullBodyImages == 0 ? 0 : 1).getBufferedImage(),
													  onlyThoraxImage));
		}

		this.setTitle("DPD Quant results");
	}

}
