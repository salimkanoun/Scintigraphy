package org.petctviewer.scintigraphy.colonic;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class FenResultsColonicTransit extends FenResults {

	private static final long serialVersionUID = -8587906502937827065L;

	public FenResultsColonicTransit(ControleurScin controller) {
		super(controller);
		// TODO Auto-generated constructor stub

		this.setMainTab(new TabMain(this, "24h", controller, 1));
		if (this.getController().getModel().getImageSelection().length > 1)
			this.setMainTab(new TabMain(this, "48h", controller, 2));
		if (this.getController().getModel().getImageSelection().length > 2)
			this.setMainTab(new TabMain(this, "72h", controller, 3));

	}

	private class TabMain extends TabResult {

		private ControleurScin controller;
		private int time;

		public TabMain(FenResults parent, String title, ControleurScin controller, int time) {
			super(parent, title, true);
			// TODO Auto-generated constructor stub

			this.controller = controller;
			
			this.time = time;

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
			return new DynamicImage(this.controller.getVue().getImagePlus().getBufferedImage());
		}

	}

}
