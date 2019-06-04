package org.petctviewer.scintigraphy.mibg.tabResults;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.mibg.ModelMIBG;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class TabMainMIBG extends TabResult {

	private final ControllerScin controller;
	final ImagePlus montage;

	public TabMainMIBG(FenResults parent, String title, List<ImagePlus> captures) {
		super(parent, title, true);
		this.controller = this.parent.getController();

		ImageStack stackCapture = Library_Capture_CSV.captureToStack(captures.toArray(new ImagePlus[0]));
		this.montage = this.montage(stackCapture);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {

		JPanel grid = new JPanel(new GridLayout(0, 1));

		String[] results = ((ModelMIBG) this.controller.getModel()).getResults();

		for (String s : results)
			grid.add(new JLabel(s));

		return grid;
	}

	@Override
	public JPanel getResultContent() {
		return new DynamicImage(montage.getImage());
	}

	private ImagePlus montage(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Results MIBG", captures);
		imp = mm.makeMontage2(imp, 1, 2, 0.50, 1, 2, 1, 10, false);
		return imp;
	}

}
