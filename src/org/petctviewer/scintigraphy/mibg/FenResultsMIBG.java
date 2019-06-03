package org.petctviewer.scintigraphy.mibg;

import java.util.List;

import org.petctviewer.scintigraphy.mibg.tabResults.TabContrastMIBG;
import org.petctviewer.scintigraphy.mibg.tabResults.TabMainMIBG;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.ImagePlus;

public class FenResultsMIBG extends FenResults {

	private static final long serialVersionUID = 1L;

	public FenResultsMIBG(ControllerScin controller, List<ImagePlus> captures) {
		super(controller);
		// TODO Auto-generated constructor stub

		System.out.println("this.getController().getModel().getImageSelection().length : "
				+ this.getController().getModel().getImageSelection().length);
		this.setMainTab(new TabMainMIBG(this, "Main", captures));
		this.addTab(new TabContrastMIBG("Contrast", "timed", this, captures));

	}

}
