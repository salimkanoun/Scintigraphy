package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

public class FenResultats_Renal {

	private Container principal, zoomed, kidneys, timedImage, tabCort, tabUreter, tabOther, tabPost;
	private final int width = 1000, height = 800;

	public FenResultats_Renal(VueScinDyn vue, BufferedImage capture, ChartPanel chartPanel) {
		this.principal = new TabPrincipal(vue, capture, chartPanel, width, height).getContentPane();
		this.zoomed = new TabZoomed(vue, width, height).getContentPane();
		this.kidneys = new TabROE(vue, width, height).getContentPane();
		this.timedImage = new TabTimedImage(vue, 4, 5, width, height).getContentPane();
		this.tabCort = new TabCort(vue, width, height).getContentPane();
		this.tabUreter = new TabUreter(vue, width, height).getContentPane();
		this.tabOther = new TabOther(vue, width, height).getContentPane();
		this.tabPost = new TabPostMict(vue, width, height).getContentPane();

		createAndShowGUI();
	}


	private void createAndShowGUI() {

		// Create and set up the window.
		final JFrame frame = new JFrame("Results Renal Exam");

		// set grid layout for the frame
		frame.getContentPane().setLayout(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.principal);
		tabbedPane.addTab("Timed image", this.timedImage);
		tabbedPane.addTab("ROE", this.kidneys);

		// si les pelvis sont activees
		if (RenalSettings.getOrganSettings()[1]) {
			tabbedPane.addTab("Corticals/Pelvis", this.tabCort);
		}

		// si les ureteres sont activees
		if (RenalSettings.getOrganSettings()[2]) {
			tabbedPane.addTab("Ureters", this.tabUreter);
		}

		tabbedPane.addTab("Zoomed", this.zoomed);
		tabbedPane.addTab("Other", this.tabOther);

		tabbedPane.addTab("Post-micturition", this.tabPost);

		frame.getContentPane().add(tabbedPane);

		// Display the window
		frame.setPreferredSize(new Dimension(width, height));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
