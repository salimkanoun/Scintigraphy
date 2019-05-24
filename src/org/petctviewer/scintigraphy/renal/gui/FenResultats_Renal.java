package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import ij.Prefs;

public class FenResultats_Renal extends FenResults {
	private static final long serialVersionUID = 1L;

	private final int width = 1000, height = 800;

	public FenResultats_Renal(RenalScintigraphy vue, BufferedImage capture, ControllerScin controller) {
		super(controller);
		this.addTab(new TabPrincipal(vue, capture, this));
		this.addTab(new TabROE(vue, this));
		this.addTab(new TabTimedImage(vue, 4, 5, this));
		if (Prefs.get("renal.pelvis.preferred", true))
			this.addTab(new TabCort(vue, this));
		if (Prefs.get("renal.ureter.preferred", true))
			this.addTab(new TabUreter(vue, this));
		this.addTab(new TabZoomed(vue, this));
		this.addTab(new TabOther(vue, this));
		this.addTab(new TabPostMict(vue, this));
		if (((Model_Renal) controller.getModel()).getPatlakChart() != null) {
			this.addTab(new TabPatlak(vue, this));
		}
		if (Prefs.get("petctviewer.scin.experimental", false))
			this.addTab(new TabDeconvolve(this, "Deconvolve"));

		this.setTitle("Results Renal Exam");
		this.setPreferredSize(new Dimension(width, height));
		this.setLocationRelativeTo(vue.getFenApplication());
	}

	// renomme la serie
	static void renameSeries(ChartPanel chartPanel, String oldKey, String newKey) {
		XYSeriesCollection dataset = ((XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset());
		try {
			dataset.getSeries(oldKey).setKey(newKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
