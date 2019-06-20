package org.petctviewer.scintigraphy.generic.dynamic;

import org.jfree.chart.ChartPanel;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TabAntPost extends TabResult {

	private final BufferedImage capture;

	public TabAntPost(BufferedImage capture, String antOrPost, FenResults parent) {
		super(parent, antOrPost, true);
//		this.createCaptureButton("_" + antOrPost);
		this.setAdditionalInfo("_" + antOrPost);
		this.capture = capture;
		
		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		Model_GeneralDyn modele = (Model_GeneralDyn) this.parent.getModel();
		int cols = (modele.getNbRoi() + 1) / 2;
		JPanel grid = new JPanel(new GridLayout(2, cols));

		grid.add(new DynamicImage(capture));

		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(((FenResultat_GeneralDyn) parent).getAsso(),
				modele.getSeries());
		for (ChartPanel c : cPanels) {
			c.setPreferredSize(new Dimension(capture.getWidth(), capture.getHeight()));
			grid.add(c);
		}
		return grid;
	}
}