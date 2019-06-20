package org.petctviewer.scintigraphy.colonic;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import ij.ImagePlus;

public class FenResultsColonicTransit extends FenResults {

	private static final long serialVersionUID = -8587906502937827065L;

	public FenResultsColonicTransit(ControllerScin controller, List<ImagePlus> captures, int[] times) {
		super(controller);
		// TODO Auto-generated constructor stub

		this.setMainTab(new TabMain(this, ""+times[0]+"h", controller, 0, captures.get(0)));
		if (this.getController().getModel().getImageSelection().length > 2)
			this.addTab(new TabMain(this, ""+times[1]+"h", controller, 1, captures.get(1)));
		if (this.getController().getModel().getImageSelection().length > 3)
			this.addTab(new TabMain(this, ""+times[2]+"h", controller, 2, captures.get(2)));

	}

	private class TabMain extends TabResult {

		private final ControllerScin controller;
		private final int time;
		private final ImagePlus capture;

		public TabMain(FenResults parent, String title, ControllerScin controller, int time, ImagePlus capture) {
			super(parent, title, true);
			// TODO Auto-generated constructor stub

			this.controller = controller;
			
			this.time = time;

			this.capture = capture;
			
			this.reloadDisplay();
		}

		@Override
		public Component getSidePanelContent() {
			// TODO Auto-generated method stub

			JPanel grid = new JPanel(new GridLayout(0, 2));

			String[] results = ((ModelColonicTransit) this.controller.getModel()).getResults(time);

			for (String s : results)
				grid.add(new JLabel(s));

			return grid;
		}

		@Override
		public Container getResultContent() {
			// TODO Auto-generated method stub
			return new DynamicImage(this.capture.getBufferedImage());
		}

	}

}
