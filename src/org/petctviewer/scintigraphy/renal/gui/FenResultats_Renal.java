package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

public class FenResultats_Renal {

	private Container principal, zoomed, kidneys, timedImage, tabCort, tabUreter, tabOther, tabPost;

	public FenResultats_Renal(VueScinDyn vue, BufferedImage capture, ChartPanel chartPanel) {
		this.principal = new TabPrincipal(vue, capture, chartPanel).getContentPane();

		int w = this.principal.getWidth();
		int h = this.principal.getHeight();

		this.zoomed = new TabZoomed(vue).getContentPane();
		this.kidneys = new TabROE(vue, w, h).getContentPane();
		this.timedImage = new TabTimedImage(vue, principal.getHeight() / 4, 4, 5).getContentPane();
		this.tabCort = new TabCort(vue, w, h).getContentPane();
		this.tabUreter = new TabUreter(vue).getContentPane();
		this.tabOther = new TabOther(vue).getContentPane();
		this.tabPost = new TabPostMict(vue, w, h).getContentPane();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
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

		// si les corticales sont activees
		if (RenalSettings.getSettings()[1]) {
			tabbedPane.addTab("Corticals/Pelvis", this.tabCort);
		}

		// si les ureteres sont activees
		if (RenalSettings.getSettings()[2]) {
			tabbedPane.addTab("Ureters", this.tabUreter);
		}

		tabbedPane.addTab("Zoomed", this.zoomed);
		tabbedPane.addTab("Other", this.tabOther);
		
		tabbedPane.addTab("Post-micturition", this.tabPost);

		frame.getContentPane().add(tabbedPane);

		// Display the window
		frame.setPreferredSize(new Dimension(1000, 800));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
