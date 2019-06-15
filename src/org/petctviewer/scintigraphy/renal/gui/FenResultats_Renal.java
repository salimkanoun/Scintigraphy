package org.petctviewer.scintigraphy.renal.gui;

import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabContrastModifier;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabMain;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabRenal;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FenResultats_Renal extends FenResults {
	private static final long serialVersionUID = 1L;

	public FenResultats_Renal(RenalScintigraphy vue, BufferedImage capture, ControllerScin controller) {
		super(controller);
		this.addTab(new TabPrincipal(capture, this));
		this.addTab(new TabROE(vue, this));
		// Prepare image for tab
		ImagePlus montage = Library_Capture_CSV.creerMontage(vue.getFrameDurations(), vue.getImpPost().getImagePlus(),
															 200, 4, 5);
		montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		this.addTab(new TabContrastModifier(this, "Timed Image", montage));
		if (Prefs.get(PrefTabRenal.PREF_PELVIS, true))
			this.addTab(new TabCort(vue, this));
		if (Prefs.get(PrefTabRenal.PREF_PELVIS, true))
			this.addTab(new TabUreter(vue, this));
		this.addTab(new TabZoomed(vue, this));
		this.addTab(new TabOther(vue, this));
		this.addTab(new TabPostMict(this));
		if (((Model_Renal) controller.getModel()).getPatlakChart() != null) {
			this.addTab(new TabPatlak(vue, this));
		}
		if (Prefs.get(PrefTabMain.PREF_EXPERIMENTS, false))
			this.addTab(new TabDeconvolve(this, "Deconvolve"));

		this.setTitle("Results Renal Exam");
		int height = 800;
		int width = 1000;
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
